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

package uk.ac.ebi.uniprot.urml.procedures.unirule.placeholders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.uniprot.urml.facts.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link MaxSigFreqPlaceholderResolver}
 *
 * @author Alexandre Renaux
 */
class MaxSigFreqPlaceholderResolverTest {

    @Test
    void resolveWithMultipleSignaturesWithSomeNotInArgs(){
        Protein protein = Protein.builder().withId("P12345")
                .withOrganism(Organism.builder().build())
                .withSequence(ProteinSequence.builder().build()).build();

        List<ProteinSignature> proteinSignatureList = new ArrayList<>();
        ProteinSignature sig1 = buildProteinSignature(protein, SignatureType.SMART, "SM00326", 1);
        ProteinSignature sig2 = buildProteinSignature(protein, SignatureType.SMART, "SM00999", 5);
        ProteinSignature sig3 = buildProteinSignature(protein, SignatureType.PROSITE, "PS50002", 3);
        ProteinSignature sig4 = buildProteinSignature(protein, SignatureType.PROSITE, "PS50008", 6);
        ProteinSignature sig5 = buildProteinSignature(protein, SignatureType.PFAM, "PF00018", 2);
        proteinSignatureList.addAll(Arrays.asList(sig1, sig2, sig3, sig4, sig5));

        ProteinAnnotation
                proteinAnnotation = buildProteinAnnotation("Contains @MAX_SIG_FREQ|SM00326,PS50002,PF00018|@ SH3 domain(s).", protein);
        MaxSigFreqPlaceholderResolver.resolve(proteinAnnotation, proteinSignatureList);
        assertThat(proteinAnnotation.getValue(), equalTo("Contains 3 SH3 domains."));
        assertFalse(proteinAnnotation.getHasPlaceholder());
    }

    @Test
    void resolveWithOneSignatureInMultipleArgs(){
        Protein protein = Protein.builder().withId("P12345")
                .withOrganism(Organism.builder().build())
                .withSequence(ProteinSequence.builder().build()).build();

        List<ProteinSignature> proteinSignatureList = new ArrayList<>();
        ProteinSignature sig1 = buildProteinSignature(protein, SignatureType.PFAM, "PF00018", 2);
        proteinSignatureList.add(sig1);

        ProteinAnnotation
                proteinAnnotation = buildProteinAnnotation("Contains @MAX_SIG_FREQ|SM00326,PS50002,PF00018|@ SH3 domain(s).", protein);
        MaxSigFreqPlaceholderResolver.resolve(proteinAnnotation, proteinSignatureList);
        assertThat(proteinAnnotation.getValue(), equalTo("Contains 2 SH3 domains."));
        assertFalse(proteinAnnotation.getHasPlaceholder());
    }

    @Test
    void resolveWithOneSignatureWithSingleOccurrenceRemovePlural(){
        Protein protein = Protein.builder().withId("P12345")
                .withOrganism(Organism.builder().build())
                .withSequence(ProteinSequence.builder().build()).build();

        List<ProteinSignature> proteinSignatureList = new ArrayList<>();
        ProteinSignature sig1 = buildProteinSignature(protein, SignatureType.PFAM, "PF00018", 1);
        ProteinSignature sig2 = buildProteinSignature(protein, SignatureType.SMART, "SM00326", 1);
        proteinSignatureList.addAll(Arrays.asList(sig1, sig2));

        ProteinAnnotation
                proteinAnnotation = buildProteinAnnotation("Contains @MAX_SIG_FREQ|SM00326,PF00018|@ SH3 domain(s).", protein);
        MaxSigFreqPlaceholderResolver.resolve(proteinAnnotation, proteinSignatureList);
        assertThat(proteinAnnotation.getValue(), equalTo("Contains 1 SH3 domain."));
        assertFalse(proteinAnnotation.getHasPlaceholder());
    }

    @Test
    void resolveMultipleMaxSigFreqPlaceholdersAreAllReplaced(){
        Protein protein = Protein.builder().withId("P12345")
                .withOrganism(Organism.builder().build())
                .withSequence(ProteinSequence.builder().build()).build();

        List<ProteinSignature> proteinSignatureList = new ArrayList<>();
        ProteinSignature sig1 = buildProteinSignature(protein, SignatureType.SMART, "SM00326", 1);
        ProteinSignature sig2 = buildProteinSignature(protein, SignatureType.PROSITE, "PS50002", 3);
        proteinSignatureList.addAll(Arrays.asList(sig1, sig2));

        ProteinAnnotation
                proteinAnnotation = buildProteinAnnotation("Contains @MAX_SIG_FREQ|SM00326,PS50002|@ SH3 domain(s). (...) containing @MAX_SIG_FREQ|SM00326,PS50002|@ SH3 domain(s).", protein);
        MaxSigFreqPlaceholderResolver.resolve(proteinAnnotation, proteinSignatureList);
        assertThat(proteinAnnotation.getValue(), equalTo("Contains 3 SH3 domains. (...) containing 3 SH3 domains."));
        assertFalse(proteinAnnotation.getHasPlaceholder());
    }

    @Test
    void resolveWithNoSignatureMatchingArgsReplacedWith0(){
        Protein protein = Protein.builder().withId("P12345")
                .withOrganism(Organism.builder().build())
                .withSequence(ProteinSequence.builder().build()).build();

        List<ProteinSignature> proteinSignatureList = new ArrayList<>();
        ProteinSignature sig1 = buildProteinSignature(protein, SignatureType.PFAM, "PF00018", 2);
        proteinSignatureList.add(sig1);

        ProteinAnnotation
                proteinAnnotation = buildProteinAnnotation("Contains @MAX_SIG_FREQ|SM00326,PS50002|@ SH3 domain(s).", protein);
        MaxSigFreqPlaceholderResolver.resolve(proteinAnnotation, proteinSignatureList);
        assertThat(proteinAnnotation.getValue(), equalTo("Contains 0 SH3 domains."));
        assertFalse(proteinAnnotation.getHasPlaceholder());
    }

    @Test
    void resolveWithNoSignatureAtAllReplacedWith0(){
        Protein protein = Protein.builder().withId("P12345")
                .withOrganism(Organism.builder().build())
                .withSequence(ProteinSequence.builder().build()).build();

        List<ProteinSignature> proteinSignatureList = new ArrayList<>();

        ProteinAnnotation
                proteinAnnotation = buildProteinAnnotation("Contains @MAX_SIG_FREQ|SM00326,PS50002|@ SH3 domain(s).", protein);
        MaxSigFreqPlaceholderResolver.resolve(proteinAnnotation, proteinSignatureList);
        assertThat(proteinAnnotation.getValue(), equalTo("Contains 0 SH3 domains."));
        assertFalse(proteinAnnotation.getHasPlaceholder());
    }

    @Test
    void resolveWithNoArgumentsDoesNotResolvePlaceholder(){
        Protein protein = Protein.builder().withId("P12345")
                .withOrganism(Organism.builder().build())
                .withSequence(ProteinSequence.builder().build()).build();

        List<ProteinSignature> proteinSignatureList = new ArrayList<>();
        ProteinSignature sig1 = buildProteinSignature(protein, SignatureType.PFAM, "PF00018", 2);
        proteinSignatureList.add(sig1);

        ProteinAnnotation
                proteinAnnotation = buildProteinAnnotation("Contains @MAX_SIG_FREQ||@ SH3 domain(s).", protein);
        MaxSigFreqPlaceholderResolver.resolve(proteinAnnotation, proteinSignatureList);
        assertThat(proteinAnnotation.getValue(), equalTo("Contains @MAX_SIG_FREQ||@ SH3 domain(s)."));
        assertTrue(proteinAnnotation.getHasPlaceholder());
    }


    private ProteinAnnotation buildProteinAnnotation(String annotationValue, Protein protein){
        return ProteinAnnotation.builder().withProtein(protein).withValue(annotationValue).withHasPlaceholder(true).build();
    }

    private ProteinSignature buildProteinSignature(Protein protein, SignatureType signatureType, String signatureName, int frequency){
        return ProteinSignature.builder().withProtein(protein).withSignature().withType(signatureType)
                .withValue(signatureName).end().withFrequency(frequency).build();
    }
}
