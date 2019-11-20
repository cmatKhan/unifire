package uk.ac.ebi.uniprot.urml.core.xml.writers;

import com.sun.xml.txw2.output.IndentingXMLStreamWriter;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import uk.ac.ebi.uniprot.urml.core.xml.schema.JAXBContextInitializationException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.OutputStream;

import static javax.xml.stream.XMLOutputFactory.newFactory;

/**
 * @author Vishal Joshi
 */
public abstract class AbstractURMLWriter<S, E> implements URMLWriter<S, E> {

    private final String namespace;
    private final OutputStream os;
    protected final XMLStreamWriter xmlStreamWriter;
    protected final JAXBContext jaxbContext;

    public AbstractURMLWriter(OutputStream os, JAXBContext jaxbContext, String namespace) throws XMLStreamException {
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
    public void close() throws Exception {
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
