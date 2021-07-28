package uk.ac.ebi.uniprot.urml.input.parsers.xml;

import uk.ac.ebi.uniprot.urml.input.parsers.FactSetChunkParser;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import org.uniprot.urml.facts.FactSet;

public abstract class AbstractXmlFactSetChunkParser<T> implements FactSetChunkParser {

    private final Integer chunkSize;
    private final PartialUnmarshaller<T> unmarshaller;

    public AbstractXmlFactSetChunkParser(InputStream inputStream, Integer chunkSize) throws IOException {
        Class<T> genericsType = (Class<T>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0];
        this.unmarshaller = new PartialUnmarshaller<>(inputStream, genericsType, 1);
        this.chunkSize = chunkSize;
    }

    @Override public Iterator<FactSet> nextChunk() throws XMLStreamException, JAXBException {
        Collection<T> xmlEntities = new HashSet<>();
        int processedProteins = 0;
        while(unmarshaller.hasNext() && processedProteins++ < chunkSize) {
            T xmlEntity = unmarshaller.next();
            xmlEntities.add(xmlEntity);
        }
        return convertToFactSet(xmlEntities);
    }

    protected abstract Iterator<FactSet> convertToFactSet(Collection<T> xmlEntities);

    @Override public boolean hasNext() throws XMLStreamException {
        return unmarshaller.hasNext();
    }

}
