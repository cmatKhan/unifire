package org.proteininformationresource.pirsr;

import uk.ac.ebi.uniprot.urml.core.utils.FactMerger;
import uk.ac.ebi.uniprot.urml.core.xml.writers.URMLFactWriter;
import uk.ac.ebi.uniprot.urml.input.InputType;
import uk.ac.ebi.uniprot.urml.input.parsers.FactSetParser;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uniprot.urml.facts.*;

/**
 * Runner to launch the PIRSR application
 *
 * @author Chuming Chen
 */
public class PIRSRRunner {

	private static final Logger logger = LoggerFactory.getLogger(PIRSRRunner.class);
	private final File pirsrDataDirectory;
	private final File inputFactFile;
	private final InputType inputType;
	private final File outputDirectory;
	private final File hmmalignCommand;

	public PIRSRRunner(File pirsrDataDirectory, File inputFactFile, InputType inputType, File outputDirectory,
			File hmmalignCommand) {
		this.pirsrDataDirectory = pirsrDataDirectory;
		this.inputFactFile = inputFactFile;
		this.inputType = inputType;
		this.outputDirectory = outputDirectory;
		this.hmmalignCommand = hmmalignCommand;
		logArguments();
	}

	public void run() throws IOException {
		Set<PIRSR> pirsrInfo = getPIRSRInfo(pirsrDataDirectory);
		Map<String, Set<PIRSR>> pirsrTriggerMap = getTriggerMap(pirsrInfo);
		logger.info("Collecting triggered proteins from InterProScan XML file...");
		Map<Protein, Set<PIRSR>> triggeredProteins = getTriggerProteins(pirsrTriggerMap);
		logger.info("Done collecting triggered proteins from InterProScan XML file.");

		Set<PIRSR> triggeredPIRSR = createFasta(triggeredProteins);
		logger.info("Running hmmalign of triggered proteins against SRHMM...");
		runHMMAlign(triggeredPIRSR);
		logger.info("Done running hmmalign of matched proteins against SRHMM.");
		List<PositionalProteinSignature> matchedProteinFacts = getMatchedProteinFacts(triggeredProteins);

		logger.info("Adding SRHMM signatures to InterProScan input file...");
		addPositionalProteinSignature(matchedProteinFacts);
		logger.info("Done adding SRHMM signatures to InterProScan input file.");

		logger.info(String.format("The enhanced InterProScan XML file is at \"%s/%s\"", this.outputDirectory,
				this.inputFactFile.getName().replaceAll("(?i).xml$", "-urml.xml")));
	}

	private void addPositionalProteinSignature(List<PositionalProteinSignature> matchedProteinFacts) {

		String outFile = this.inputFactFile.getName().replaceAll("(?i).xml$", "-urml.xml");
		try (InputStream factInputStream = new FileInputStream(this.inputFactFile);
				OutputStream outputStream = new FileOutputStream(this.outputDirectory + "/" + outFile);
				URMLFactWriter factWriter = new URMLFactWriter(outputStream)) {

			Iterator<FactSet> factSetIterator = FactSetParser.of(inputType).parse(factInputStream);
			FactMerger factMerger = new FactMerger();
			FactSet updatedFactSet = new FactSet();
			logger.info("Merging facts...");
			updatedFactSet.setFact(factMerger.merge(factSetIterator, matchedProteinFacts));
			logger.info("Done merging facts. In total we have {} facts.", updatedFactSet.getFact().size());

			logger.info("Writing facts...");
			factWriter.write(updatedFactSet);
			logger.info("Done writing facts.");
		}
		catch (IOException | JAXBException | XMLStreamException e) {
			logger.error(e.getMessage());
		}
	}

	private Map<Protein, Set<PIRSR>> getTriggerProteins(Map<String, Set<PIRSR>> pirsrTriggerMap)
			throws IOException {
		Map<Protein, Set<PIRSR>> triggeredProteins = new HashMap<>();
		try(InputStream factInputStream = new FileInputStream(this.inputFactFile)) {
			Iterator<FactSet> factSetIterator = FactSetParser.of(inputType).parse(factInputStream);

			while (factSetIterator.hasNext()) {
				FactSet factSet = factSetIterator.next();

				List<Fact> facts = new ArrayList<>(factSet.getFact());

				Protein protein = null;
				for (Fact fact : facts) {
					if (fact instanceof Protein) {
						protein = (Protein) fact;
					}
					if (fact instanceof ProteinSignature) {
						ProteinSignature proteinSignature = (ProteinSignature) fact;
						String signature = proteinSignature.getSignature().getValue();
						Set<PIRSR> pirsrList = pirsrTriggerMap.get(signature);
						if (pirsrList != null) {
							Set<PIRSR> triggeredPIRSRList = triggeredProteins.get(protein);
							if (triggeredPIRSRList == null) {
								triggeredPIRSRList = new HashSet<>();
							}
							triggeredPIRSRList.addAll(pirsrList);
							triggeredProteins.put(protein, triggeredPIRSRList);
						}
					}
				}
			}
		}
		return triggeredProteins;
	}

	private List<PositionalProteinSignature> getMatchedProteinFacts(Map<Protein, Set<PIRSR>> triggeredProteins) {
		Set<PIRSR> matchedPIRSRs = new HashSet<>();
		for (Set<PIRSR> prisrs : triggeredProteins.values()) {
			matchedPIRSRs.addAll(prisrs);
		}

		List<Protein> matchedProteins = new ArrayList<>(triggeredProteins.keySet());
		Map<String, Protein> proteinMap = new HashMap<>();
		for (Protein protein : matchedProteins) {
			proteinMap.put(protein.getId(), protein);
		}
		List<PositionalProteinSignature> matchedProteinFacts = new ArrayList<>();
		for (PIRSR pirsr : matchedPIRSRs) {
			String alignOutFile = this.outputDirectory + "/aln/" + pirsr.getRuleAC() + ".aln";
			File f = new File(alignOutFile);

			Map<String, String> acToAlignment = new HashMap<>();
			List<String> lines;
			try {
				lines = FileUtils.readLines(f, "UTF-8");

				for (String line : lines) {
					line = line.trim();
					if (!(line.startsWith("#") || line.length() == 0 || line.startsWith("//"))) {
						String[] aln = line.split("\\s+");
						
						String alignment = acToAlignment.get(aln[0]);

						if (alignment == null) {
							alignment = "";
						} 
						alignment += aln[1];
						alignment = alignment.replaceAll("\\.", "");
						acToAlignment.put(aln[0], alignment);
						
					}
				}
				for (Map.Entry<String,String> acToAl: acToAlignment.entrySet()) {
					Protein protein = proteinMap.get(acToAl.getKey());
					String srhmmAlign = acToAl.getValue();
					
					PositionalProteinSignature pps = createPositionalProteinSignature(protein, pirsr, 1,
							protein.getSequence().getLength(), srhmmAlign);
					matchedProteinFacts.add(pps);
				}

			} catch (IOException e) {
				logger.error(e.getMessage());
			}

		}
		return matchedProteinFacts;
	}

	
	private PositionalProteinSignature createPositionalProteinSignature(Protein protein, PIRSR pirsr, int start, int end, String aln) {
		PositionalProteinSignature positionalProteinSignature = new PositionalProteinSignature();

		positionalProteinSignature.setProtein(protein);

		Signature signature = new Signature();
		signature.setValue(pirsr.getSrhmmAC());
		signature.setType(SignatureType.SRHMM);
		positionalProteinSignature.setSignature(signature);

		positionalProteinSignature.setFrequency(1);

		positionalProteinSignature.setPositionStart(start);
		positionalProteinSignature.setPositionEnd(end);

		SequenceAlignment sequenceAlignment = new SequenceAlignment();
		sequenceAlignment.setValue(aln);

		positionalProteinSignature.setAlignment(sequenceAlignment);
		return positionalProteinSignature;
	}

	private void runHMMAlign(Set<PIRSR> triggeredPIRSR) throws IOException {
		for (PIRSR pirsr : triggeredPIRSR) {
			String alignOutFile = this.outputDirectory + "/aln/" + pirsr.getRuleAC() + ".aln";
			File file = new File(alignOutFile);
			if (!file.getParentFile().isDirectory() && !file.getParentFile().mkdirs()) {
				throw new IOException(String.format("Cannot create folder %s.", file.getParentFile().getPath()));
			}
			String matchedSeqFile = this.outputDirectory + "/seq/" + pirsr.getRuleAC() + ".fasta";
			String srHMMModelFile = this.pirsrDataDirectory + "/sr_hmm/" + pirsr.getRuleAC() + ".hmm";
			
			String hmmalignCommandLine = this.hmmalignCommand + " -o " + alignOutFile + " " + srHMMModelFile + " " + matchedSeqFile;
			String commandMsg = executeCommand(hmmalignCommandLine);
			if (commandMsg != null && commandMsg.length() > 0) {
				logger.error(commandMsg);
				System.exit(1);
			}
		}

	}

	private static String executeCommand(String command) {

		StringBuilder output = new StringBuilder();

		Process p;
		try {
			p = Runtime.getRuntime().exec(command);
			p.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

			String line;
			while ((line = reader.readLine()) != null) {
				output.append(line).append("\n");
			}
			reader = new BufferedReader(new InputStreamReader(p.getErrorStream()));

			while ((line = reader.readLine()) != null) {
				output.append(line).append("\n");
			}

		} catch (Exception e) {
			logger.error(e.getMessage());
		}

		return output.toString();

	}

	private Set<PIRSR> createFasta(Map<Protein, Set<PIRSR>> triggeredProteins) throws IOException {
		Map<PIRSR, Set<Protein>> targetFasta = new HashMap<>();
		for (Entry<Protein, Set<PIRSR>> entry : triggeredProteins.entrySet()) {
			Protein protein = entry.getKey();
			Set<PIRSR> pirsrs = entry.getValue();

			for (PIRSR pirsr : pirsrs) {
				Set<Protein> proteins = targetFasta.get(pirsr);
				if (proteins == null) {
					proteins = new HashSet<>();
				}
				proteins.add(protein);
				targetFasta.put(pirsr, proteins);
			}
		}

		for (Entry<PIRSR, Set<Protein>> entry : targetFasta.entrySet()) {
			PIRSR pirsr = entry.getKey();
			File file = new File(this.outputDirectory + "/seq/" + pirsr.getRuleAC() + ".fasta");
			if (!file.getParentFile().isDirectory() && !file.getParentFile().mkdirs()) {
				throw new IOException(String.format("Cannot create folder %s", file.getParentFile().getPath()));
			}
			StringBuilder fasta = new StringBuilder();
			for (Protein p : entry.getValue()) {
				fasta.append(">").append(p.getId()).append("\n");
				fasta.append(p.getSequence().getValue()).append("\n");
			}
			try (FileWriter writer = new FileWriter(file)) {
				writer.write(fasta.toString());
			} catch (IOException e) {
				logger.error(e.getMessage());
			}
		}
		return targetFasta.keySet();

	}

	private Map<String, Set<PIRSR>> getTriggerMap(Set<PIRSR> pirsrInfo) {
		Map<String, Set<PIRSR>> triggerMap = new HashMap<>();
		for (PIRSR pirsr : pirsrInfo) {
			String trigger = pirsr.getTrigger();
			Set<PIRSR> pirsrList = triggerMap.get(trigger);
			if (pirsrList == null) {
				pirsrList = new HashSet<>();
			}
			pirsrList.add(pirsr);
			triggerMap.put(trigger, pirsrList);
		}
		return triggerMap;
	}

	private Set<PIRSR> getPIRSRInfo(File pirsrDataDirectory) {
		Set<PIRSR> pirsrInfo = new HashSet<>();
		try {

			File f = new File(pirsrDataDirectory + "/sr_tp/sr_tp.seq");

			List<String> lines = FileUtils.readLines(f, "UTF-8");
			String ruleAC = "";
			String trigger = "";
			String srhmmAC = "";
			String templateAC = "";
			String templateSeq = "";
			for (String line : lines) {
				if (line.startsWith(">")) {
					String[] defLine = line.split("\t");
					templateAC = defLine[0].replace(">", "");
					ruleAC = defLine[1];
					srhmmAC = ruleAC.replace("PIRSR", "SRHMM");
					String[] rule = ruleAC.split("-");
					trigger = rule[0];
					if (trigger.startsWith("PIRSR6")) {
						trigger = trigger.replace("PIRSR6", "IPR0");
					} else {
						trigger = trigger.replace("PIRSR", "PIRSF");
					}
				} else {
					templateSeq = line;
					pirsrInfo.add(new PIRSR(ruleAC, trigger, srhmmAC, templateAC, templateSeq));
				}
			}

		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		return pirsrInfo;
	}

	private void logArguments() {
		logger.info("Launching PRISR with:");
		logger.info("  PIRSR Data Directory  = {}", this.pirsrDataDirectory.getAbsolutePath());
		logger.info("  Input fact file       = {}", this.inputFactFile.getAbsolutePath());
		logger.info("  Output Directory      = {}", this.outputDirectory.getAbsolutePath());
		logger.info("  hmmalign Command     = {}", this.hmmalignCommand.getAbsolutePath());
		logger.info("-----------------------");
	}
}
