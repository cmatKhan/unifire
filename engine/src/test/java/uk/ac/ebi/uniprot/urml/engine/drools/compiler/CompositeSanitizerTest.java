package uk.ac.ebi.uniprot.urml.engine.drools.compiler;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Vishal Joshi
 */
class CompositeSanitizerTest {

    @Test
    void testShouldVerifyIndividualSanitizersAreCalledInCorrectChronology() {
        //given
        DoubleQuotesSanitizer mockDoubleQuotesSanitizer = Mockito.mock(DoubleQuotesSanitizer.class);
        EscapeSequenceSanitizer mockEscapeSanitizer = Mockito.mock(EscapeSequenceSanitizer.class);
        CompositeSanitizer sanitizer = new CompositeSanitizer(List.of(mockEscapeSanitizer, mockDoubleQuotesSanitizer));
        String toSanitise = "some annot\\dation \"to\" sanitise";
        String doubleQuotesSanitised = "some annotdation \\\"to\\\" sanitise";
        String escapeSanitised = "some annotdation \"to\" sanitise";
        when(mockDoubleQuotesSanitizer.sanitize(escapeSanitised)).thenReturn(doubleQuotesSanitised);
        when(mockEscapeSanitizer.sanitize(toSanitise)).thenReturn(escapeSanitised);

        //when
        String sanitized = sanitizer.sanitize(toSanitise);

        //then
        assertEquals(doubleQuotesSanitised, sanitized);
        verify(mockDoubleQuotesSanitizer, times(1)).sanitize(escapeSanitised);
        verify(mockEscapeSanitizer, times(1)).sanitize(toSanitise);

    }

}