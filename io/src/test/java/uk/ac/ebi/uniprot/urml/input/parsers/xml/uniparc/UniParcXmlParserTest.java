package uk.ac.ebi.uniprot.urml.input.parsers.xml.uniparc;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.uniprot.urml.facts.Fact;
import org.uniprot.urml.facts.FactSet;
import uk.ac.ebi.uniprot.urml.core.xml.readers.URMLFactReader;
import uk.ac.ebi.uniprot.urml.core.xml.writers.URMLWriter;
import uk.ac.ebi.uniprot.urml.output.xml.IndividualFactWriter;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;

class UniParcXmlParserTest {

    private static final String BASE_PATH = "src/test/resources/samples/uniparc/";

    @Test
    void parseOneProtein() throws Exception {

        try (InputStream uniparcIS = new FileInputStream(BASE_PATH + "one_protein.xml")) {
            UniParcXmlParser uniParcXmlParser = new UniParcXmlParser();
            Iterator<FactSet> parsedFactSet = uniParcXmlParser.parse(uniparcIS);

            //collect facts for comparison
            List<Fact> parsedFactsList = new ArrayList<>();
            parsedFactSet.forEachRemaining(f -> parsedFactsList.addAll(f.getFact()));

            //read expected facts from file
            String expectedXmlFilePath = BASE_PATH + "one_protein_facts.xml";
            FactSet expectedFactSet;
            try(InputStream inputStream = new FileInputStream(expectedXmlFilePath)) {
                URMLFactReader factReader = new URMLFactReader();
                expectedFactSet = factReader.read(inputStream);
            }

            assertThat(parsedFactsList, Matchers.containsInAnyOrder(expectedFactSet.getFact().toArray()));
        }
    }

    private void writeFactsFile(Iterator<FactSet> factSetIterator, String outputPath) throws Exception {
        List<FactSet> factSetList = new ArrayList<>();
        factSetIterator.forEachRemaining(factSetList::add);

        try (OutputStream outputStream = new FileOutputStream(outputPath);
             URMLWriter<FactSet, Fact> factWriter = new IndividualFactWriter(outputStream)) {
            for (FactSet factSet : factSetList) {
                for (Fact fact : factSet.getFact()) {
                    factWriter.writeElementWise(fact);
                }
                factWriter.completeWrite();
            }
        }
    }
}