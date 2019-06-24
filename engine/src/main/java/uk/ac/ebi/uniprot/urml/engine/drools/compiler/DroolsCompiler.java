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

package uk.ac.ebi.uniprot.urml.engine.drools.compiler;

import uk.ac.ebi.uniprot.urml.engine.drools.engine.DroolsRuleBase;

import java.io.InputStream;
import java.util.List;
import org.kie.api.KieBaseConfiguration;
import org.kie.api.KieServices;
import org.kie.api.builder.*;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.conf.EqualityBehaviorOption;
import org.kie.api.runtime.KieContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Compiles Drools rules into a {@link DroolsRuleBase}
 *
 * @author Alexandre Renaux
 */
class DroolsCompiler {

    private static final Logger logger = LoggerFactory.getLogger(DroolsCompiler.class);

    private final KieServices kieServices;
    private final KieFileSystem kfs;
    private ReleaseId releaseId;
    private String ruleBaseName;

    DroolsCompiler() {
        this.kieServices = KieServices.Factory.get();
        this.kfs = kieServices.newKieFileSystem();
    }

    void addRules(InputStream inputStream, String identifier){
        kfs.write(String.format("src/main/resources/%s.drl", identifier),
                kieServices.getResources().newInputStreamResource(inputStream));
    }

    DroolsRuleBase compile(String ruleBaseName){
        this.ruleBaseName = ruleBaseName;
        releaseId = compileAndCheckErrors();
        return getRuleBase();
    }

    private DroolsRuleBase getRuleBase() {
        KieContainer kc = kieServices.newKieContainer(releaseId);
        KieBaseConfiguration kieBaseConf = kieServices.newKieBaseConfiguration();
        return new DroolsRuleBase(kc.newKieBase(kieBaseConf));
    }

    private ReleaseId compileAndCheckErrors() {
        logger.info("Compiling rule base {}", ruleBaseName);

        KieModuleModel kieModuleModel = kieServices.newKieModuleModel();
        kieModuleModel.newKieBaseModel(ruleBaseName).setDefault(true)
                .setEqualsBehavior( EqualityBehaviorOption.EQUALITY);
        kfs.writeKModuleXML(kieModuleModel.toXML());

        KieBuilder kieBuilder = kieServices.newKieBuilder(kfs).buildAll();
        Results results = kieBuilder.getResults();

        List<Message> warnings = results.getMessages(Message.Level.WARNING);
        for (Message warning : warnings) {
            if (logger.isWarnEnabled()) {
                logger.warn(warning.getText());
            }
        }

        List<Message> errors = results.getMessages(Message.Level.ERROR);
        if (!errors.isEmpty()) {
            for (Message error : errors) {
                if (logger.isErrorEnabled()) {
                    logger.error(error.getText());
                }
            }
            throw new IllegalStateException(String.format("Unexpected error when compiling %s rule base.", ruleBaseName));
        }
        return kieServices.getRepository().getDefaultReleaseId();
    }

}
