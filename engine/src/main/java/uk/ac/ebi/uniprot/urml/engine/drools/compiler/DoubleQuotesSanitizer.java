package uk.ac.ebi.uniprot.urml.engine.drools.compiler;

/**
 * @author Vishal Joshi
 */
public class DoubleQuotesSanitizer implements Sanitizer {

    @Override
    public String sanitize(String toSanitize) {
        return toSanitize.replace("\"", "\\\"");
    }

}
