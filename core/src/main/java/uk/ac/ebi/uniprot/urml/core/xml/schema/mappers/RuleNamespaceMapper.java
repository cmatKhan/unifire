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

package uk.ac.ebi.uniprot.urml.core.xml.schema.mappers;

import uk.ac.ebi.uniprot.urml.core.xml.schema.URMLConstants;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

/**
 * Namespace prefix mapper for rule model
 *
 * @author Alexandre Renaux
 */
public class RuleNamespaceMapper extends NamespacePrefixMapper {

    @Override
    public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
        if(URMLConstants.URML_RULE_NAMESPACE.equals(namespaceUri)) {
            return "";
        } else if(URMLConstants.URML_FACT_NAMESPACE.equals(namespaceUri)) {
            return URMLConstants.URML_FACT_NAMESPACE_PREFIX;
        }
        return suggestion;
    }

    @Override
    public String[] getPreDeclaredNamespaceUris() {
        return new String[] { URMLConstants.URML_FACT_NAMESPACE , URMLConstants.URML_RULE_NAMESPACE};
    }

}
