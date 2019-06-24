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

import org.uniprot.urml.facts.Fact;
import org.uniprot.urml.facts.FactSet;

import java.util.Collection;

/**
 * Aggregates a list of facts to produce a sets of coherent facts, i.e {@link FactSet} containing all the necessary
 * facts to trigger a rule.
 *
 * @author Alexandre Renaux
 */
public interface FactSetAggregator {

    boolean addFact(Fact fact);

    Collection<FactSet> getFactSets();

}
