package uk.ac.ebi.uniprot.urml.core.xml.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uniprot.urml.facts.SignatureType;
import org.uniprot.urml.rules.*;

import java.util.List;
import java.util.Optional;

public class FunfamRulesFilter implements RulesFilter {

    private final Logger log = LoggerFactory.getLogger(FunfamRulesFilter.class);

    @Override
    public Rules filter(Rules rules) {
        log.info("filtering rules for {}", rules.getName());
        List<Rule> filteredRules = rules.getRule().stream()
                .map(this::filterFunfamConditions)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
        log.info("filtered rules from {} to {} rules", rules.getRule().size(), filteredRules.size());
        return
                Rules.copyOf(rules)
                        .withRule(filteredRules)
                        .build();
    }

    // returns Optional of empty rule if rule has only funfam conditions,
    // otherwise creates copy of the rule after removing all funfam conditions
    private Optional<Rule> filterFunfamConditions(Rule rule) {
        List<ConjunctiveConditionSet> filteredConditionsSet = rule.getConditions().getAND().stream()
                .filter(conditionSet -> !hasFunfamCondition(conditionSet))
                .toList();

        if (filteredConditionsSet.isEmpty()) {
            log.debug("rule {} has only funfam conditions, removing it", rule.getId());
            return Optional.empty();
        }

        if (filteredConditionsSet.equals(rule.getConditions().getAND())) {
            return Optional.of(rule);
        }

        DisjunctiveConditionSet filteredDisjunctiveConditionSet = DisjunctiveConditionSet.builder()
                .withAND(filteredConditionsSet)
                .build();
        return Optional.of(Rule.builder().copyOf(rule)
                .withConditions(filteredDisjunctiveConditionSet)
                .build());
    }

    private boolean hasFunfamCondition(ConjunctiveConditionSet conditionSet) {
        return conditionSet.getCondition().stream().anyMatch(this::isFunfamCondition);
    }

    private boolean isFunfamCondition(Condition condition) {
        return condition.getFilter().stream().anyMatch(this::isFunfamFilter);
    }

    private boolean isFunfamFilter(Filter filter) {
        return filter.getOn().equals("signature") &&
                filter.getField().stream().anyMatch(this::isFunfamField);
    }

    private boolean isFunfamField(Field field) {
        return field.getAttribute().equals("type") && field.getValue().equals(SignatureType.FUNFAM.value());
    }
}
