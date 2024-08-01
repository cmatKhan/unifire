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

import org.uniprot.urml.facts.*;

/**
 * Utils static methods for procedure unit testing
 *
 * @author Alexandre Renaux
 */
public class TestUtils {

    public static Protein createProtein(String id, String sequence) {
        return Protein.builder().withId(id).withSequence()
                .withLength(sequence == null ? 0 : sequence.length()).withIsFragment(false)
                .withValue(sequence).end().build();
    }

    public static Signature createSignature(SignatureType signatureType, String signatureValue){
        return Signature.builder().withType(signatureType).withValue(signatureValue).build();
    }

    public static PositionalProteinSignature createPositionalProteinSignature(TemplateProtein protein,
                                                                              Signature signature, int positionStart, int positionEnd, String alignment){
        return PositionalProteinSignature.builder()
                .withSignature(signature)
                .withProtein(protein)
                .withPositionStart(positionStart).withPositionEnd(positionEnd)
                .withAlignment().withValue(alignment).end()
                .build();
    }

    public static PositionalMapping createUnmappedPositionalMapping(PositionalProteinSignature targetMatch,
                                                                    PositionalProteinSignature templateMatch, String templateStart, String templateEnd){
        return PositionalMapping.builder().withIsValid(false).withProtein((Protein) targetMatch.getProtein())
                .withTemplateMatch(templateMatch).withTargetMatch(targetMatch)
                .withTemplateStart(templateStart).withTemplateEnd(templateEnd)
                .build();
    }
}