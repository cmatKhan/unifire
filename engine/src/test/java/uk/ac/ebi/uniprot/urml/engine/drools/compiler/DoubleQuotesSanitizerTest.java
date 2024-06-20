package uk.ac.ebi.uniprot.urml.engine.drools.compiler;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.of;

/**
 * @author Vishal Joshi
 */
class DoubleQuotesSanitizerTest {
    private DoubleQuotesSanitizer sanitizer = new DoubleQuotesSanitizer();

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
                of("some value with double quotes \" should be sanitized",
                        "some value with double quotes \\\" should be sanitized",
                        "single double quotes should be escaped"),
                of("multiple \" double quotes should be escaped \" after sanitization",
                        "multiple \\\" double quotes should be escaped \\\" after sanitization",
                        "multiple double quotes should be escaped"),
                of("some value without double quotes",
                        "some value without double quotes",
                        "no change if no double quotes"));
    }
}