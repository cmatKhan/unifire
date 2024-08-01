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

import org.junit.jupiter.api.Test;
import org.uniprot.urml.facts.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Unit tests for {@link OlnOrOrfNamePlaceholderResolver}.
 *
 * @author Alexandre Renaux
 */
class OlnOrOrfNamePlaceholderResolverTest {

    @Test
    void noGenesResolvedToEmptyString(){
        ProteinAnnotation
                proteinAnnotation = buildProteinAnnotationWithPlaceholdersWithGenes("UPF0107 protein @OLN_OR_ORF_NAME@.", null);
        OlnOrOrfNamePlaceholderResolver.resolve(proteinAnnotation);
        assertThat(proteinAnnotation.getValue(), equalTo("UPF0107 protein ."));
        assertFalse(proteinAnnotation.getHasPlaceholder());
    }

    @Test
    void emptyGeneResolvedToEmptyString(){
        GeneInformation gene = GeneInformation.builder().build();
        ProteinAnnotation
                proteinAnnotation = buildProteinAnnotationWithPlaceholdersWithGenes("UPF0107 protein @OLN_OR_ORF_NAME@.", gene);
        OlnOrOrfNamePlaceholderResolver.resolve(proteinAnnotation);
        assertThat(proteinAnnotation.getValue(), equalTo("UPF0107 protein ."));
        assertFalse(proteinAnnotation.getHasPlaceholder());
    }

    @Test
    void oneGeneWithAllMultipleGeneFieldsResolvedToFirstOln(){
        GeneInformation gene = GeneInformation.builder().withNames("geneA", "synonA1", "synonA2").withOrfOrOlnNames("olnA1", "olnA2").build();
        ProteinAnnotation
                proteinAnnotation = buildProteinAnnotationWithPlaceholdersWithGenes("UPF0107 protein @OLN_OR_ORF_NAME@.", gene);
        OlnOrOrfNamePlaceholderResolver.resolve(proteinAnnotation);
        assertThat(proteinAnnotation.getValue(), equalTo("UPF0107 protein olnA1."));
        assertFalse(proteinAnnotation.getHasPlaceholder());
    }

    @Test
    void oneGeneWithNoOlnButMultipleOrfAndMultipleNameResolvedToFirstOrf(){
        GeneInformation gene = GeneInformation.builder().withNames("geneA", "synonA1", "synonA2").withOrfOrOlnNames("orfA1", "orfA2").build();
        ProteinAnnotation
                proteinAnnotation = buildProteinAnnotationWithPlaceholdersWithGenes("UPF0107 protein @OLN_OR_ORF_NAME@.", gene);
        OlnOrOrfNamePlaceholderResolver.resolve(proteinAnnotation);
        assertThat(proteinAnnotation.getValue(), equalTo("UPF0107 protein orfA1."));
        assertFalse(proteinAnnotation.getHasPlaceholder());
    }

    @Test
    void oneGeneWithNoOlnNoOrfButMultipleNamesResolvedToFirstName(){
        GeneInformation gene = GeneInformation.builder().withNames("geneA", "synonA1", "synonA2").build();
        ProteinAnnotation
                proteinAnnotation = buildProteinAnnotationWithPlaceholdersWithGenes("UPF0107 protein @OLN_OR_ORF_NAME@.", gene);
        OlnOrOrfNamePlaceholderResolver.resolve(proteinAnnotation);
        assertThat(proteinAnnotation.getValue(), equalTo("UPF0107 protein geneA."));
        assertFalse(proteinAnnotation.getHasPlaceholder());
    }

    @Test
    void multiplePlaceholderAllResolved(){
        GeneInformation gene = GeneInformation.builder().withNames("geneA", "synonA1", "synonA2").withOrfOrOlnNames("olnA1", "olnA2").build();

        ProteinAnnotation
                proteinAnnotation = buildProteinAnnotationWithPlaceholdersWithGenes("UPF0107 protein @OLN_OR_ORF_NAME@. It is @OLN_OR_ORF_NAME@.", gene);
        OlnOrOrfNamePlaceholderResolver.resolve(proteinAnnotation);
        assertThat(proteinAnnotation.getValue(), equalTo("UPF0107 protein olnA1. It is olnA1."));
        assertFalse(proteinAnnotation.getHasPlaceholder());
    }

    private ProteinAnnotation buildProteinAnnotationWithPlaceholdersWithGenes(String annotationValue, GeneInformation geneData){
        Protein.Builder<Void> proteinBuilder = Protein.builder().withId("P12345").withOrganism(Organism.builder().build())
                .withSequence(ProteinSequence.builder().build());
        Protein protein = (geneData != null) ? proteinBuilder.withGene(geneData).build() : proteinBuilder.build();
        return ProteinAnnotation.builder().withProtein(protein).withHasPlaceholder(true).withValue(annotationValue).build();
    }

}
