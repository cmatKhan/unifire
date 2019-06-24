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
import java.io.OutputStream;

/**
 * URML writer based on {@link XMLStreamWriter} with possibility to write the whole superset {@link S} or element by
 * element {@link E}.
 *
 * @author Alexandre Renaux
 */
public interface URMLWriter<S, E> {

    /**
     * Marshal a superset into an outputstream
     * @param superSet The superset to marshal.
     * @param out the outputstream to write to
     */
    void write(S superSet, OutputStream out) throws JAXBException, XMLStreamException;

    /**
     * Initialize an {@link XMLStreamWriter} linked to the given {@link OutputStream}.
     * Combined with {@link #marshallElement(Object, XMLStreamWriter)} and {@link #write(XMLStreamWriter)}, the
     * resulting XML can be written element by element.
     * @param out output stream
     * @param superSet a superSet, which can contain no elements {@link E}.
     * @return an xml stream writer
     */
    XMLStreamWriter initStream(OutputStream out, S superSet) throws XMLStreamException;

    /**
     * Marshall an element {@link E} and stream it to the writer
     * @see #initStream(OutputStream, Object)
     * @param element an element {@link E} to be marshaled
     * @param writer the writer from {@link #initStream(OutputStream, S)} method
     */
    void marshallElement(E element, XMLStreamWriter writer) throws JAXBException;

    /**
     * Finalize the marshalling process by writing all the elements marshaled through
     * @see #initStream(OutputStream, Object)
     * {@link #marshallElement(Object, XMLStreamWriter)}
     * @param writer the writer from {@link #initStream(OutputStream, S)} method
     */
    default void write(XMLStreamWriter writer) throws XMLStreamException {
        writer.writeCharacters(String.format("%n"));
        writer.writeEndDocument();
        writer.flush();
        writer.close();
    }

}
