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

package uk.ac.ebi.uniprot.urml.procedures.unirule.positionalfeatures;

import uk.ac.ebi.kraken.datamining.unirule.annotationprocess.featureprocess.SequenceRange;
import uk.ac.ebi.kraken.datamining.unirule.annotationprocess.featureprocess.positional.PositionProcessor;
import uk.ac.ebi.kraken.datamining.unirule.annotationprocess.featureprocess.positional.exceptions
        .PositionalFeatureMappingException;
import uk.ac.ebi.kraken.datamining.unirule.annotationprocess.replacement.exceptions.ReplacementResolverException;
import uk.ac.ebi.uniprot.urml.procedures.unirule.PositionalMappingData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uniprot.urml.facts.PositionalMapping;

/**
 *
 *
 * @author arenaux
 */
public class PositionalMapper {

    private static final Logger logger = LoggerFactory.getLogger(PositionalMapper.class);

    public static void map(PositionalMapping positionalMapping){
        try {
            PositionProcessor positionProcessor = new PositionProcessor();
            String start = positionalMapping.getTemplateStart();
            String end = positionalMapping.getTemplateEnd();

            PositionalMappingData positionProcessingData = new PositionalMappingData(positionalMapping);

            SequenceRange sequenceRange = positionProcessor.processPositions(start, end, positionProcessingData);
            positionalMapping.setMappedStart(sequenceRange.aaStartPosition());
            positionalMapping.setMappedEnd(sequenceRange.aaStopPosition());
            positionalMapping.setMappedSequence(sequenceRange.extractSubstring(positionalMapping.getProtein().getSequence().getValue()));
            positionalMapping.setIsMapped(true);
            positionalMapping.setIsValid(true);
        } catch (PositionalFeatureMappingException | ReplacementResolverException | InvalidPositionalMappingDataException e) {
            positionalMapping.setIsMapped(true);
            positionalMapping.setIsValid(false);
            logger.warn(e.getMessage());
        }
    }

}
