package uk.ac.ebi.uniprot.urml.core.xml.writers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.uniprot.urml.facts.FactSet;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.uniprot.urml.facts.ProteinAnnotation.builder;

/**
 * @author Vishal Joshi
 */
class URMLFactWriterTest {

    @TempDir
    File tempDir;

    @Test
    public void testShouldVerifyThatFactSuperSetIsWrittenSuccessfully() throws FileNotFoundException, JAXBException, XMLStreamException {

        //given
        File outputFile = new File(tempDir.getAbsolutePath()+"/test.xml");
        URMLFactWriter factWriter = new URMLFactWriter(new FileOutputStream(outputFile));
        FactSet factSet = FactSet.builder()
                .withFact(builder().withType("type1").build(), builder().withType("type2").build())
                .build();


        //when and then
        assertDoesNotThrow(() -> factWriter.write(factSet));

    }

}