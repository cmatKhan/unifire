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

package uk.ac.ebi.uniprot.urml.engine.common;

import java.util.Collection;
import java.util.function.BiFunction;
import org.uniprot.urml.facts.Fact;
import org.uniprot.urml.facts.FactSet;

/**
 * Functional representation of the rule execution process.
 * The execution process needs a {@link RuleEngine} (already started) and some input {@link FactSet} to produce output
 * {@link FactSet} retrieved with the given {@link FactRetriever}.
 *
 * @author Alexandre Renaux
 */
public class RuleExecution implements BiFunction<RuleEngine, FactSet, FactSet> {

    private final FactRetriever<? extends Fact> factRetriever;

    public RuleExecution(FactRetriever<? extends Fact> factRetriever){
        this.factRetriever = factRetriever;
    }

    @Override
    public FactSet apply(RuleEngine ruleEngine, FactSet inputFacts) {
        for (Fact fact : inputFacts.getFact()) {
            ruleEngine.insert(fact);
        }
        ruleEngine.fireAllRules();
        Collection<? extends Fact> outputFacts = factRetriever.retrieveAll();
        return FactSet.builder().withFact(outputFacts).build();
    }
}
