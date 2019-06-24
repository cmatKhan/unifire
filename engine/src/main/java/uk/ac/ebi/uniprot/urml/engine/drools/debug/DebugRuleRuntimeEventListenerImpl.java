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

package uk.ac.ebi.uniprot.urml.engine.drools.debug;

import java.io.PrintStream;
import org.kie.api.event.rule.DebugRuleRuntimeEventListener;
import org.kie.api.event.rule.ObjectDeletedEvent;
import org.kie.api.event.rule.ObjectInsertedEvent;
import org.kie.api.event.rule.ObjectUpdatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Alexandre Renaux
 *
 * Debugs RHS operations happening during the rule execution (inserted/deleted/updated facts)
 *
 */
public class DebugRuleRuntimeEventListenerImpl extends DebugRuleRuntimeEventListener {

    private final Logger logger = LoggerFactory.getLogger("WorkingMemory");

    public DebugRuleRuntimeEventListenerImpl() {
        super();
    }

    public DebugRuleRuntimeEventListenerImpl(PrintStream stream) {
        super(stream);
    }

    @Override
    public void objectDeleted(ObjectDeletedEvent event) {
        logger.debug("Delete (via {}): {}", event.getRule(), event.getOldObject());
    }

    @Override
    public void objectInserted(ObjectInsertedEvent event) {
        if (event.getRule() == null) {
            logger.debug("Insert: {}", event.getObject());
        } else {
            logger.debug("Insert (via {}): {}",event.getRule().getName(), event.getObject());
        }
    }

    @Override
    public void objectUpdated(ObjectUpdatedEvent event) {
        logger.debug("Update (via {}): {} - to -> {}", event.getRule().getName(), event.getOldObject(),
                event.getObject());
    }
}
