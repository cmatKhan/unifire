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

import uk.ac.ebi.kraken.datamining.unirule.annotationprocess.replacement.resolvers.GeneNameResolver;
import uk.ac.ebi.kraken.model.unirule.Placeholder;

import java.util.List;
import org.uniprot.urml.facts.GeneInformation;
import org.uniprot.urml.facts.Protein;
import org.uniprot.urml.facts.ProteinAnnotation;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Resolves {@link ProteinAnnotation#value} to replace {@link Placeholder#GENE_NAME} placeholder
 * using {@link Protein}->{@link GeneInformation} data</li>
 *
 * @author Alexandre Renaux
 */
public class GeneNamePlaceholderResolver extends GeneNameResolver implements PlaceholderResolver {

    private static final GeneNamePlaceholderResolver genePlaceholderResolver = new GeneNamePlaceholderResolver();

    public static void resolve(ProteinAnnotation proteinAnnotation){
        genePlaceholderResolver.resolveGeneName(proteinAnnotation);
    }

    private void resolveGeneName(ProteinAnnotation proteinAnnotation){
        List<String> geneNames;
        GeneInformation gene = proteinAnnotation.getProtein().getGene();
        geneNames = gene != null ? gene.getNames() : newArrayList();
        resolve(proteinAnnotation, getResolvedAnnotationValue(proteinAnnotation.getValue(), geneNames));
    }

}
