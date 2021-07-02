package uk.ac.ebi.uniprot.urml.input.parsers;

import java.util.Iterator;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import org.uniprot.urml.facts.FactSet;

public interface FactSetChunkParser {
    Iterator<FactSet> nextChunk() throws XMLStreamException, JAXBException;
    boolean hasNext() throws XMLStreamException;
}
