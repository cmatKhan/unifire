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
import org.uniprot.urml.facts.*;

import static com.google.common.collect.Sets.newHashSet;

/**
 * Aggregates a list of facts to create set of coherent facts around {@link TemplateProtein} / {@link Protein}:
 * <ul>
 *     <li>{@link TemplateProtein}</li>
 *     <li>{@link Collection<ProteinSignature>}</li>
 *     <li>{@link Organism} (optional)</li>
 * </ul>
 *
 * @author Alexandre Renaux
 */
public class ProteinFactSetAggregator implements FactSetAggregator {

    private Map<TemplateProtein, Collection<ProteinSignature>> factData;

    public ProteinFactSetAggregator(){
        this.factData = new HashMap<>();
    }

    @Override
    public boolean addFact(Fact fact) {
        if (fact instanceof TemplateProtein){
            if (!factData.containsKey(fact)){
                factData.put((TemplateProtein) fact, newHashSet());
                return true;
            }
        } else if (fact instanceof ProteinSignature){
            if (factData.containsKey(((ProteinSignature) fact).getProtein())){
                factData.get(((ProteinSignature) fact).getProtein()).add((ProteinSignature) fact);
            } else {
                factData.put(((ProteinSignature) fact).getProtein(), newHashSet((ProteinSignature) fact));
            }
            return true;
        }
        return false;
    }

    @Override
    public Collection<FactSet> getFactSets() {
        Collection<FactSet> factSets = new ArrayList<>();
        for (Map.Entry<TemplateProtein, Collection<ProteinSignature>> entry : factData.entrySet()) {
            TemplateProtein protein = entry.getKey();
            Collection<ProteinSignature> proteinSignatures = entry.getValue();
            FactSet factSet = new FactSet();
            factSet.getFact().add(protein);
            if (protein instanceof Protein){
                factSet.getFact().add(((Protein) protein).getOrganism());
            }
            proteinSignatures.forEach(proteinSignature -> factSet.getFact().add(proteinSignature));
            factSets.add(factSet);
        }
        return factSets;
    }

}
