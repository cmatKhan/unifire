package org.proteininformationresource.pirsr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Collections;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uniprot.urml.facts.Fact;
import org.uniprot.urml.facts.FactSet;
import org.uniprot.urml.facts.PositionalProteinSignature;
import org.uniprot.urml.facts.Protein;
import org.uniprot.urml.facts.ProteinSignature;
import org.uniprot.urml.facts.SequenceAlignment;
import org.uniprot.urml.facts.Signature;
import org.uniprot.urml.facts.SignatureType;

import uk.ac.ebi.uniprot.urml.core.xml.writers.URMLFactWriter;
import uk.ac.ebi.uniprot.urml.input.InputType;
import uk.ac.ebi.uniprot.urml.input.parsers.FactSetParser;

/**
 * Runner to launch the PIRSR application
 *
 * @author Chuming Chen
 */
public class PIRSRRunner {

	private static final Logger logger = LoggerFactory.getLogger(PIRSRRunner.class);
	private final File pirsrDataDirectory;
	private final File inputFactFile;
	private final File outputDirectory;
	private final File hmmalignCommand;

	public PIRSRRunner(File pirsrDataDirectory, File inputFactFile, File outputDirectory, File hmmalignCommand) {
		this.pirsrDataDirectory = pirsrDataDirectory;
		this.inputFactFile = inputFactFile;
		this.outputDirectory = outputDirectory;
		this.hmmalignCommand = hmmalignCommand;
		logArguments();
	}

	public void run() throws IOException, JAXBException {
		Set<PIRSR> pirsrInfo = getPIRSRInfo(pirsrDataDirectory);
		Map<String, Set<PIRSR>> pirsrTriggerMap = getTriggerMap(pirsrInfo);
		logger.info("Collecting triggered proteins from InterProScan XML file...");
		Map<Protein, Set<PIRSR>> triggeredProteins = getTriggerProteins(pirsrTriggerMap);
		logger.info("Done collecting triggered proteins from InterProScan XML file.");

		Set<PIRSR> triggeredPIRSR = createFasta(triggeredProteins);
		logger.info("Running hmmalign of triggered proteins against SRHMM...");
		runHMMAlign(triggeredPIRSR);
		logger.info("Done running hmmalign of matched proteins against SRHMM.");
		Map<Protein, Set<PositionalProteinSignature>> matchedProteinFacts = getMatchedProteinFacts(triggeredProteins);

		logger.info("Adding SRHMM signatures to InterProScan XML file...");
		addPositionalProteinSignature(matchedProteinFacts);
		logger.info("Done adding SRHMM signatures to InterProScan XML file.");

		logger.info("The enhanced InterProScan XML file is at \"" + this.outputDirectory + "/"
				+ this.inputFactFile.getName().replaceAll("(?i).xml$", "-urml.xml") + "\"");
	}

	private void addPositionalProteinSignature(Map<Protein, Set<PositionalProteinSignature>> matchedProteinFacts) {

		String outFile = this.inputFactFile.getName().replaceAll("(?i).xml$", "-urml.xml");
		try {
			InputStream factInputStream = new FileInputStream(this.inputFactFile);
			OutputStream outputStream = new FileOutputStream(this.outputDirectory + "/" + outFile);

			URMLFactWriter factWriter = new URMLFactWriter(outputStream);
			FactSet updatedFactSet = new FactSet();
			List<Fact> allFacts = new ArrayList<Fact>();
			Iterator<FactSet> factSetIterator = FactSetParser.of(InputType.INTERPROSCAN_XML).parse(factInputStream);
			while (factSetIterator.hasNext()) {
				FactSet factSet = factSetIterator.next();
				List<Fact> facts = new ArrayList<>(factSet.getFact());

				Protein protein = null;
				for (Fact fact : facts) {
					if (fact instanceof Protein) {
						protein = (Protein) fact;
					}
				}
				for (PositionalProteinSignature positionalProteinSignature : matchedProteinFacts.getOrDefault(protein, Collections.emptySet())) {
					facts.add(positionalProteinSignature);
				}
				allFacts.addAll(facts);
			}
			updatedFactSet.setFact(allFacts);
			factWriter.write(updatedFactSet);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JAXBException e) {
			e.printStackTrace();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}

	}

	private Map<Protein, Set<PIRSR>> getTriggerProteins(Map<String, Set<PIRSR>> pirsrTriggerMap) throws FileNotFoundException, IOException {
		Map<Protein, Set<PIRSR>> triggeredProteins = new HashMap<Protein, Set<PIRSR>>();
		InputStream factInputStream = new FileInputStream(this.inputFactFile);
		Iterator<FactSet> factSetIterator = FactSetParser.of(InputType.INTERPROSCAN_XML).parse(factInputStream);

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
							triggeredPIRSRList = new HashSet<PIRSR>();
						}
						triggeredPIRSRList.addAll(pirsrList);
						triggeredProteins.put(protein, triggeredPIRSRList);
					}
				}
			}
		}
		return triggeredProteins;
	}

	private Map<Protein, Set<PositionalProteinSignature>> getMatchedProteinFacts(Map<Protein, Set<PIRSR>> triggeredProteins) {
		Set<PIRSR> matchedPIRSRs = new HashSet<PIRSR>();
		for (Set<PIRSR> prisrs : triggeredProteins.values()) {
			matchedPIRSRs.addAll(prisrs);
		}

		List<Protein> matchedProteins = new ArrayList<Protein>(triggeredProteins.keySet());
		Map<String, Protein> proteinMap = new HashMap<String, Protein>();
		for (Protein protein : matchedProteins) {
			proteinMap.put(protein.getId(), protein);
		}
		Map<Protein, Set<PositionalProteinSignature>> matchedProteinFacts = new HashMap<Protein, Set<PositionalProteinSignature>>();
		for (PIRSR pirsr : matchedPIRSRs) {
			String alignOutFile = this.outputDirectory + "/aln/" + pirsr.getRuleAC() + ".aln";
			File f = new File(alignOutFile);

			Map<String, String> acToAlignment = new HashMap<String, String>();
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
				for (String ac : acToAlignment.keySet()) {
					String srhmmAlign = acToAlignment.get(ac);
					
					PositionalProteinSignature pps = createPositionalProteinSignature(proteinMap.get(ac), pirsr, 1, proteinMap.get(ac).getSequence().getLength(), srhmmAlign);
					Set<PositionalProteinSignature> signatures = matchedProteinFacts.get(proteinMap.get(ac));
					if (signatures == null) {
						signatures = new HashSet<PositionalProteinSignature>();
					}
					signatures.add(pps);
					matchedProteinFacts.put(proteinMap.get(ac), signatures);
				}

			} catch (IOException e) {
				e.printStackTrace();
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

	private void runHMMAlign(Set<PIRSR> triggeredPIRSR) {
		for (PIRSR pirsr : triggeredPIRSR) {
			String alignOutFile = this.outputDirectory + "/aln/" + pirsr.getRuleAC() + ".aln";
			File file = new File(alignOutFile);
			file.getParentFile().mkdirs();
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

		StringBuffer output = new StringBuffer();

		Process p;
		try {
			p = Runtime.getRuntime().exec(command);
			p.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

			String line = "";
			while ((line = reader.readLine()) != null) {
				output.append(line + "\n");
			}
			reader = new BufferedReader(new InputStreamReader(p.getErrorStream()));

			line = "";
			while ((line = reader.readLine()) != null) {
				output.append(line + "\n");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return output.toString();

	}

	private Set<PIRSR> createFasta(Map<Protein, Set<PIRSR>> triggeredProteins) {
		Map<PIRSR, Set<Protein>> targetFasta = new HashMap<PIRSR, Set<Protein>>();
		for (Entry<Protein, Set<PIRSR>> entry : triggeredProteins.entrySet()) {
			Protein protein = (Protein) entry.getKey();
			Set<PIRSR> pirsrs = entry.getValue();

			for (PIRSR pirsr : pirsrs) {
				Set<Protein> proteins = targetFasta.get(pirsr);
				if (proteins == null) {
					proteins = new HashSet<Protein>();
				}
				proteins.add(protein);
				targetFasta.put(pirsr, proteins);
			}
		}

		for (Entry<PIRSR, Set<Protein>> entry : targetFasta.entrySet()) {
			PIRSR pirsr = (PIRSR) entry.getKey();
			File file = new File(this.outputDirectory + "/seq/" + pirsr.getRuleAC() + ".fasta");
			file.getParentFile().mkdirs();
			String fasta = "";
			for (Protein p : entry.getValue()) {
				fasta += ">" + p.getId() + "\n";
				fasta += p.getSequence().getValue() + "\n";
			}
			try {
				FileWriter writer = new FileWriter(file);
				writer.write(fasta);
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return targetFasta.keySet();

	}

	private Map<String, Set<PIRSR>> getTriggerMap(Set<PIRSR> pirsrInfo) {
		Map<String, Set<PIRSR>> triggerMap = new HashMap<String, Set<PIRSR>>();
		for (PIRSR pirsr : pirsrInfo) {
			String trigger = pirsr.getTrigger();
			Set<PIRSR> pirsrList = triggerMap.get(trigger);
			if (pirsrList == null) {
				pirsrList = new HashSet<PIRSR>();
			}
			pirsrList.add(pirsr);
			triggerMap.put(trigger, pirsrList);
		}
		return triggerMap;
	}

	private Set<PIRSR> getPIRSRInfo(File pirsrDataDirectory) {
		Set<PIRSR> pirsrInfo = new HashSet<PIRSR>();
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
			e.printStackTrace();
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
