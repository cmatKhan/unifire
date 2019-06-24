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

package uk.ac.ebi.uniprot.urml.engine.drools;

import uk.ac.ebi.uniprot.urml.core.xml.readers.URMLFactReader;
import uk.ac.ebi.uniprot.urml.core.xml.readers.URMLRuleReader;
import uk.ac.ebi.uniprot.urml.engine.common.ProteinAnnotationRetriever;
import uk.ac.ebi.uniprot.urml.engine.common.RuleEngine;
import uk.ac.ebi.uniprot.urml.engine.common.RuleEngineVendor;
import uk.ac.ebi.uniprot.urml.engine.common.RuleExecution;

import javax.xml.bind.Unmarshaller;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.uniprot.urml.facts.FactSet;
import org.uniprot.urml.rules.Rules;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

/**
 * Blackbox testing for Drools execution including:
 *      - URML to DRL transpilling
 *      - Drools rulebase compilation
 *      - Fact XML reading into FactSet
 *      - RuleExecution
 *      - Comparison of output FactSet with expected FactSet.
 *
 * @author Alexandre Renaux
 */

public class DroolsExecutionTest {

    @ParameterizedTest(name = "{index}: ruleFileName={0}, factFileName={1}")
    @CsvSource({ "simple_rule, facts_1"})
    public void droolsExecutionReturnExpectedFacts(String ruleFileName, String factFileName) throws Exception {
        URMLRuleReader ruleReader = new URMLRuleReader();
        Rules rules = ruleReader.read(this.getClass().getResourceAsStream("/rules/" +ruleFileName+".xml"));

        RuleEngine ruleEngine = RuleEngine.of(rules, RuleEngineVendor.DROOLS);

        URMLFactReader factReader = new URMLFactReader();
        FactSet inputFacts = factReader.read(this.getClass().getResourceAsStream(
                "/facts/input/" +ruleFileName+"/"+factFileName+".xml"));

        ProteinAnnotationRetriever retriever = new ProteinAnnotationRetriever(ruleEngine);
        ruleEngine.start();
        FactSet outputFacts = new RuleExecution(retriever).apply(ruleEngine, inputFacts);
        ruleEngine.dispose();

        Unmarshaller unmarshaller = factReader
                .loadDependentFacts(this.getClass().getResourceAsStream("/facts/input/"+ruleFileName+"/"+factFileName+".xml"));
        FactSet expectedOutputFacts = factReader.read(this.getClass().getResourceAsStream(
                "/facts/output/" +ruleFileName+"/"+factFileName+".out.xml"), unmarshaller);

        assertThat(outputFacts.getFact(), containsInAnyOrder(expectedOutputFacts.getFact().toArray()));
    }
}
