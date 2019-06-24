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

import org.uniprot.urml.rules.*;

/**
 * Provides basic validation for {@link Rule}
 *
 * @author Alexandre Renaux
 */
public class StandardRuleValidator implements RuleValidator {

    private final InformationValidator informationValidator = new StandardInformationValidator();
    private final ConditionValidator conditionValidator = new StandardConditionValidator();
    private final ActionValidator actionValidator = new StandardActionValidator();

    public boolean isValid(Rule rule, ValidationErrors validationErrors){
        InformationSet informationSet = rule.getMeta();
        if (informationSet != null) {
            for (Information information : informationSet.getInformation()) {
                if(!informationValidator.isValid(information, validationErrors)){
                    validationErrors.add(formatError("Invalid information for rule", rule));
                    return false;
                }
            }
        }
        DisjunctiveConditionSet conditions = rule.getConditions();
        if (conditions != null && !conditions.getAND().isEmpty()){
            for (ConjunctiveConditionSet conjunctiveConditionSet : conditions.getAND()) {
                if (conjunctiveConditionSet != null && !conjunctiveConditionSet.getCondition().isEmpty()){
                    for (Condition condition : conjunctiveConditionSet.getCondition()) {
                        if (!conditionValidator.isValid(condition, validationErrors)){
                            validationErrors.add(formatError("Invalid condition for rule", rule));
                            return false;
                        }
                    }
                } else {
                    validationErrors.add(formatError("Null or empty AND condition set", rule));
                    return false;
                }
            }
        } else {
            validationErrors.add(formatError("Null or empty OR condition set", rule));
            return false;
        }
        Actions actions = rule.getActions();
        for (Action action : actions.getAction()) {
            if(!actionValidator.isValid(action, validationErrors)){
                validationErrors.add(formatError("Invalid action for rule", rule));
                return false;
            }
        }
        return true;
    }

    private String formatError(String error, Rule rule){
        return String.format("%s for rule %s", error, rule.getId());
    }

}
