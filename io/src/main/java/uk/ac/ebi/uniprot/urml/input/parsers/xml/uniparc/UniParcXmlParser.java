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

package uk.ac.ebi.uniprot.urml.input.parsers.xml.uniparc;

import uk.ac.ebi.uniprot.urml.input.parsers.FactSetParser;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import javax.xml.bind.JAXBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uniprot.uniparc.Uniparc;
import org.uniprot.urml.facts.FactSet;

/**
 * Parses the UniParc XML and provides an iterator of {@link FactSet}
 *
 * @author Alexandre Renaux
 */
public class UniParcXmlParser implements FactSetParser {

    private final static Logger logger = LoggerFactory.getLogger(UniParcXmlParser.class);

    private final UniParcXmlUnmarshaller uniparcXmlUnmarshaller;

    public UniParcXmlParser() {
        this.uniparcXmlUnmarshaller = new UniParcXmlUnmarshaller();
    }

    public Iterator<FactSet> parse(InputStream inputStream) throws IOException {
        Uniparc uniparcEntries;
        try {
            uniparcEntries = uniparcXmlUnmarshaller.read(inputStream);
        } catch (JAXBException e) {
            throw new IOException("Cannot parse the input source", e);
        }

        return new UniParcXmlEntryConverter(uniparcEntries);
    }

}
