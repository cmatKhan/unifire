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

package uk.ac.ebi.uniprot.urml.output.column;

import uk.ac.ebi.uniprot.urml.output.FactSetWriter;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Comparator;
import java.util.List;
import org.uniprot.urml.facts.Fact;
import org.uniprot.urml.facts.FactSet;
import org.uniprot.urml.facts.ProteinAnnotation;

import static java.util.Comparator.naturalOrder;

/**
 * Writes {@link ProteinAnnotation} from a {@link FactSet} into a TSV (tab-separated value) format.
 *
 * @author Alexandre Renaux
 */
public class AnnotationTSVWriter implements FactSetWriter<ProteinAnnotation> {

    private static final char SEP = '\t';
    private static final String NEWLINE = System.getProperty("line.separator");

    private final PrintWriter printWriter;
    private final Comparator<ProteinAnnotation> annotationComparator;

    public AnnotationTSVWriter(OutputStream outputStream) {
        this.printWriter = new PrintWriter(outputStream);
        this.annotationComparator = initAnnotationComparator();
        writeHeader(printWriter);
    }

    @Override
    public void write(FactSet factSet) {
        List<Fact> factList = factSet.getFact();
        factList.stream().map(f -> (ProteinAnnotation) f).sorted(annotationComparator).forEach(this::write);
    }

    @Override
    public void write(ProteinAnnotation proteinAnnotation) {
        printWriter.append(proteinAnnotation.getEvidence());
        printWriter.append(SEP);
        printWriter.append(proteinAnnotation.getProtein().getId());
        printWriter.append(SEP);
        printWriter.append(proteinAnnotation.getType());
        printWriter.append(SEP);
        if (proteinAnnotation.getValue() != null) {
            printWriter.append(proteinAnnotation.getValue());
        }
        printWriter.append(SEP);
        if (proteinAnnotation.getPositionStart() != null) {
            printWriter.append(String.valueOf(proteinAnnotation.getPositionStart()));
        }
        printWriter.append(SEP);
        if (proteinAnnotation.getPositionEnd() != null) {
            printWriter.append(String.valueOf(proteinAnnotation.getPositionEnd()));
        }
        printWriter.append(NEWLINE);
    }

    @Override
    public void close() {
        printWriter.close();
    }

    private static void writeHeader(PrintWriter printWriter){
        printWriter.append("Evidence\tProteinId\tAnnotationType\tValue\tStart\tEnd\n");
    }

    private static Comparator<ProteinAnnotation> initAnnotationComparator(){
        return Comparator.comparing((ProteinAnnotation p) -> p.getProtein().getId())
                  .thenComparing(ProteinAnnotation::getPositionStart, Comparator.nullsFirst(naturalOrder()))
                  .thenComparing(ProteinAnnotation::getPositionEnd, Comparator.nullsFirst(naturalOrder()))
                  .thenComparing(ProteinAnnotation::getType)
                  .thenComparing(ProteinAnnotation::getValue, Comparator.nullsFirst(naturalOrder()));
    }
}
