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

package uk.ac.ebi.uniprot.urml.procedures;

import uk.ac.ebi.uniprot.urml.core.xml.readers.URMLRuleReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import javax.xml.bind.JAXBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uniprot.urml.rules.Rules;

/**
 * Injects procedure rules according to the rule base name
 *
 * @author Alexandre Renaux
 */
public class ProcedureRuleInjector {

    private final Logger logger = LoggerFactory.getLogger(ProcedureRuleInjector.class);


    private static final String BASE_PATH = "/proceduralRules/";
    private final URMLRuleReader urmlRuleReader;

    public ProcedureRuleInjector() {
        this.urmlRuleReader = new URMLRuleReader();
    }

    public void inject(Rules rules){
        Optional<Rules> proceduralRules = getProceduralRules(rules.getName());
        proceduralRules.ifPresent(procRules -> rules.getRule().addAll(procRules.getRule()));
    }

    private Optional<Rules> getProceduralRules(String ruleBaseName) {
        if (ruleBaseName.equals("org.uniprot.unirule")) {
            logger.debug("Add {} procedural rules to the rule base", ruleBaseName);
            return Optional.of(getRules("unirule-procedural-rules.xml", ruleBaseName));
        }
        return Optional.empty();
    }

    private Rules getRules(String urmlRuleFileName, String ruleBaseName){
        InputStream ruleIS = ProcedureRuleInjector.class.getResourceAsStream(BASE_PATH +urmlRuleFileName);
        try {
            return urmlRuleReader.read(ruleIS);
        } catch (JAXBException | IOException e) {
            throw new IllegalStateException("Procedural rules for "+ ruleBaseName +" cannot be retrieved", e);
        }
    }

}
