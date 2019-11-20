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

package uk.ac.ebi.uniprot.urml.core.xml.writers;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * URML writer based on {@link XMLStreamWriter} with possibility to write the whole superset {@link S} or element by
 * element {@link E}.
 *
 * @author Alexandre Renaux
 */
public interface URMLWriter<S, E> extends AutoCloseable {

    /**
     * Marshal a superset into an outputstream
     *
     * @param superSet The superset to marshal.
     */
    void write(S superSet) throws JAXBException, XMLStreamException;

    /**
     * Marshall an element {@link E} and stream it to the writer.
     * After writing all elements, this must be followed by {@link URMLWriter#completeWrite()}
     *
     * @param element an element {@link E} to be marshaled
     */
    void writeElementWise(E element) throws JAXBException;

    /**
     * Finalise completion of writing xml elements
     *
     */
    void completeWrite() throws XMLStreamException;

}
