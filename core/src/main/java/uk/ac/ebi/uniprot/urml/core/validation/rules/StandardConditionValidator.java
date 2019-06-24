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

import java.util.List;
import javax.xml.namespace.QName;
import org.uniprot.urml.rules.Condition;
import org.uniprot.urml.rules.Field;
import org.uniprot.urml.rules.Filter;

/**
 * Provides basic validation for {@link Condition}
 * // TODO: at the moment, only validates the qnames / attributes against the model.
 * // TODO: relational (with/of) should be validated at the DisjunctiveConditionSet level, using a Map<bindingName, QName) or something.
 *
 * @author Alexandre Renaux
 */
public class StandardConditionValidator implements ConditionValidator {

    private QualifiedNameAttributeValidator qualifiedNameAttributeValidator = new StandardQualifiedNameAttributeValidator();

    public boolean isValid(Condition condition, ValidationErrors validationErrors){
        QName qName = condition.getOn();
        return hasValidFilter(qName, condition.getFilter(), validationErrors);
    }

    private boolean hasValidFilter(QName qName, List<Filter> filters, ValidationErrors validationErrors) {
        for (Filter filter : filters) {
            if (!qualifiedNameAttributeValidator.isValid(qName, filter.getOn(), validationErrors)){
                validationErrors.add("Invalid attribute "+ filter.getOn()+ " for QName="+qName);
                return false;
            }
            for (Field field : filter.getField()) {
                String attribute = filter.getOn() + "." + field.getAttribute();
                if (!qualifiedNameAttributeValidator.isValid(qName, attribute, validationErrors)){
                    validationErrors.add("Invalid attribute "+ attribute+ " for QName="+qName);
                    return false;
                }
            }
        }
        return true;
    }

}
