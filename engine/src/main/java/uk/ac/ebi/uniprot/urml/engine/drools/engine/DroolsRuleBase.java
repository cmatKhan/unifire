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

import uk.ac.ebi.uniprot.urml.engine.drools.DroolsIOException;
import uk.ac.ebi.uniprot.urml.engine.drools.debug.DebugAgendaEventListenerImpl;
import uk.ac.ebi.uniprot.urml.engine.drools.debug.DebugRuleRuntimeEventListenerImpl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieModule;
import org.kie.api.io.Resource;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Initializes a Drools rule base, wrapping {@link KieBase}
 *
 * @author Alexandre Renaux
 */
public class DroolsRuleBase {

    private static final Logger logger = LoggerFactory.getLogger(DroolsRuleBase.class);

    private KieBase kbase;

    public DroolsRuleBase(KieBase kbase){
        this.kbase = kbase;
    }

    public DroolsRuleBase(File kbaseFile) throws IOException {
        initFromFile(kbaseFile);
    }

    private void initFromFile(File kbaseFile) throws IOException {
        KieServices kieServices = KieServices.Factory.get();
        Resource jarRes;

        try (FileInputStream fis = new FileInputStream(kbaseFile)) {
            byte[] fileContent = new byte[(int) kbaseFile.length()];
            if (fis.read(fileContent) > 0) {
                jarRes = kieServices.getResources().newByteArrayResource(fileContent);
            }
            else {
                throw new DroolsIOException("Error while reading in DroolsRuleBase");
            }
        } catch (IOException e){
            throw new DroolsIOException("Error while reading in DroolsRuleBase", e);
        }

        KieModule km = kieServices.getRepository().addKieModule( jarRes );

        KieContainer kc = kieServices.newKieContainer( km.getReleaseId() );
        logger.info("Initializing rule base...");
        kbase = kc.getKieBase();
        logger.info("Initializing rule base session...");
    }

    KieSession getNewSession(){
        return kbase.newKieSession();
    }

    /* Note: Affects performance */
    KieSession getNewDebugSession() {
        KieSession kieSession = getNewSession();
        kieSession.addEventListener(new DebugAgendaEventListenerImpl());
        kieSession.addEventListener(new DebugRuleRuntimeEventListenerImpl());
        return kieSession;
    }

}
