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

package uk.ac.ebi.uniprot.urml.engine.common;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uniprot.urml.facts.ProteinAnnotation;

/**
 * Implementation of {@link FactRetriever} for {@link ProteinAnnotation}.
 * @author Alexandre Renaux
 */
public class ProteinAnnotationRetriever implements FactRetriever<ProteinAnnotation> {

    private static final Logger logger = LoggerFactory.getLogger(ProteinAnnotationRetriever.class);

    private final RuleEngine ruleEngine;

    public ProteinAnnotationRetriever(RuleEngine ruleEngine) {
        this.ruleEngine = ruleEngine;
    }

    @Override
    public Iterator<ProteinAnnotation> retrieve(){
        return ruleEngine.query(ProteinAnnotation.class);
    }

    @Override
    public List<ProteinAnnotation> retrieveAll(){
        Iterator<ProteinAnnotation> iterator = retrieve();
        List<ProteinAnnotation> elementList = new ArrayList<>();
        iterator.forEachRemaining(elementList::add);
        return elementList;
    }
}
