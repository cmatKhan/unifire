package uk.ac.ebi.uniprot.urml.engine.drools.compiler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.uniprot.urml.rules.Rules;
import uk.ac.ebi.uniprot.urml.core.xml.readers.URMLRuleReader;

import javax.xml.bind.JAXBException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;

import static org.mockito.Mockito.*;

/**
 * @author Vishal Joshi
 */
class URMLToDroolsTranspilerTest {

    @TempDir
    Path outputDirectory;
    private URMLToDroolsTranspiler transpiler;
    @Test
    void shouldBeAbleToCorrectRuleWithEscapeSequence() throws JAXBException, IOException {
        //given
        Path transpiled_rules = outputDirectory.resolve("transpiled_rules.drl");
        OutputStream outputStream = new FileOutputStream(transpiled_rules.toFile());
        URMLRuleReader ruleReader = new URMLRuleReader();
        Rules rules = ruleReader.read(this.getClass().getResourceAsStream("/rules/ARBA00037344_urml.xml"));
        CompositeSanitizer mockedSanitizer = mock(CompositeSanitizer.class);
        when(mockedSanitizer.sanitize("comment.function")).thenReturn("comment.function");
        when(mockedSanitizer.sanitize("Consistently activates both the downstream target Thor\\d4EBP and" +
                " the feedback control target InR"))
                .thenReturn("Consistently activates both the downstream target Thord4EBP" +
                        " and the feedback control target InR");

        transpiler = new URMLToDroolsTranspiler(outputStream, mockedSanitizer);

        //when
        transpiler.translate(rules);

        //then
        verify(mockedSanitizer, times(2)).sanitize(anyString());
    }
}