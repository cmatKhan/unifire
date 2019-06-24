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
import org.kie.api.event.rule.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Alexandre Renaux
 *
 * Debugs the rule engine agenda, showing the sequence of executed rules
 *
 */
public class DebugAgendaEventListenerImpl extends DebugAgendaEventListener {

    private final Logger logger = LoggerFactory.getLogger("Agenda");

    public DebugAgendaEventListenerImpl() {
        super();
    }

    public DebugAgendaEventListenerImpl(PrintStream stream) {
        super(stream);
    }

    @Override
    public void afterMatchFired(AfterMatchFiredEvent event) {
        logger.debug("After Match Fired");
    }

    @Override
    public void afterRuleFlowGroupActivated(RuleFlowGroupActivatedEvent event) {
        logger.debug("After Rule Flow Group Activated: {}", event.getRuleFlowGroup().getName());
    }

    @Override
    public void afterRuleFlowGroupDeactivated(RuleFlowGroupDeactivatedEvent event) {
        logger.debug("After Rule Flow Group Deactivated: {}", event.getRuleFlowGroup().getName());
    }

    @Override
    public void agendaGroupPopped(AgendaGroupPoppedEvent event) {
        logger.debug("Agenda Group popped: {}", event.getAgendaGroup().getName());
    }

    @Override
    public void agendaGroupPushed(AgendaGroupPushedEvent event) {
        logger.debug("Agenda Group pushed: {}", event.getAgendaGroup().getName());
    }

    @Override
    public void beforeMatchFired(BeforeMatchFiredEvent event) {
        logger.debug("Execute Rule: {}",event.getMatch().getRule());
    }

    @Override
    public void beforeRuleFlowGroupActivated(RuleFlowGroupActivatedEvent event) {
        logger.debug("RuleFlow Group activated: {}",event.getRuleFlowGroup().getName());
    }

    @Override
    public void beforeRuleFlowGroupDeactivated(RuleFlowGroupDeactivatedEvent event) {
        logger.debug("RuleFlow Group deactivated: {}",event.getRuleFlowGroup().getName());
    }

    @Override
    public void matchCancelled(MatchCancelledEvent event) {
        logger.debug("Rule execution cancelled: {}",event.getMatch().getRule());
    }

    @Override
    public void matchCreated(MatchCreatedEvent event) {
    }
}
