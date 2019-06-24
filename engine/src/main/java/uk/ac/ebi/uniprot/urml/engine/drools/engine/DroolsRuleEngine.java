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

package uk.ac.ebi.uniprot.urml.engine.drools.engine;

import uk.ac.ebi.uniprot.urml.engine.common.RuleEngine;

import java.util.*;
import org.drools.core.common.DefaultFactHandle;
import org.kie.api.runtime.ClassObjectFilter;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.FactHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uniprot.urml.facts.Fact;

/**
 * Implementation of {@link RuleEngine} wrapping Drools {@link KieSession}.
 * @author Alexandre Renaux
 */
public class DroolsRuleEngine implements RuleEngine {

    private static final Logger logger = LoggerFactory.getLogger("DroolsRuleEngine");

    private final DroolsRuleBase droolsRuleBase;
    private KieSession kieSession;

    public DroolsRuleEngine(DroolsRuleBase droolsRuleBase){
        this.droolsRuleBase = droolsRuleBase;
    }

    @Override
    public void start(){
        this.kieSession = droolsRuleBase.getNewSession();
    }

    @Override
    public void insert(Fact fact) {
        kieSession.insert(fact);
    }

    @Override
    public void fireAllRules() {
        kieSession.fireAllRules();
    }

    @Override
    public void dispose() {
        if (kieSession != null) {
            kieSession.dispose();
        }
    }

    @Override
    public <E extends Fact> Iterator<E> query(Class<E> factClass) {
        Collection<FactHandle> factHandles = kieSession.getFactHandles(new ClassObjectFilter(factClass));
        return new FactIterator<>(factHandles.iterator());
    }

    protected class FactIterator<E> implements Iterator<E> {

        private Iterator<FactHandle> factHandleIterator;

        FactIterator(Iterator<FactHandle> factHandleIterator) {
            this.factHandleIterator = factHandleIterator;
        }

        @Override
        public boolean hasNext() {
            return factHandleIterator.hasNext();
        }

        @SuppressWarnings("unchecked")
        @Override
        public E next() {
            return (E) ((DefaultFactHandle) factHandleIterator.next()).getObject();
        }
    }

}
