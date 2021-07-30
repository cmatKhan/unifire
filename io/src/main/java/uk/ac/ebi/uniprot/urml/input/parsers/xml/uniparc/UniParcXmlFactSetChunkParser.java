package uk.ac.ebi.uniprot.urml.input.parsers.xml.uniparc;

import uk.ac.ebi.uniprot.urml.input.parsers.xml.AbstractXmlFactSetChunkParser;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import org.uniprot.uniparc.Entry;
import org.uniprot.urml.facts.FactSet;

public class UniParcXmlFactSetChunkParser extends AbstractXmlFactSetChunkParser<Entry> {

    private static final Integer DEFAULT_CHUNKSIZE = 1000;

    public UniParcXmlFactSetChunkParser(InputStream inputStream) throws IOException {
        this(inputStream, DEFAULT_CHUNKSIZE);
    }

    public UniParcXmlFactSetChunkParser(InputStream inputStream, Integer chunkSize) throws IOException {
        super(inputStream, chunkSize);
    }

    @Override
    protected Iterator<FactSet> convertToFactSet(Collection<Entry> xmlEntities) {
        return new UniParcXmlEntryConverter(xmlEntities);
    }
}
