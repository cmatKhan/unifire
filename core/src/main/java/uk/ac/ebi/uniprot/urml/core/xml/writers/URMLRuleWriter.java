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

package uk.ac.ebi.uniprot.urml.core.xml.writers;

import uk.ac.ebi.uniprot.urml.core.xml.schema.JAXBContextInitializationException;
import uk.ac.ebi.uniprot.urml.core.xml.schema.URMLConstants;
import uk.ac.ebi.uniprot.urml.core.xml.schema.mappers.RuleNamespaceMapper;

import com.sun.xml.txw2.output.IndentingXMLStreamWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uniprot.urml.rules.ObjectFactory;
import org.uniprot.urml.rules.Rule;
import org.uniprot.urml.rules.Rules;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * URML rule writer using JAXB.
 *
 * @author Alexandre Renaux
 */
public class URMLRuleWriter implements URMLWriter<Rules, Rule> {

    private final Logger logger = LoggerFactory.getLogger(URMLRuleWriter.class);

    private JAXBContext jc;

    public URMLRuleWriter() {
        try {
            this.jc = JAXBContext.newInstance(URMLConstants.URML_RULES_JAXB_CONTEXT);
        } catch (JAXBException e){
            throw new JAXBContextInitializationException("Cannot initialize the URML rule writer", e);
        }
    }

    @Override
    public void write(Rules rules, OutputStream out) throws JAXBException, XMLStreamException {
        XMLStreamWriter writerNonIndenting = XMLOutputFactory.newFactory().createXMLStreamWriter(out);
        XMLStreamWriter writer = new IndentingXMLStreamWriter(writerNonIndenting);
        Marshaller marshaller = initMarshaller();
        marshaller.marshal(rules, writer);
    }

    @Override
    public XMLStreamWriter initStream(OutputStream out, Rules rules) throws XMLStreamException {
        logger.info("Initializing rule stream marshaller");
        XMLStreamWriter writerNonIndenting = XMLOutputFactory.newFactory().createXMLStreamWriter(out);
        XMLStreamWriter writer = new IndentingXMLStreamWriter(writerNonIndenting);
        writeRoot(getRootDocument(rules), writer);
        return writer;
    }

    @Override
    public void marshallElement(Rule rule, XMLStreamWriter writer) throws JAXBException {
        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
        try {
            marshaller.marshal(rule, writer);
        } catch (JAXBException | RuntimeException e){
            throw new JAXBException("Cannot marshall rule: "+rule, e);
        }
    }

    private Document getRootDocument(Rules rules) {
        List<Rule> ruleList = null;
        if (rules == null){
            rules = new ObjectFactory().createRules();
        } else if (rules.isSetRule()){
            ruleList = rules.getRule();
            rules.unsetRule();
        }
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()){
            XMLStreamWriter tmpWriter = XMLOutputFactory.newFactory().createXMLStreamWriter(outputStream);
            tmpWriter = new IndentingXMLStreamWriter(tmpWriter);
            Marshaller marshaller = initMarshaller();
            marshaller.marshal(rules, tmpWriter);
            if (ruleList != null) {
                rules.setRule(ruleList);
            }
            try (InputStream newInput = new ByteArrayInputStream(outputStream.toByteArray())) {
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                dbFactory.setNamespaceAware(true);
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                return dBuilder.parse(newInput);
            }
        } catch (Exception e){
            throw new IllegalArgumentException("Conversion failed for document root", e);
        }
    }

    private void writeRoot(Document doc, XMLStreamWriter writer) throws XMLStreamException {
        writer.setDefaultNamespace(URMLConstants.URML_RULE_NAMESPACE);
        writer.writeStartDocument();
        writer.writeStartElement(URMLConstants.URML_RULE_NAMESPACE, doc.getDocumentElement().getNodeName());

        NamedNodeMap attributes = doc.getDocumentElement().getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Node attribute = attributes.item(i);
            if (attribute.getNamespaceURI() == null) {
                writer.writeAttribute(attribute.getNodeName(), attribute.getNodeValue());
            } else {
                writer.writeNamespace(attribute.getLocalName(), attribute.getNodeValue());
            }
        }
    }

    private Marshaller initMarshaller() throws JAXBException {
        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new RuleNamespaceMapper());
        return marshaller;
    }
}
