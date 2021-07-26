/*
 *  Copyright (c) 2021 European Molecular Biology Laboratory
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
        this.unmarshaller = new PartialUnmarshaller<>(inputStream, Protein.class, 1);
        this.chunkSize = chunkSize;
    }

    @Override public Iterator<FactSet> nextChunk() throws XMLStreamException, JAXBException {
        Set<Protein> proteins = new HashSet<>();
        int processedProteins = 0;
        while(unmarshaller.hasNext() && processedProteins++ < chunkSize) {
            Protein protein = unmarshaller.next();
            proteins.add(protein);
        }

        return new InterProXmlProteinConverter(proteins);
    }

    @Override public boolean hasNext() throws XMLStreamException {
        return unmarshaller.hasNext();
    }
}
