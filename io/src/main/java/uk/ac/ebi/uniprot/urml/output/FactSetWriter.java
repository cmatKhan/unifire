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

package uk.ac.ebi.uniprot.urml.output;

import uk.ac.ebi.uniprot.urml.output.column.AnnotationTSVWriter;
import uk.ac.ebi.uniprot.urml.output.xml.AnnotationXMLWriter;

import java.io.Closeable;
import java.io.OutputStream;
import org.uniprot.urml.facts.Fact;
import org.uniprot.urml.facts.FactSet;

/**
 * Writes a specific type of fact {@link E}
 *
 * @author Alexandre Renaux
 */
public interface FactSetWriter<E extends Fact> extends Closeable {

    void write(FactSet factSet);

    void write(E fact);

    /**
     * Create the appropriate parser {@link FactSetWriter} for the given {@link OutputFormat}
     * @param outputStream output stream
     * @param outputFormat output format
     * @return writer for the specified output format
     */
    static FactSetWriter of(OutputStream outputStream, OutputFormat outputFormat){
        switch (outputFormat){
            case ANNOTATION_XML:
                return new AnnotationXMLWriter(outputStream);
            case ANNOTATION_TSV:
                return new AnnotationTSVWriter(outputStream);
            default:
                throw new IllegalArgumentException("Unsupported output format "+outputFormat);
        }
    }

}
