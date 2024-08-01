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
 * Unit tests for {@link GeneNamePlaceholderResolver}
 *
 * @author Alexandre Renaux
 */
class GeneNamePlaceholderResolverTest {

    @Test
    void resolveGeneNamePlaceholderWithOneGeneReplacedWithItsName(){
        GeneInformation gene = GeneInformation.builder().withNames("geneA").build();

        ProteinAnnotation proteinAnnotation = buildProteinAnnotationWithGenes("Annotation with gene named @GENE_NAME@.", gene);
        GeneNamePlaceholderResolver.resolve(proteinAnnotation);
        assertThat(proteinAnnotation.getValue(), equalTo("Annotation with gene named geneA."));
        assertFalse(proteinAnnotation.getHasPlaceholder());
    }

    @Test
    void resolveGeneNamePlaceholderWithMultipleGenesReplacedWithFirstGeneName(){
        GeneInformation gene = GeneInformation.builder().withNames("geneA", "geneB").build();

        ProteinAnnotation proteinAnnotation = buildProteinAnnotationWithGenes("Annotation with gene named @GENE_NAME@.", gene);
        GeneNamePlaceholderResolver.resolve(proteinAnnotation);
        assertThat(proteinAnnotation.getValue(), equalTo("Annotation with gene named geneA."));
        assertFalse(proteinAnnotation.getHasPlaceholder());
    }

    @Test
    void resolveMultipleGeneNamePlaceholdersWithOneGeneAllReplacedWithItsName(){
        GeneInformation gene = GeneInformation.builder().withNames("geneA").build();

        ProteinAnnotation proteinAnnotation = buildProteinAnnotationWithGenes("Annotation with gene named @GENE_NAME@. It is @GENE_NAME@.", gene);
        GeneNamePlaceholderResolver.resolve(proteinAnnotation);
        assertThat(proteinAnnotation.getValue(), equalTo("Annotation with gene named geneA. It is geneA."));
        assertFalse(proteinAnnotation.getHasPlaceholder());
    }

    @Test
    void resolveMultipleGeneNamePlaceholdersWithMultipleGenesAllReplacedWithFirstGeneName(){
        GeneInformation gene = GeneInformation.builder().withNames("geneA", "geneB").build();

        ProteinAnnotation proteinAnnotation = buildProteinAnnotationWithGenes("Annotation with gene named @GENE_NAME@. It is @GENE_NAME@.", gene);
        GeneNamePlaceholderResolver.resolve(proteinAnnotation);
        assertThat(proteinAnnotation.getValue(), equalTo("Annotation with gene named geneA. It is geneA."));
        assertFalse(proteinAnnotation.getHasPlaceholder());
    }

    @Test
    void resolveGeneNamePlaceholderWithNoGeneReplacedWithEmptyString(){
        ProteinAnnotation proteinAnnotation = buildProteinAnnotationWithGenes("Annotation with gene named @GENE_NAME@.", null);
        GeneNamePlaceholderResolver.resolve(proteinAnnotation);
        assertThat(proteinAnnotation.getValue(), equalTo("Annotation with gene named ."));
        assertFalse(proteinAnnotation.getHasPlaceholder());
    }

    @Test
    void resolveGeneNamePlaceholderWithEmptyGeneNameReplaceWithEmptyString(){
        GeneInformation gene = GeneInformation.builder().withOrfOrOlnNames("orf1").build();

        ProteinAnnotation proteinAnnotation = buildProteinAnnotationWithGenes("Annotation with gene named @GENE_NAME@.", gene);
        GeneNamePlaceholderResolver.resolve(proteinAnnotation);
        assertThat(proteinAnnotation.getValue(), equalTo("Annotation with gene named ."));
        assertFalse(proteinAnnotation.getHasPlaceholder());
    }

    private ProteinAnnotation buildProteinAnnotationWithGenes(String annotationValue, GeneInformation geneData){
        Protein.Builder<Void> builder = Protein.builder().withId("P12345").withOrganism(Organism.builder().build())
                .withSequence(ProteinSequence.builder().build());
        if (geneData != null){
            builder.withGene(geneData);
        }
        return ProteinAnnotation.builder().withProtein(builder.build()).withValue(annotationValue)
                .withHasPlaceholder(true).build();
    }

}
