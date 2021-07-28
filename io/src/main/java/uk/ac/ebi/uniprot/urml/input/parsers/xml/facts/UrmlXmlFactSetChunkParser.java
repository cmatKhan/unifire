package uk.ac.ebi.uniprot.urml.input.parsers.xml.facts;

import uk.ac.ebi.uniprot.urml.input.collections.ProteinFactSetAggregator;
import uk.ac.ebi.uniprot.urml.input.parsers.xml.AbstractXmlFactSetChunkParser;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import org.uniprot.urml.facts.Fact;
import org.uniprot.urml.facts.FactSet;

public class UrmlXmlFactSetChunkParser extends AbstractXmlFactSetChunkParser<Fact> {

    private static final Integer DEFAULT_CHUNKSIZE = 1000;

    public UrmlXmlFactSetChunkParser(InputStream inputStream) throws IOException {
        this(inputStream, DEFAULT_CHUNKSIZE);
    }

    public UrmlXmlFactSetChunkParser(InputStream inputStream, Integer chunkSize) throws IOException {
        super(inputStream, chunkSize);
    }

    @Override
    protected Iterator<FactSet> convertToFactSet(Collection<Fact> facts) {
        ProteinFactSetAggregator proteinFactSetAggregator = new ProteinFactSetAggregator();
        facts.forEach(proteinFactSetAggregator::addFact);
        return proteinFactSetAggregator.getFactSets().iterator();
    }
}
