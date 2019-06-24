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

package uk.ac.ebi.uniprot.urml.procedures.unirule;

import uk.ac.ebi.kraken.datamining.unirule.annotationprocess.featureprocess.positional.model
        .PositionProcessingProteinData;
import uk.ac.ebi.kraken.datamining.unirule.annotationprocess.featureprocess.positional.model.SequenceAlignment;
import uk.ac.ebi.kraken.datamining.unirule.annotationprocess.featureprocess.positional.model.SignatureMatch;
import uk.ac.ebi.kraken.interfaces.unirule.PositionProcessingData;
import uk.ac.ebi.uniprot.urml.procedures.unirule.positionalfeatures.InvalidPositionalMappingDataException;

import org.uniprot.urml.facts.PositionalMapping;
import org.uniprot.urml.facts.PositionalProteinSignature;
import org.uniprot.urml.facts.Protein;

/**
 * Facade for all data necessary for positional feature processing.
 * It transforms {@link PositionalMapping} fact data to {@link SequenceAlignment}, {@link SignatureMatch} and
 * {@link PositionProcessingProteinData}.
 *
 * @author Alexandre Renaux
 */
public class PositionalMappingData implements PositionProcessingData {

    private final SequenceAlignment templateAlignment;

    private final SequenceAlignment targetAlignment;

    private final SignatureMatch targetMatch;

    private final PositionProcessingProteinData targetProteinData;

    public PositionalMappingData(PositionalMapping positionalMapping) throws InvalidPositionalMappingDataException {
        nullSafetyCheck(positionalMapping);
        PositionalProteinSignature templateMatch = positionalMapping.getTemplateMatch();
        if (templateMatch == null || templateMatch.getSignature() == null || templateMatch.getAlignment() == null) {
            this.templateAlignment = null;
        } else {
            this.templateAlignment = new SequenceAlignment(templateMatch.getSignature().getValue(),
                    templateMatch.getAlignment().getValue(), templateMatch.getPositionStart());
        }
        PositionalProteinSignature targetMatch = positionalMapping.getTargetMatch();
        this.targetAlignment =
                    new SequenceAlignment(targetMatch.getSignature().getValue(), targetMatch.getAlignment().getValue(),
                            targetMatch.getPositionStart());
        Protein protein = positionalMapping.getProtein();
        this.targetProteinData = new PositionProcessingProteinData(protein.getId(), protein.getSequence().getLength());
        this.targetMatch = new SignatureMatch(targetMatch.getSignature().getValue(), targetMatch.getPositionStart(),
                    targetMatch.getPositionEnd());
    }

    @Override
    public SequenceAlignment getTemplateAlignment() {
        return templateAlignment;
    }

    @Override
    public SequenceAlignment getTargetAlignment() {
        return targetAlignment;
    }

    @Override
    public SignatureMatch getInterProMatchForTargetAlignment() {
        return targetMatch;
    }

    @Override
    public PositionProcessingProteinData getTargetProteinData() {
        return targetProteinData;
    }

    private void nullSafetyCheck(PositionalMapping positionalMapping) throws InvalidPositionalMappingDataException {
        if (positionalMapping == null){
            throw new InvalidPositionalMappingDataException("Null positional mapping.");
        }
        PositionalProteinSignature targetMatch = positionalMapping.getTargetMatch();
        if (targetMatch == null || targetMatch.getSignature() == null || targetMatch.getAlignment() == null) {
            throw new InvalidPositionalMappingDataException("Invalid positional mapping, missing target match data: "+positionalMapping);
        }
    }
}
