package uk.ac.ebi.uniprot.urml.engine.drools.compiler;

import org.junit.jupiter.api.Test;
import org.uniprot.urml.rules.Rules;
import uk.ac.ebi.uniprot.urml.core.xml.readers.URMLRuleReader;

import javax.xml.bind.JAXBException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * @author Vishal Joshi
 */
class URMLToDroolsCompilerTest {

    private URMLToDroolsCompiler compiler;
    @Test
    void shouldBeAbleToCompileRuleWithInvalidFormat() throws JAXBException, IOException {
        //given
        URMLRuleReader ruleReader = new URMLRuleReader();
        Rules rules = ruleReader.read(this.getClass().getResourceAsStream("/rules/ARBA00037344_urml.xml"));
        compiler = new URMLToDroolsCompiler();

        //when and then
        assertDoesNotThrow(() -> compiler.compile(rules));
    }
}