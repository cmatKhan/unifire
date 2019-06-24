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

package uk.ac.ebi.uniprot.urml.input.parsers.facts.xml;

import uk.ac.ebi.uniprot.urml.core.xml.readers.URMLFactReader;
import uk.ac.ebi.uniprot.urml.input.collections.ProteinFactSetAggregator;
import uk.ac.ebi.uniprot.urml.input.parsers.FactSetParser;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import javax.xml.bind.JAXBException;
import org.uniprot.urml.facts.FactSet;

/**
 * Parses facts XML (URML model) and aggregates proteins and their dependent data into {@link FactSet}s using
 * {@link ProteinFactSetAggregator}.
 *
 * @author Alexandre Renaux
 */
public class FactXmlParser implements FactSetParser {

    private final URMLFactReader factReader;
    private final ProteinFactSetAggregator proteinFactSetAggregator;

    public FactXmlParser() {
        this.factReader = new URMLFactReader();
        this.proteinFactSetAggregator = new ProteinFactSetAggregator();
    }

    @Override
    public Iterator<FactSet> parse(InputStream factInputStream) throws IOException {
        FactSet factSet;
        try {
            factSet = factReader.read(factInputStream);
        } catch (JAXBException e) {
            throw new IOException("Cannot parse the input source", e);
        }
        factSet.getFact().forEach(proteinFactSetAggregator::addFact);
        return proteinFactSetAggregator.getFactSets().iterator();
    }
}
