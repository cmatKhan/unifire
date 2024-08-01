/*
 * Copyright (c) 2018 European Molecular Biology Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package uk.ac.ebi.uniprot.urml.core.xml.writers;

import uk.ac.ebi.uniprot.urml.core.xml.schema.JAXBContextInitializationException;

import com.sun.xml.txw2.output.IndentingXMLStreamWriter;
import java.io.IOException;
import java.io.OutputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import static javax.xml.stream.XMLOutputFactory.newFactory;

/**
 * @author Vishal Joshi
 */
public abstract class AbstractURMLWriter<S, E> implements URMLWriter<S, E> {

    private final String namespace;
    private final OutputStream os;
    protected final XMLStreamWriter xmlStreamWriter;
    protected final JAXBContext jaxbContext;

    protected AbstractURMLWriter(OutputStream os, JAXBContext jaxbContext, String namespace) throws XMLStreamException {
        this.os = os;
        this.namespace = namespace;
        try {
            this.jaxbContext = jaxbContext;
        } catch (Exception e) {
            throw new JAXBContextInitializationException("Cannot initialize the URML fact writer", e);
        }
        this.xmlStreamWriter = new IndentingXMLStreamWriter(newFactory().createXMLStreamWriter(os));
    }

    @Override
    public void completeWrite() throws XMLStreamException {
        xmlStreamWriter.writeCharacters(String.format("%n"));
        xmlStreamWriter.writeEndDocument();
        xmlStreamWriter.flush();
    }

    @Override
    public void close() throws IOException, XMLStreamException {
        os.close();
        xmlStreamWriter.close();
    }


    protected void writeRoot(Document doc) throws XMLStreamException {
        xmlStreamWriter.setDefaultNamespace(namespace);
        xmlStreamWriter.writeStartDocument();
        xmlStreamWriter.writeStartElement(namespace, doc.getDocumentElement().getNodeName());

        NamedNodeMap attributes = doc.getDocumentElement().getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Node attribute = attributes.item(i);
            if (attribute.getNamespaceURI() == null) {
                xmlStreamWriter.writeAttribute(attribute.getNodeName(), attribute.getNodeValue());
            } else {
                xmlStreamWriter.writeNamespace(attribute.getLocalName(), attribute.getNodeValue());
            }
        }
    }

    protected abstract Marshaller initMarshaller() throws JAXBException;

    protected abstract Document getRootDocument(S objectToSerialise);

}
