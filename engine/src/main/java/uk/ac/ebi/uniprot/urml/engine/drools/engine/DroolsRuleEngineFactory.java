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

package uk.ac.ebi.uniprot.urml.engine.drools.engine;

import uk.ac.ebi.uniprot.urml.engine.drools.compiler.URMLToDroolsCompiler;

import org.uniprot.urml.rules.Rules;

/**
 * Builds a Drools rule engine {@link DroolsRuleEngine} from a set of rules {@link Rules}
 *
 * @author Alexandre Renaux
 */
public class DroolsRuleEngineFactory {

    private DroolsRuleEngineFactory() {
        throw new IllegalStateException("Farctory class DroolsRuleEngineFactory is not meant to be instantiated.");
    }

    public static DroolsRuleEngine build(Rules rules){
        URMLToDroolsCompiler urmlToDroolsCompiler = new URMLToDroolsCompiler();
        return new DroolsRuleEngine(urmlToDroolsCompiler.compile(rules));
    }

}
