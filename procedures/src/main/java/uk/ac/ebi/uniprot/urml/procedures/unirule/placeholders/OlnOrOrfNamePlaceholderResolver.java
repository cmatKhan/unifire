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

import uk.ac.ebi.kraken.datamining.unirule.annotationprocess.replacement.resolvers.LocusTagResolver;
import uk.ac.ebi.kraken.model.unirule.Placeholder;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uniprot.urml.facts.GeneInformation;
import org.uniprot.urml.facts.Protein;
import org.uniprot.urml.facts.ProteinAnnotation;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Resolves {@link ProteinAnnotation#value} using {@link Protein}->{@link GeneInformation} data to replace
 * {@link Placeholder#OLN_ORF_NAME} placeholder.
 *
 * @author Alexandre Renaux
 */
public class OlnOrOrfNamePlaceholderResolver extends LocusTagResolver implements PlaceholderResolver {

    private static final Logger logger = LoggerFactory.getLogger(OlnOrOrfNamePlaceholderResolver.class);

    private static final OlnOrOrfNamePlaceholderResolver olnOrOrfNamePlaceholderResolver = new OlnOrOrfNamePlaceholderResolver();

    public static void resolve(ProteinAnnotation proteinAnnotation){
        olnOrOrfNamePlaceholderResolver.resolveOlnOrOlfName(proteinAnnotation);
    }

    public void resolveOlnOrOlfName(ProteinAnnotation proteinAnnotation){
        Protein protein = proteinAnnotation.getProtein();
        GeneInformation geneInformation = protein.getGene();
        List<String> names = geneInformation != null ? geneInformation.getNames() : newArrayList();
        List<String> orfOrOlnNames = geneInformation != null ? geneInformation.getOrfOrOlnNames() : newArrayList();
        resolve(proteinAnnotation, getResolvedAnnotationValue(proteinAnnotation.getValue(),
                orfOrOlnNames, names, protein.getId()));
    }

    @Override
    public void reportMissingData(String accession) {
        logger.debug("{} replacement for {}", Placeholder.OLN_ORF_NAME.getName(), accession);
    }
}
