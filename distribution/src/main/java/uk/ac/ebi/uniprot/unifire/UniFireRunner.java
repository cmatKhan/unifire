/*
 *  Copyright (c) 2018 European Molecular Biology Laboratory
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package uk.ac.ebi.uniprot.unifire;

import uk.ac.ebi.uniprot.urml.core.UniFireRuntimeException;
import uk.ac.ebi.uniprot.urml.core.xml.readers.URMLRuleReader;
import uk.ac.ebi.uniprot.urml.engine.common.ProteinAnnotationRetriever;
import uk.ac.ebi.uniprot.urml.engine.common.RuleEngine;
import uk.ac.ebi.uniprot.urml.engine.common.RuleEngineVendor;
import uk.ac.ebi.uniprot.urml.engine.common.RuleExecution;
import uk.ac.ebi.uniprot.urml.input.InputType;
import uk.ac.ebi.uniprot.urml.input.collections.FactSetPartitioner;
import uk.ac.ebi.uniprot.urml.input.collections.TemplateProteinSignatureRetriever;
import uk.ac.ebi.uniprot.urml.input.parsers.FactSetChunkParser;
import uk.ac.ebi.uniprot.urml.input.parsers.FactSetParser;
import uk.ac.ebi.uniprot.urml.output.FactSetWriter;
import uk.ac.ebi.uniprot.urml.output.OutputFormat;
import uk.ac.ebi.uniprot.urml.procedures.ProcedureRuleInjector;

import java.io.*;
import java.util.Iterator;
import java.util.stream.StreamSupport;
import javax.xml.bind.JAXBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uniprot.urml.facts.FactSet;
import org.uniprot.urml.rules.Rules;

/**
 * Runner to launch the UniFIRE application
 *
 * @author Alexandre Renaux
 */
public class UniFireRunner {

    private static final Logger logger = LoggerFactory.getLogger(UniFireRunner.class);

    private final File ruleBaseFile;
    private final File inputFactFile;
    private final File outputFactFile;
    private final File templateFactFile;
    private final InputType inputType;
    private final OutputFormat outputFormat;
    private final Integer inputChunkSize;
    private final Boolean templatesProvided;
    private TemplateProteinSignatureRetriever templateRetriever;

    public UniFireRunner(File ruleBaseFile, File inputFactFile, File outputFactFile, InputType inputType,
            OutputFormat outputFormat, Integer inputChunkSize, File templateFactFile) {
        this.ruleBaseFile = ruleBaseFile;
        this.inputFactFile = inputFactFile;
        this.outputFactFile = outputFactFile;
        this.inputType = inputType;
        this.outputFormat = outputFormat;
        this.templateFactFile = templateFactFile;
        this.inputChunkSize = inputChunkSize;
        this.templatesProvided = templateFactFile != null;
        logArguments();
    }

    public void run() throws IOException, JAXBException {
        try (InputStream factInputStream = new FileInputStream(inputFactFile)){

            /* Ingest rules & build rule engine */
            URMLRuleReader urmlRuleReader = new URMLRuleReader();
            Rules rules = urmlRuleReader.read(new FileInputStream(ruleBaseFile));
            ProcedureRuleInjector procedureRuleInjector = new ProcedureRuleInjector();
            procedureRuleInjector.inject(rules);
            RuleEngine ruleEngine = RuleEngine.of(rules, RuleEngineVendor.DROOLS);

            /* Prepare the execution process */
            ProteinAnnotationRetriever factRetriever = new ProteinAnnotationRetriever(ruleEngine);
            RuleExecution ruleExecution = new RuleExecution(factRetriever);
            if (templatesProvided) initTemplateRetriever();


            /* Execute rules for each partition & output annotations */
            try (FactSetWriter factSetWriter = FactSetWriter.of(new FileOutputStream(outputFactFile), outputFormat)) {
                // URML-FACT input cannot be read chunk-by-chunk, because facts depend on each other and
                // therefore the XML cannot be splitted
                // e.g. a protein depends on an organism. If the organism-fact ends up in a different chunk than the
                // protein, then the protein is left without taxonomy property.
                if (InputType.FACT_XML == inputType) {
                    Iterator<FactSet> factSetIterator = FactSetParser.of(inputType).parse(factInputStream);
                    FactSetPartitioner factSetPartitioner = new FactSetPartitioner(factSetIterator, inputChunkSize);
                    factSetPartitioner.forEachRemaining(partition -> {
                        logger.info("Processing {} facts for {} proteins",
                                partition.getFactSet().getFact().size(), partition.getSize());
                        processFactSet(ruleEngine, ruleExecution, factSetWriter, partition.getFactSet());
                    });
                }
                else {
                    /* Ingest input data & partition them */
                    try(FactSetChunkParser factSetChunkParser =
                            FactSetChunkParser.of(inputType, factInputStream, inputChunkSize)) {
                        while (factSetChunkParser.hasNext()) {
                            FactSet factSet = mergeFactSets(factSetChunkParser.nextChunk());
                            logger.info("Processing {} facts for {} proteins ", factSet.getFact().size(),
                                    inputChunkSize);
                            processFactSet(ruleEngine, ruleExecution, factSetWriter, factSet);
                        }
                    } catch (Exception e) {
                        throw new UniFireRuntimeException("Error while processing input fact data in chunks.", e);
                    }
                }

            } finally {
                ruleEngine.dispose();
            }
        }
    }

    private void processFactSet(RuleEngine ruleEngine, RuleExecution ruleExecution, FactSetWriter factSetWriter,
            FactSet factSet) {
        ruleEngine.start();
        if (templatesProvided) {
            templateRetriever.retrieveFor(factSet).forEach(ruleEngine::insert);
        }
        FactSet outputFacts = ruleExecution.apply(ruleEngine, factSet);
        logger.info("Write {} prediction(s)", outputFacts.getFact().size());
        factSetWriter.write(outputFacts);
        ruleEngine.dispose();
        logger.info("Done processing facts.");
    }

    private FactSet mergeFactSets(Iterator<FactSet> factSetIterator) {
        FactSet.Builder<Void> factSetBuilder = FactSet.builder();
        Iterable<FactSet> factSetIterable = () -> factSetIterator;
        StreamSupport.stream(factSetIterable.spliterator(), false)
                .flatMap(fs -> fs.getFact().stream())
                .forEach(factSetBuilder::addFact);

        return factSetBuilder.build();
    }

    private void initTemplateRetriever() throws IOException {
        try (InputStream templateFactStream = new FileInputStream(templateFactFile)) {
            Iterator<FactSet> templateFacts = FactSetParser.of(InputType.FACT_XML).parse(templateFactStream);
            templateRetriever = new TemplateProteinSignatureRetriever(templateFacts);
        }
    }

    private void logArguments(){
        logger.info("Launching UniFIRE with:");
        logger.info("  Rule file           = {}", ruleBaseFile.getAbsolutePath());
        logger.info("  Input fact file     = {}", inputFactFile.getAbsolutePath());
        logger.info("  Input source        = {}", inputType.getDescription());
        logger.info("  Output fact file    = {}", outputFactFile.getAbsolutePath());
        logger.info("  Output format       = {}", outputFormat.getDescription());
        logger.info("  Template fact file  = {}",
                (templatesProvided ? templateFactFile.getAbsolutePath() : "Not provided"));
        logger.info("  Input chunk size    = {}", inputChunkSize);
        logger.info("-----------------------");
    }

}
