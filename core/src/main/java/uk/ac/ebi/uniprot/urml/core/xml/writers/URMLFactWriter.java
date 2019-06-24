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
import uk.ac.ebi.uniprot.urml.core.xml.schema.mappers.FactNamespaceMapper;

import com.sun.xml.txw2.output.IndentingXMLStreamWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.apache.commons.lang3.ArrayUtils;
import org.uniprot.urml.facts.Fact;
import org.uniprot.urml.facts.FactSet;
import org.uniprot.urml.facts.ObjectFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * URML fact writer, marshalling {@link Fact} elements into the specified {@link OutputStream} using JAXB.
 *
 * @author Alexandre Renaux
 */
public class URMLFactWriter implements URMLWriter<FactSet, Fact> {

    private JAXBContext jc;
    private Marshaller marshaller;

    public URMLFactWriter() {
        try {
            this.jc = JAXBContext.newInstance(URMLConstants.URML_FACTS_JAXB_CONTEXT);
        } catch (Exception e){
            throw new JAXBContextInitializationException("Cannot initialize the URML fact writer", e);
        }
    }

    @Override
    public void write(FactSet factSet, OutputStream out) throws JAXBException, XMLStreamException {
        XMLStreamWriter writerNonIndenting = XMLOutputFactory.newFactory().createXMLStreamWriter(out);
        XMLStreamWriter writer = new IndentingXMLStreamWriter(writerNonIndenting);
        if (marshaller == null) {
            marshaller = initMarshaller();
        }
        marshaller.marshal(factSet, writer);
    }

    @Override
    public XMLStreamWriter initStream(OutputStream out, FactSet factSet) throws XMLStreamException {
        XMLStreamWriter writerNonIndenting = XMLOutputFactory.newFactory().createXMLStreamWriter(out);
        XMLStreamWriter writer = new IndentingXMLStreamWriter(writerNonIndenting);
        writeRoot(getRootDocument(factSet), writer);
        return writer;
    }

    @Override
    public void marshallElement(Fact fact, XMLStreamWriter writer) throws JAXBException {
        if (marshaller == null) {
            marshaller = jc.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
            marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new FactNamespaceMapper() {
                @Override
                public String[] getContextualNamespaceDecls() {
                    return ArrayUtils.addAll(super.getContextualNamespaceDecls(), XSI_PREFIX, XMLConstants
                            .W3C_XML_SCHEMA_INSTANCE_NS_URI);
                }
            });
        }

       QName qualifiedName = new QName(URMLConstants.URML_FACT_NAMESPACE, URMLConstants.URML_FACT_TAG);
       JAXBElement<Fact> jbe = new JAXBElement<>(qualifiedName, Fact.class, fact);
       try {
           marshaller.marshal(jbe, writer);
       } catch (JAXBException | RuntimeException e){
            throw new JAXBException("Cannot marshall fact: "+fact, e);
       }
    }

    private Document getRootDocument(FactSet factSet) {
        List<Fact> factList = null;
        if (factSet == null){
            factSet = new ObjectFactory().createFactSet();
        } else if (factSet.isSetFact()){
            factList = factSet.getFact();
            factSet.unsetFact();
        }
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()){
            XMLStreamWriter tmpWriter = XMLOutputFactory.newFactory().createXMLStreamWriter(outputStream);
            tmpWriter = new IndentingXMLStreamWriter(tmpWriter);
            marshaller = initMarshaller();
            marshaller.marshal(factSet, tmpWriter);
            if (factList != null) {
                factSet.setFact(factList);
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
        writer.setDefaultNamespace(URMLConstants.URML_FACT_NAMESPACE);
        writer.writeStartDocument();
        writer.writeStartElement(URMLConstants.URML_FACT_NAMESPACE, doc.getDocumentElement().getNodeName());

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
        Marshaller m = jc.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        m.setProperty("com.sun.xml.bind.namespacePrefixMapper", new FactNamespaceMapper());
        return m;
    }


}
