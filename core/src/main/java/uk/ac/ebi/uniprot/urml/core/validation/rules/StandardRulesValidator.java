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

package uk.ac.ebi.uniprot.urml.core.validation.rules;

import uk.ac.ebi.uniprot.urml.core.validation.ValidationErrors;

import org.uniprot.urml.rules.Rule;
import org.uniprot.urml.rules.Rules;

/**
 * Provides basic validation for {@link Rules}
 *
 * @author Alexandre Renaux
 */
public class StandardRulesValidator implements RulesValidator {

    private final StandardRuleValidator ruleValidator = new StandardRuleValidator();

    public boolean isValid(Rules rules, ValidationErrors validationErrors){
        for (Rule rule : rules.getRule()) {
            if (!ruleValidator.isValid(rule, validationErrors)){
                validationErrors.add("Invalid rule "+rule.getId());
                return false;
            }
        }
        return true;
    }

}
