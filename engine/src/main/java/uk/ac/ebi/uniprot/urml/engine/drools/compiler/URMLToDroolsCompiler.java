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

import uk.ac.ebi.uniprot.urml.core.validation.ValidationErrorStack;
import uk.ac.ebi.uniprot.urml.core.validation.ValidationErrors;
import uk.ac.ebi.uniprot.urml.core.validation.rules.RuleValidationException;
import uk.ac.ebi.uniprot.urml.core.validation.rules.RulesValidator;
import uk.ac.ebi.uniprot.urml.core.validation.rules.StandardRulesValidator;
import uk.ac.ebi.uniprot.urml.core.xml.readers.URMLRuleReader;
import uk.ac.ebi.uniprot.urml.engine.drools.engine.DroolsRuleBase;

import java.io.*;
import javax.xml.bind.JAXBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uniprot.urml.rules.Rules;

/**
 * Compiles an URML file or {@link Rules} to a Drools rule base (indexed/optimized rules).
 *
 * @author Alexandre Renaux
 */
public class URMLToDroolsCompiler {

    private static final Logger logger = LoggerFactory.getLogger(URMLToDroolsCompiler.class);

    private final DroolsCompiler droolsCompiler;

    public URMLToDroolsCompiler() {
        this.droolsCompiler = new DroolsCompiler();
    }

    /**
     * Validate, transpile into Drools Rule Language and compiles the rules
     * @param rules rules to compile
     * @return a drools rule base
     */
    public DroolsRuleBase compile(Rules rules) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        RulesValidator rulesValidator = new StandardRulesValidator();
        ValidationErrors validationErrors = new ValidationErrorStack();
        if (!rulesValidator.isValid(rules, validationErrors)){
            if (logger.isErrorEnabled()) {
                logger.error("Errors in {}_{}:\n{}", rules.getName(), rules.getVersion(), validationErrors.format());
                throw new RuleValidationException(
                        String.format("Error when validating %s_%s. %s", rules.getName(), rules.getVersion(),
                                validationErrors.getLastError()));
            }
        }

        URMLToDroolsTranspiler urmlToDroolsTranspiler = new URMLToDroolsTranspiler(baos);
        urmlToDroolsTranspiler.translate(rules);

        InputStream transpiledRules = new ByteArrayInputStream(baos.toByteArray());

        droolsCompiler.addRules(transpiledRules, rules.getName() + "-" + rules.getVersion());
        return droolsCompiler.compile(rules.getName()+"-"+rules.getVersion());
    }

    /**
     * Validate, transpile into Drools Rule Language and compiles the rules from one or multiple files
     * @param urmlInputFiles files in the URML format to compile into the same rule base
     * @return a drools rule base
     */
    public DroolsRuleBase compile(File... urmlInputFiles) throws IOException, JAXBException {
        URMLRuleReader urmlReader = new URMLRuleReader();

        Rules.Builder<Void> ruleSetBuilder = Rules.builder();

        for (File urmlInputFile : urmlInputFiles) {
            Rules rules = urmlReader.read(new FileInputStream(urmlInputFile));
            ruleSetBuilder.addRule(rules.getRule());
        }

        return compile(ruleSetBuilder.build());
    }

}
