/*
 *  Copyright (c) 2021 European Molecular Biology Laboratory
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

package uk.ac.ebi.uniprot.urml.input.parsers.interpro.xml;

import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.hamcrest.core.Is;
import org.junit.jupiter.api.Test;
import org.uniprot.urml.facts.*;
import uk.ac.ebi.uniprot.urml.input.parsers.xml.interpro.InterProXmlFactSetChunkParser;
import uk.ac.ebi.uniprot.urml.input.parsers.xml.interpro.InterProXmlProteinParser;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.StreamSupport;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

class InterProXmlProteinChunkParserTest {

    private static final String BASE_PATH = "/samples/interpro.xml/";

    @Test
    void parseOneProteinOneXrefChunkParser() throws Exception {
        Iterator<FactSet> parsedFactSet;
        try (InputStream interproXmlIS = getClass().getResourceAsStream(BASE_PATH + "one_protein_matches.xml")) {
            InterProXmlFactSetChunkParser interProXmlChunkProteinParser = new InterProXmlFactSetChunkParser(interproXmlIS);
            assertThat(interProXmlChunkProteinParser.hasNext(), Is.is(true));
            parsedFactSet = interProXmlChunkProteinParser.nextChunk();
            assertThat(interProXmlChunkProteinParser.hasNext(), Is.is(false));
        }

        ConvertedInterProDataChecker checker = createCheckerForOneProteinOneXref();

        int proteinCounter = 0;
        while (parsedFactSet.hasNext()) {
            FactSet factSet = parsedFactSet.next();
            for (Fact fact : factSet.getFact()) {
                if (fact instanceof Protein) {
                    ++proteinCounter;
                }
            }
            checker.check(factSet);
        }
        assertThat(proteinCounter, Is.is(1));
    }

    @Test
    void parseOneProteinTwoXrefsChunkParser() throws Exception {
        Iterator<FactSet> parsedFactSet;
        try (InputStream interproXmlIS = getClass().getResourceAsStream(BASE_PATH + "one_protein_two_xrefs.xml")) {
            InterProXmlFactSetChunkParser interProXmlChunkProteinParser =
                    new InterProXmlFactSetChunkParser(interproXmlIS);
            assertThat(interProXmlChunkProteinParser.hasNext(), Is.is(true));
            parsedFactSet = interProXmlChunkProteinParser.nextChunk();
            assertThat(interProXmlChunkProteinParser.hasNext(), Is.is(false));
        }

        ConvertedInterProDataChecker checker = createCheckerForOneProteinTwoXrefs();

        int proteinCounter = 0;
        while (parsedFactSet.hasNext()) {
            FactSet factSet = parsedFactSet.next();
            for (Fact fact : factSet.getFact()) {
                if (fact instanceof Protein) {
                    ++proteinCounter;
                }
            }
            checker.check(factSet);
        }
        assertThat(proteinCounter, Is.is(2));
    }

    @Test
    void sameResultAsInterProXmlProteinParser() throws IOException, XMLStreamException, JAXBException {
        List<Fact> factsFromChunkParser = new ArrayList<>();
        try (InputStream interproXmlIS = getClass().getResourceAsStream(BASE_PATH + "UP000307542-ipr.xml")) {
            InterProXmlFactSetChunkParser interProXmlChunkProteinParser =
                    new InterProXmlFactSetChunkParser(interproXmlIS, 10);
            while (interProXmlChunkProteinParser.hasNext()) {
                Iterator<FactSet> parsedFactSet = interProXmlChunkProteinParser.nextChunk();
                Iterable<FactSet> iterableFactSet = () -> parsedFactSet;

                StreamSupport.stream(iterableFactSet.spliterator(), false)
                        .map(FactSet::getFact)
                        .forEach(factsFromChunkParser::addAll);
            }
        }

        List<Fact> factsFromParser = new ArrayList<>();
        try (InputStream interproXmlIS = getClass().getResourceAsStream(BASE_PATH + "UP000307542-ipr.xml")) {
            Iterator<FactSet> parsedFactSet = new InterProXmlProteinParser().parse(interproXmlIS);
            Iterable<FactSet> iterableFactSet = () -> parsedFactSet;

            StreamSupport.stream(iterableFactSet.spliterator(), false)
                    .map(FactSet::getFact)
                    .forEach(factsFromParser::addAll);
        }

        assertThat(factsFromChunkParser.size(), is(factsFromParser.size()));
        assertThat(factsFromChunkParser, IsIterableContainingInAnyOrder.containsInAnyOrder(factsFromParser.toArray()));
    }

    private ConvertedInterProDataChecker createCheckerForOneProteinOneXref() {
        return new ConvertedInterProDataChecker() {
            protected int expectedNumberOfProteins() {
                return 1;
            }

            protected int expectedNumberOfProteinSignatures() {
                return 7; // 2 integrated libraries + 2 IPR equivalents + 3 unintegrated libraries
            }

            protected void checkProtein(Protein protein) {
                assertThat(protein.getId(), equalTo("ABIZv1_0003"));
                assertThat(protein.getSequence().getLength(), equalTo(323));
                assertThat(protein.getSequence().getValue().length(), equalTo(323));
                assertFalse(protein.getSequence().getIsFragment());
            }

            protected void checkOrganism(Organism organism) {
                assertTrue(organism.isSetId());
                assertThat(organism.getLineage().getIds(),
                        containsInAnyOrder(1, 131567, 2, 1783257, 74201, 203494, 48461, 203557, 2735, 2736,
                                240016));
                assertThat(organism.getScientificName(), equalTo("Verrucomicrobium spinosum DSM 4136"));
            }

            protected void checkProteinSignature(ProteinSignature proteinSignature) {
                assertTrue(proteinSignature.isSetSignature() && proteinSignature.getSignature().isSetValue());
                switch (proteinSignature.getSignature().getValue()) {
                    case "PF01370", "IPR001509" -> {
                        checkInterProSignatureType(proteinSignature, SignatureType.PFAM);
                        assertThat(proteinSignature.getFrequency(), equalTo(1));
                    }
                    case "MF_00956", "IPR028614" -> {
                        checkInterProSignatureType(proteinSignature, SignatureType.HAMAP);
                        assertThat(proteinSignature.getFrequency(), equalTo(1));
                    }
                    case "PTHR43238", "PTHR43238:SF3" -> {
                        checkInterProSignatureType(proteinSignature, SignatureType.PANTHER);
                        assertThat(proteinSignature.getFrequency(), equalTo(1));
                    }
                    case "3.40.309.10:FF:000001" -> {
                        checkInterProSignatureType(proteinSignature, SignatureType.FUNFAM);
                        assertThat(proteinSignature.getFrequency(), equalTo(1));
                    }
                    default -> fail("Unexpected protein signature" + proteinSignature);
                }
            }
        };
    }

    private ConvertedInterProDataChecker createCheckerForOneProteinTwoXrefs() {
        return new ConvertedInterProDataChecker() {
            private List<String> expectedProteinNames = Arrays.asList(
                    // We have 4 signature matches for each protein, so the protein name is checked 5 times: once for
                    // ProteinFact and 4 times for ProteinSignatures
                    "A0A6B9WDM2", "A0A6B9WDM2", "A0A6B9WDM2", "A0A6B9WDM2", "A0A6B9WDM2",
                    "A0A6C0TBF5", "A0A6C0TBF5", "A0A6C0TBF5", "A0A6C0TBF5", "A0A6C0TBF5");

            protected int expectedNumberOfProteins() {
                return 1;
            }

            protected int expectedNumberOfProteinSignatures() {
                return 4; // 2 libraries + 2 IPR equivalents
            }

            protected void checkProtein(Protein protein) {
                assertThat(protein.getId(), equalTo(expectedProteinNames.get(proteinCounter)));
                assertThat(protein.getSequence().getLength(), equalTo(222));
                assertThat(protein.getSequence().getValue().length(), equalTo(222));
                assertFalse(protein.getSequence().getIsFragment());
            }

            protected void checkOrganism(Organism organism) {
                assertTrue(organism.isSetId());
                assertThat(organism.getLineage().getIds(), containsInAnyOrder(1, 10239, 2559587, 76804, 2499399, 11118, 2501931, 694002, 2509511, 694009, 2697049));
                assertThat(organism.getScientificName(), equalTo("Severe acute respiratory syndrome coronavirus 2"));
            }

            protected void checkProteinSignature(ProteinSignature proteinSignature) {
                assertTrue(proteinSignature.isSetSignature() && proteinSignature.getSignature().isSetValue());
                switch (proteinSignature.getSignature().getValue()) {
                    case "PF01635" -> {
                        checkInterProSignatureType(proteinSignature, SignatureType.PFAM);
                        assertThat(proteinSignature.getFrequency(), equalTo(1));
                    }
                    case "IPR002574" -> {
                        checkInterProSignatureType(proteinSignature, SignatureType.PFAM);
                        checkInterProSignatureType(proteinSignature, SignatureType.HAMAP);
                        assertThat(proteinSignature.getFrequency(), equalTo(1));
                    }
                    case "MF_04202" -> {
                        checkInterProSignatureType(proteinSignature, SignatureType.HAMAP);
                        assertThat(proteinSignature.getFrequency(), equalTo(1));
                    }
                    default -> fail("Unexpected protein signature" + proteinSignature);
                }
            }

        };
    }
}
