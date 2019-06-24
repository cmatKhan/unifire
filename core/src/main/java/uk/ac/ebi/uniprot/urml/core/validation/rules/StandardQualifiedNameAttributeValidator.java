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

import uk.ac.ebi.uniprot.urml.core.model.facts.reflection.FactModelHelper;
import uk.ac.ebi.uniprot.urml.core.model.facts.reflection.FactModelReflectionException;
import uk.ac.ebi.uniprot.urml.core.validation.ValidationErrors;
import uk.ac.ebi.uniprot.urml.core.xml.schema.URMLConstants;

import com.google.common.base.Strings;
import javax.xml.namespace.QName;

/**
 * Provides basic validation for {@link QName} + attribute
 *
 * @author Alexandre Renaux
 */
public class StandardQualifiedNameAttributeValidator implements QualifiedNameAttributeValidator {

    @Override
    public boolean isValid(QName qName, String attribute, ValidationErrors validationErrors) {
        if (Strings.isNullOrEmpty(attribute)){
            validationErrors.add("Null or empty attribute");
            return false;
        }
        if(qName == null){
            validationErrors.add("Qualified name cannot be null");
            return false;
        } else if (!URMLConstants.URML_FACT_NAMESPACE.equals(qName.getNamespaceURI())){
            validationErrors.add("Qualified name namespace is "+qName.getNamespaceURI()+ " (expected: "
                                                                +URMLConstants.URML_FACT_NAMESPACE + ")");
            return false;
        }
        try {
            FactModelHelper.getFactAttribute(qName, attribute.split("\\."));
        } catch (FactModelReflectionException e) {
            validationErrors.add(e.getMessage());
            return false;
        }
        return true;
    }
}
