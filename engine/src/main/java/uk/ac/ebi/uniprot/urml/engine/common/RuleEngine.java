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

import uk.ac.ebi.uniprot.urml.engine.drools.engine.DroolsRuleEngineFactory;

import java.util.Iterator;
import org.uniprot.urml.facts.Fact;
import org.uniprot.urml.rules.Rules;

/**
 * Rule engine specifications
 *
 * @author Alexandre Renaux
 */
public interface RuleEngine {

    /**
     * Starts the rule engine. Must be called before any other method.
     */
    void start();

    /**
     * Inserts a fact into the working memory.
     * Be sure to call {@link #start()} before.
     *
     * @param fact the fact
     */
    void insert(Fact fact);

    /**
     * Applies the rules on the inserted facts.
     */
    void fireAllRules();

    /**
     * Dispose the rule engine, removing all the inserted facts from the working memory.
     * The method {@link #start()} would need to be called to use the rule engine again.
     */
    void dispose();

    /**
     * Query all facts matching the specified fact class.
     *
     * @param <E> the fact class generics
     * @param factClass the fact class
     * @return an iterator of facts matching the specified class.
     */
    <E extends Fact> Iterator<E> query(Class<E> factClass);

    /**
     * Build and prepare a {@link RuleEngine} from a set of rules
     * @param rules a set of rules
     * @param ruleEngineVendor rule engine technology to use
     * @return a rule engine with all the loaded rules
     */
    static RuleEngine of(Rules rules, RuleEngineVendor ruleEngineVendor){
        if (RuleEngineVendor.DROOLS == ruleEngineVendor ) {
            return DroolsRuleEngineFactory.build(rules);
        }
        else {
            throw new IllegalArgumentException(String.format("Unsupported vendor %s", ruleEngineVendor));
        }
    }

}
