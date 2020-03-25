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

package uk.ac.ebi.uniprot.urml.input.parsers.interpro.xml;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.uniprot.urml.facts.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Unit tests for {@link InterProXmlProteinParser}
 *
 * @author Alexandre Renaux
 */
public class InterProXmlProteinParserTest {

    private static final String BASE_PATH = "/samples/interpro.xml/";

    @Test
    public void parseOneProteinOneXref() throws Exception {
        InputStream interproXmlIS = getClass().getResourceAsStream(BASE_PATH+"one_protein_matches.xml");
        InterProXmlProteinParser interProXmlProteinParser = new InterProXmlProteinParser();
        Iterator<FactSet> parsedFactSet = interProXmlProteinParser.parse(interproXmlIS);

        ConvertedInterProDataChecker checker = new ConvertedInterProDataChecker() {
            protected int expectedNumberOfProteins() {
                return 1;
            }

            protected int expectedNumberOfProteinSignatures() {
                return 4; // 2 libraries + 2 IPR equivalents
            }

            protected void checkProtein(Protein protein) {
                assertThat(protein.getId(), equalTo("ABIZv1_0003"));
                assertThat(protein.getSequence().getLength(), equalTo(323));
                assertThat(protein.getSequence().getValue().length(), equalTo(323));
                assertFalse(protein.getSequence().getIsFragment());
            }

            protected void checkOrganism(Organism organism) {
                assertTrue(organism.isSetId());
                assertThat(organism.getLineage().getIds(), containsInAnyOrder(1,131567,2,1783257,74201,203494,48461,203557,2735,2736,240016));
                assertThat(organism.getScientificName(), equalTo("Verrucomicrobium spinosum DSM 4136"));
            }

            protected void checkProteinSignature(ProteinSignature proteinSignature) {
                assertTrue(proteinSignature.isSetSignature() && proteinSignature.getSignature().isSetValue());
                switch (proteinSignature.getSignature().getValue()){
                    case "PF01370": case "IPR001509":
                        checkInterProSignatureType(proteinSignature, SignatureType.PFAM);
                        assertThat(proteinSignature.getFrequency(), equalTo(1));
                        break;
                    case "MF_00956":case "IPR028614":
                        checkInterProSignatureType(proteinSignature, SignatureType.HAMAP);
                        assertThat(proteinSignature.getFrequency(), equalTo(1));
                        break;
                    default:
                        fail("Unexpected protein signature"+proteinSignature);
                }
            }

        };

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
        assertThat(proteinCounter, is(1));
    }

    @Test
    public void parseOneProteinTwoXrefs() throws Exception {
        InputStream interproXmlIS = getClass().getResourceAsStream(BASE_PATH+"one_protein_two_xrefs.xml");
        InterProXmlProteinParser interProXmlProteinParser = new InterProXmlProteinParser();
        Iterator<FactSet> parsedFactSet = interProXmlProteinParser.parse(interproXmlIS);

        ConvertedInterProDataChecker checker = new ConvertedInterProDataChecker() {
            private List<String> expectedProteinNames = Arrays.asList(
                    // We have 4 signature matches for each protein, so the protein name is checked 5 times: once for
                    // ProteinFact and 4 times for ProteinSignatures
                    "A0A6B9WDM2","A0A6B9WDM2","A0A6B9WDM2","A0A6B9WDM2","A0A6B9WDM2",
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
                assertThat(organism.getLineage().getIds(), containsInAnyOrder(1,10239,2559587,76804,2499399,11118,2501931,694002,2509511,694009,2697049));
                assertThat(organism.getScientificName(), equalTo("Severe acute respiratory syndrome coronavirus 2"));
            }

            protected void checkProteinSignature(ProteinSignature proteinSignature) {
                assertTrue(proteinSignature.isSetSignature() && proteinSignature.getSignature().isSetValue());
                switch (proteinSignature.getSignature().getValue()){
                    case "PF01635":
                        checkInterProSignatureType(proteinSignature, SignatureType.PFAM);
                        assertThat(proteinSignature.getFrequency(), equalTo(1));
                        break;
                    case "IPR002574":
                        checkInterProSignatureType(proteinSignature, SignatureType.PFAM);
                        checkInterProSignatureType(proteinSignature, SignatureType.HAMAP);
                        assertThat(proteinSignature.getFrequency(), equalTo(1));
                        break;
                    case "MF_04202":
                        checkInterProSignatureType(proteinSignature, SignatureType.HAMAP);
                        assertThat(proteinSignature.getFrequency(), equalTo(1));
                        break;
                    default:
                        fail("Unexpected protein signature"+proteinSignature);
                }
            }

        };

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
        assertThat(proteinCounter, is(2));
    }

    private abstract class ConvertedInterProDataChecker {

        protected int proteinCounter = 0;
        private int organismCounter = 0;
        private int proteinSignatureCounter = 0;

        void check(FactSet factSet){
            organismCounter = 0;
            proteinSignatureCounter = 0;
            for (Fact fact : factSet.getFact()) {
                if (fact instanceof Organism) {
                    checkOrganism((Organism) fact);
                    organismCounter++;
                } else if (fact instanceof ProteinSignature) {
                    ProteinSignature proteinSignature = (ProteinSignature) fact;
                    checkProteinSignature(proteinSignature);
                    checkProtein((Protein) proteinSignature.getProtein());
                    proteinSignatureCounter++;
                    proteinCounter++;
                } else if (fact instanceof Protein){
                    Protein protein = (Protein) fact;
                    checkProtein(protein);
                    checkOrganism(protein.getOrganism());
                    proteinCounter++;
                } else {
                    fail("Unexpected fact type: "+fact);
                }
            }
            assertThat(organismCounter, equalTo(organismCounter));
            assertThat(proteinSignatureCounter, equalTo(expectedNumberOfProteinSignatures()));
        }

        protected abstract int expectedNumberOfProteins();

        protected abstract int expectedNumberOfProteinSignatures();

        protected abstract void checkProtein(Protein protein);

        protected abstract void checkOrganism(Organism organism);

        protected abstract void checkProteinSignature(ProteinSignature proteinSignature);

        void checkInterProSignatureType(ProteinSignature proteinSignature, SignatureType expectedLibraryType){
            if (proteinSignature.getSignature().getValue().startsWith("IPR")){
                assertThat(proteinSignature.getSignature().getType(), equalTo(SignatureType.INTER_PRO));
            } else {
                assertThat(proteinSignature.getSignature().getType(), equalTo(expectedLibraryType));
            }
        }
    }

}