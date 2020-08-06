package uk.ac.ebi.uniprot.urml.output.xml;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.junit.rules.TemporaryFolder;
import org.uniprot.urml.facts.*;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Created by Hermann Zellner on 05/08/2020.
 */
public class AnnotationXMLWriterTest {

    public TemporaryFolder folder= new TemporaryFolder();

    private final static String ACCESSION_1 = "P12345";
    private final static String ACCESSION_2 = "P12346";
    private final static String ANNOTATION_1 = "Some annotation value";
    private final static String ANNOTATION_2 = "Another annotation value";

    @Test
    public void writeOneProteinAnnotation() throws IOException {
        String expectedResult = "<?xml version=\"1.0\" ?>\n" +
                "<facts xmlns=\"http://uniprot.org/urml/facts\" xmlns:xsi=\"http://www.w3" +
                ".org/2001/XMLSchema-instance\">\n" +
                "  <fact xsi:type=\"ProteinAnnotation\">\n" +
                "    <protein>P12345</protein>\n" +
                "    <value>Some annotation value</value>\n" +
                "  </fact>\n" +
                "</facts>";


        String result;
        try(ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            try (AnnotationXMLWriter annotationXMLWriter = new AnnotationXMLWriter(outputStream)) {
                annotationXMLWriter.write(buildProteinAnnotation(ACCESSION_1, ANNOTATION_1));
            }
            result = outputStream.toString("UTF-8");
        }

        Diff diff = DiffBuilder.compare(expectedResult).withTest(result)
                .ignoreWhitespace()
                .checkForIdentical()
                .build();

        assertFalse(diff.hasDifferences());
    }

    @Test
    public void writeTwoProteinAnnotations() throws IOException {
        String expectedResult = "<?xml version=\"1.0\" ?>\n" +
                "<facts xmlns=\"http://uniprot.org/urml/facts\" xmlns:xsi=\"http://www.w3" +
                ".org/2001/XMLSchema-instance\">\n" +
                "  <fact xsi:type=\"ProteinAnnotation\">\n" +
                "    <protein>P12345</protein>\n" +
                "    <value>Some annotation value</value>\n" +
                "  </fact>\n" +
                "  <fact xsi:type=\"ProteinAnnotation\">\n" +
                "    <protein>P12346</protein>\n" +
                "    <value>Another annotation value</value>\n" +
                "  </fact>\n" +
                "</facts>";


        String result;
        try(ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            try (AnnotationXMLWriter annotationXMLWriter = new AnnotationXMLWriter(outputStream)) {
                annotationXMLWriter.write(buildProteinAnnotation(ACCESSION_1, ANNOTATION_1));
                annotationXMLWriter.write(buildProteinAnnotation(ACCESSION_2, ANNOTATION_2));
            }
            result = outputStream.toString("UTF-8");
        }



        Diff diff = DiffBuilder.compare(expectedResult).withTest(result)
                .ignoreWhitespace()
                .checkForIdentical()
                .build();

        assertFalse(diff.hasDifferences());
    }

    @Test
    public void writeFactSetWithOneProteinAnnotation() throws IOException {

        String expectedResult = "<?xml version=\"1.0\" ?>\n" +
                "<facts xmlns=\"http://uniprot.org/urml/facts\" xmlns:xsi=\"http://www.w3" +
                ".org/2001/XMLSchema-instance\">\n" +
                "  <fact xsi:type=\"ProteinAnnotation\">\n" +
                "    <protein>P12345</protein>\n" +
                "    <value>Some annotation value</value>\n" +
                "  </fact>\n" +
                "</facts>";

        FactSet factSet = FactSet.builder().withFact(
                buildProteinAnnotation(ACCESSION_1, ANNOTATION_1)
        ).build();

        String result;
        try(ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            try (AnnotationXMLWriter annotationXMLWriter = new AnnotationXMLWriter(outputStream)) {
                annotationXMLWriter.write(factSet);
            }
            result = outputStream.toString("UTF-8");
        }

        Diff diff = DiffBuilder.compare(expectedResult).withTest(result)
                .ignoreWhitespace()
                .checkForIdentical()
                .build();

        assertFalse(diff.hasDifferences());
    }

    @Test
    public void writeFactSetWithTwoProteinAnnotations() throws IOException {

        String expectedResult = "<?xml version=\"1.0\" ?>\n" +
                "<facts xmlns=\"http://uniprot.org/urml/facts\" xmlns:xsi=\"http://www.w3" +
                ".org/2001/XMLSchema-instance\">\n" +
                "  <fact xsi:type=\"ProteinAnnotation\">\n" +
                "    <protein>P12345</protein>\n" +
                "    <value>Some annotation value</value>\n" +
                "  </fact>\n" +
                "  <fact xsi:type=\"ProteinAnnotation\">\n" +
                "    <protein>P12346</protein>\n" +
                "    <value>Another annotation value</value>\n" +
                "  </fact>\n" +
                "</facts>";

        FactSet factSet = FactSet.builder().withFact(
                buildProteinAnnotation(ACCESSION_1, ANNOTATION_1),
                buildProteinAnnotation(ACCESSION_2, ANNOTATION_2)
        ).build();

        String result;
        try(ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            try (AnnotationXMLWriter annotationXMLWriter = new AnnotationXMLWriter(outputStream)) {
                annotationXMLWriter.write(factSet);
            }
            result = outputStream.toString("UTF-8");
        }

        Diff diff = DiffBuilder.compare(expectedResult).withTest(result)
                .ignoreWhitespace()
                .checkForIdentical()
                .build();

        assertFalse(diff.hasDifferences());
    }

    @Test
    public void writeTwoFactSets() throws IOException {

        String expectedResult = "<?xml version=\"1.0\" ?>\n" +
                "<facts xmlns=\"http://uniprot.org/urml/facts\" xmlns:xsi=\"http://www.w3" +
                ".org/2001/XMLSchema-instance\">\n" +
                "  <fact xsi:type=\"ProteinAnnotation\">\n" +
                "    <protein>P12345</protein>\n" +
                "    <value>Some annotation value</value>\n" +
                "  </fact>\n" +
                "  <fact xsi:type=\"ProteinAnnotation\">\n" +
                "    <protein>P12346</protein>\n" +
                "    <value>Another annotation value</value>\n" +
                "  </fact>\n" +
                "</facts>";

        FactSet factSet1 = FactSet.builder().withFact(
                buildProteinAnnotation(ACCESSION_1, ANNOTATION_1)
        ).build();
        FactSet factSet2 = FactSet.builder().withFact(
                buildProteinAnnotation(ACCESSION_2, ANNOTATION_2)
        ).build();

        String result;
        try(ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            try (AnnotationXMLWriter annotationXMLWriter = new AnnotationXMLWriter(outputStream)) {
                annotationXMLWriter.write(factSet1);
                annotationXMLWriter.write(factSet2);
            }
            result = outputStream.toString("UTF-8");
        }

        Diff diff = DiffBuilder.compare(expectedResult).withTest(result)
                .ignoreWhitespace()
                .checkForIdentical()
                .build();

        assertFalse(diff.hasDifferences());
    }


    private ProteinAnnotation buildProteinAnnotation(String accession, String annotationValue){
        Protein.Builder<Void> builder = Protein.builder()
                .withId(accession)
                .withOrganism(
                        Organism.builder()
                                .withScientificName("Homo sapiens")
                                .build())
                .withSequence(
                        ProteinSequence.builder()
                                .withValue("ACDE")
                                .build()
                );

        return ProteinAnnotation.builder()
                .withProtein(builder.build())
                .withValue(annotationValue)
                .build();
    }
}
