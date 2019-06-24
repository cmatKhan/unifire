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

package uk.ac.ebi.uniprot.urml.core.xml.schema;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchema;
import org.uniprot.urml.facts.Fact;
import org.uniprot.urml.rules.Rule;

/**
 * Constants for URML
 *
 * @author Alexandre Renaux
 */
public class URMLConstants {

    private URMLConstants() {
        throw new IllegalStateException("Utility class URMLConstants is not supposed to be instantiated.");
    }

    public static final String URML_RULE_MODEL_PKG = "org.uniprot.urml.rules";
    public static final String URML_FACT_MODEL_PKG = "org.uniprot.urml.facts";

    public static final String URML_RULES_JAXB_CONTEXT = URML_RULE_MODEL_PKG + ":" + URML_FACT_MODEL_PKG;
    public static final String URML_FACTS_JAXB_CONTEXT = URML_FACT_MODEL_PKG;

    public static final String URML_FACT_NAMESPACE_PREFIX = "fact";

    public static final String URML_RULE_NAMESPACE = getURMLRuleNamespace();
    public static final String URML_FACT_NAMESPACE = getURMLFactNamespace();

    public static final String URML_RULE_TAG = getRuleTagName();
    public static final String URML_FACT_TAG = getFactTagName();


    private static String getURMLRuleNamespace() {
        return org.uniprot.urml.rules.ObjectFactory.class.getPackage().getAnnotation(XmlSchema.class).namespace();
    }

    private static String getURMLFactNamespace() {
        return org.uniprot.urml.facts.ObjectFactory.class.getPackage().getAnnotation(XmlSchema.class).namespace();
    }

    private static String getRuleTagName(){
        return Rule.class.getAnnotation(XmlRootElement.class).name();
    }

    private static String getFactTagName(){
        return Fact.class.getAnnotation(XmlRootElement.class).name();
    }

}
