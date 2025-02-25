package uk.ac.ebi.uniprot.urml.core.xml.filter;

import org.uniprot.urml.rules.Rules;

public interface RulesFilter {

    Rules filter(Rules rules);
}
