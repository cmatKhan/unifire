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
import java.util.Iterator;
import org.junit.jupiter.api.Test;
import org.uniprot.urml.facts.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
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
    public void parse() throws Exception {
        InputStream interproXmlIS = getClass().getResourceAsStream(BASE_PATH+"one_protein_matches.xml");
        InterProXmlProteinParser interProXmlProteinParser = new InterProXmlProteinParser();
        Iterator<FactSet> parsedFactSet = interProXmlProteinParser.parse(interproXmlIS);

        new ConvertedInterProDataChecker() {
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

        }.check(parsedFactSet);
    }

    private abstract class ConvertedInterProDataChecker {

        private int proteinCounter = 0;
        private int organismCounter = 0;
        private int proteinSignatureCounter = 0;

        void check(Iterator<FactSet> data){
            while (data.hasNext()){
                FactSet factSet = data.next();
                for (Fact fact : factSet.getFact()) {
                    if (fact instanceof Organism) {
                        checkOrganism((Organism) fact);
                        organismCounter++;
                    } else if (fact instanceof ProteinSignature) {
                        ProteinSignature proteinSignature = (ProteinSignature) fact;
                        checkProteinSignature(proteinSignature);
                        checkProtein((Protein) proteinSignature.getProtein());
                        proteinSignatureCounter++;
                    } else if (fact instanceof Protein){
                        Protein protein = (Protein) fact;
                        checkProtein(protein);
                        checkOrganism(protein.getOrganism());
                        proteinCounter++;
                    } else {
                        fail("Unexpected fact type: "+fact);
                    }
                }
                assertThat(organismCounter, equalTo(1));
                assertThat(proteinSignatureCounter, equalTo(expectedNumberOfProteinSignatures()));
            }
            assertThat(proteinCounter, equalTo(expectedNumberOfProteins()));
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