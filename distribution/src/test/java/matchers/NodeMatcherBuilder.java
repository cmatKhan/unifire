/*
 * Copyright (c) 2018 European Molecular Biology Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package matchers;

import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.ElementSelector;
import org.xmlunit.diff.ElementSelectors;
import org.xmlunit.diff.NodeMatcher;

import java.util.Collections;

/**
 * @author Vishal Joshi
 */
public class NodeMatcherBuilder {

    public static NodeMatcher factXMLNodeMatcher() {
        ElementSelector proteinSelector = buildSelector("protein");
        ElementSelector signatureSelector = buildSelector("signature");
        ElementSelector frequencySelector = buildSelector("frequency");
        ElementSelector posStartSelector = buildSelector("positionStart");
        ElementSelector posEndSelector = buildSelector("positionEnd");
        ElementSelector alignmentSelector = buildSelector("alignment");
        ElementSelector sequenceSelector = buildSelector("sequence");
        ElementSelector organismSelector = buildSelector("organism");
        ElementSelector scientificNameSelector = buildSelector("scientificName");
        ElementSelector lineageSelector = buildSelector("lineage");

        return new DefaultNodeMatcher(ElementSelectors.and(proteinSelector, signatureSelector, frequencySelector, posStartSelector, posEndSelector, alignmentSelector, sequenceSelector, organismSelector, scientificNameSelector, lineageSelector), ElementSelectors.byNameAndText);
    }

    public static NodeMatcher unifireXMLNodeMatcher() {
        ElementSelector proteinSelector = buildSelector("protein");
        ElementSelector evidenceSelector = buildSelector("evidence");
        ElementSelector typeSelector = buildSelector("type");
        ElementSelector valueSelector = buildSelector("value");
        ElementSelector posStartSelector = buildSelector("positionStart");
        ElementSelector posEndSelector = buildSelector("positionEnd");

        return new DefaultNodeMatcher(ElementSelectors.and(proteinSelector, evidenceSelector, typeSelector, valueSelector, posStartSelector, posEndSelector), ElementSelectors.byNameAndText);
    }

    private static ElementSelector buildSelector(String xmlElement) {
        return ElementSelectors.conditionalBuilder().whenElementIsNamed("fact").thenUse(ElementSelectors.byXPath("./x:" + xmlElement, Collections.singletonMap("x", "http://uniprot.org/urml/facts"), ElementSelectors.byNameAndText)).build();
    }

}
