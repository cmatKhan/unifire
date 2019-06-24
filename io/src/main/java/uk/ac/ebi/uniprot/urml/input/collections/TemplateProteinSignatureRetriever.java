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

package uk.ac.ebi.uniprot.urml.input.collections;

import java.util.*;
import java.util.stream.Collectors;
import org.uniprot.urml.facts.Fact;
import org.uniprot.urml.facts.FactSet;
import org.uniprot.urml.facts.PositionalProteinSignature;
import org.uniprot.urml.facts.TemplateProteinSignature;

/**
 * Retrieves {@link TemplateProteinSignature} facts
 *
 * @author Alexandre Renaux
 */
public class TemplateProteinSignatureRetriever {

    private final Map<String, Fact> templateProteinSignatureMap;

    public TemplateProteinSignatureRetriever(Iterator<FactSet> templateFacts) {
        this.templateProteinSignatureMap = new HashMap<>();
        init(templateFacts);
    }

    public Collection<Fact> retrieveFor(FactSet partition){
        return partition.getFact().stream().filter(fact -> fact instanceof PositionalProteinSignature)
                .map(fact -> ((PositionalProteinSignature) fact).getSignature().getValue())
                .filter(templateProteinSignatureMap::containsKey).map(templateProteinSignatureMap::get)
                .collect(Collectors.toList());
    }

    public Collection<Fact> retrieveAll(){
        return templateProteinSignatureMap.values();
    }

    private void init(Iterator<FactSet> templateFacts){
        templateFacts.forEachRemaining(fs -> fs.getFact().forEach(f -> {
            if (f instanceof TemplateProteinSignature){
                TemplateProteinSignature templateProteinSignature = (TemplateProteinSignature) f;
                templateProteinSignatureMap.put(templateProteinSignature.getSignature().getValue(),
                        templateProteinSignature);
            }
        }));
    }
}
