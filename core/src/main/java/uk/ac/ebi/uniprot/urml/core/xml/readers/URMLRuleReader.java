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

package uk.ac.ebi.uniprot.urml.core.xml.readers;

import uk.ac.ebi.uniprot.urml.core.xml.schema.JAXBContextInitializationException;
import uk.ac.ebi.uniprot.urml.core.xml.schema.URMLConstants;

import java.io.IOException;
import java.io.InputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uniprot.urml.rules.Rules;

/**
 * URML rule unmarshaller using JAXB.
 *
 * @author Alexandre Renaux
 */
public class URMLRuleReader implements URMLReader<Rules> {

    private final Logger logger = LoggerFactory.getLogger(URMLRuleReader.class);

    private Unmarshaller unmarshaller;

    public URMLRuleReader() {
        try {
            JAXBContext context = JAXBContext.newInstance(URMLConstants.URML_RULES_JAXB_CONTEXT);
            unmarshaller = context.createUnmarshaller();
        } catch (Exception e){
            throw new JAXBContextInitializationException("Cannot initialize the URML rule reader", e);
        }
    }

    @Override
    public Rules read(InputStream ruleInputStream) throws JAXBException, IOException {
        if (ruleInputStream == null){
            throw new IOException("Null input stream");
        }
        try {
            JAXBElement<?> jaxbElement = (JAXBElement<?>) unmarshaller.unmarshal(ruleInputStream);
            return ((Rules) jaxbElement.getValue());
        } finally {
            ruleInputStream.close();
        }
    }

}
