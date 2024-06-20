package uk.ac.ebi.uniprot.urml.engine.drools.compiler;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.of;

/**
 * @author Vishal Joshi
 */
class EscapeSequenceSanitizerTest {

    private EscapeSequenceSanitizer sanitizer = new EscapeSequenceSanitizer();

    @ParameterizedTest
    @MethodSource("differentDataValues")
    void testShouldRemoveEscapeCharacter(String beforeValue, String expectedValue, String expectedMessage) {

        //when
        String afterValue = sanitizer.sanitize(beforeValue);

        //then
        assertEquals(expectedValue, afterValue, expectedMessage);
    }

    private static Stream<Arguments> differentDataValues() {
        return Stream.of(
                of("target Thor\\d4EBP and the feedback control",
                "target Thor\\\\d4EBP and the feedback control", "single cases of \\d must be rectified"),
                of("target Thor\\d4EBP and the fe\\dedback control",
                        "target Thor\\\\d4EBP and the fe\\\\dedback control", "all cases of \\\\d must be rectified"),
                of("target Thord4EBP and the feedback control",
                        "target Thord4EBP and the feedback control", "no changes in string if no \\d"));
    }

}