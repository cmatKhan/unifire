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

import uk.ac.ebi.kraken.datamining.unirule.annotationprocess.replacement.exceptions.ReplacementResolverException;
import uk.ac.ebi.kraken.datamining.unirule.annotationprocess.replacement.resolvers.MaxSigFreqResolver;
import uk.ac.ebi.kraken.model.unirule.Placeholder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uniprot.urml.facts.ProteinAnnotation;
import org.uniprot.urml.facts.ProteinSignature;

/**
 * Resolves {@link ProteinAnnotation#value} using {@link ProteinSignature} list to replace
 * {@link Placeholder#MAX_SIG_FREQ} placeholder.
 *
 * @author Alexandre Renaux
 */
public class MaxSigFreqPlaceholderResolver extends MaxSigFreqResolver implements PlaceholderResolver {

    private static final Logger logger = LoggerFactory.getLogger(MaxSigFreqPlaceholderResolver.class);

    private static final MaxSigFreqPlaceholderResolver maxSigFreqPlaceholderResolver = new MaxSigFreqPlaceholderResolver();
    private static final String INTERPRO_SIGN_PREFIX = "";

    public static void resolve(ProteinAnnotation proteinAnnotation, List<ProteinSignature> proteinSignatureList) {
        maxSigFreqPlaceholderResolver.resolveMaxSigFreq(proteinAnnotation, proteinSignatureList);
    }

    private void resolveMaxSigFreq(ProteinAnnotation proteinAnnotation, List<ProteinSignature> proteinSignatureList){
        Map<String, Integer> sigFrequencyMap = new HashMap<>();
        for (ProteinSignature proteinSignature : proteinSignatureList) {
            if (proteinSignature.getProtein().equals(proteinAnnotation.getProtein())) {
                sigFrequencyMap.put(proteinSignature.getSignature().getValue(), proteinSignature.getFrequency());
            }
        }
        try {
            resolve(proteinAnnotation, getResolvedAnnotationValue(proteinAnnotation.getValue(), sigFrequencyMap));
        } catch (ReplacementResolverException e) {
            logger.debug(e.getMessage());
        }
    }

    @Override
    public void reportZeroFrequency(String annotationValue) {
        logger.debug("The {} placeholder has a signature frequency of 0: {}",
                Placeholder.MAX_SIG_FREQ.getName(), annotationValue);
    }

    @Override
    public String getInterProXrefPrefix() {
        return INTERPRO_SIGN_PREFIX;
    }

}
