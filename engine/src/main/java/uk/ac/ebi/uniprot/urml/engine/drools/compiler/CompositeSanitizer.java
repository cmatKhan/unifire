package uk.ac.ebi.uniprot.urml.engine.drools.compiler;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Vishal Joshi
 */
public class CompositeSanitizer implements Sanitizer {

    private final List<Sanitizer> orderedSanitizers;

    /**
     * Instantiate the {@link CompositeSanitizer} object with
     * multiple sanitisers.
     *
     * @param orderedSanitizers the order of sanitisers in this list is strictly the order in which the sanitisers will
     *                          be applied i.e. first sanitiser in the {@link java.util.List} will be applied first.
     *                          A different order of orderedSanitizers will lead to different results for the same input.
     */
    public CompositeSanitizer(List<Sanitizer> orderedSanitizers) {
        this.orderedSanitizers = orderedSanitizers;
    }

    @Override
    public String sanitize(String toSanitize) {
        AtomicReference<String> partiallySanitised = new AtomicReference<>(toSanitize);
        orderedSanitizers.forEach(sanitizer -> partiallySanitised.set(sanitizer.sanitize(partiallySanitised.get())));
        return partiallySanitised.get();
    }
}
