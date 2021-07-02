package uk.ac.ebi.uniprot.urml.input.parsers.xml.interpro;

import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.uniprot.urml.input.parsers.FactSetChunkParser;
import uk.ac.ebi.uniprot.urml.input.parsers.xml.PartialUnmarshaller;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import org.uniprot.urml.facts.FactSet;

public class InterProXmlProteinChunkParser implements FactSetChunkParser {

    private final Integer chunkSize;
    private final PartialUnmarshaller<Protein> unmarshaller;

    private static final Integer DEFAULT_CHUNKSIZE = 1000;

    public InterProXmlProteinChunkParser(InputStream inputStream) throws IOException {
        this(inputStream, DEFAULT_CHUNKSIZE);
    }

    public InterProXmlProteinChunkParser(InputStream inputStream, int chunkSize) throws IOException {
        this.unmarshaller = new PartialUnmarshaller<>(inputStream, Protein.class);
        this.chunkSize = chunkSize;
    }

    @Override public Iterator<FactSet> nextChunk() throws XMLStreamException, JAXBException {
        Set<Protein> proteins = new HashSet<>();
        int processedProteins = 0;
        while(unmarshaller.hasNext() && processedProteins < chunkSize) {
            proteins.add(unmarshaller.next());
        }

        return new InterProXmlProteinConverter(proteins);
    }

    @Override public boolean hasNext() throws XMLStreamException {
        return unmarshaller.hasNext();
    }
}
