package uk.ac.ebi.uniprot.urml.core.xml.filter;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.uniprot.urml.facts.SignatureType;
import org.uniprot.urml.rules.*;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class FunfamRulesFilterTest {

    final RulesFilter rulesFilter = new FunfamRulesFilter();

    @Test
    void filter() {
        //create Rules with combination of funfam only , non-funfam, and mixed conditions rules
        Rule nonFunfamRule = createTestRule("1",
                List.of(
                        List.of(Pair.of(SignatureType.INTER_PRO, "IPR0001"),
                                Pair.of(SignatureType.INTER_PRO, "IPR0002")),
                        List.of(Pair.of(SignatureType.INTER_PRO, "IPR0003"))
                ));

        Rule funfamRule1 = createTestRule("2",
                List.of(
                        List.of(Pair.of(SignatureType.FUNFAM, "3.30.1490.330:FF:000001"),
                                Pair.of(SignatureType.FUNFAM, "3.30.1490.330:FF:000002")),
                        List.of(Pair.of(SignatureType.FUNFAM, "3.30.1490.330:FF:000003"))
                ));

        Rule funfamRule2 = createTestRule("3",
                List.of(
                        List.of(Pair.of(SignatureType.FUNFAM, "3.30.1490.330:FF:000004"))
                ));

        Rule mixedRule = createTestRule("4",
                List.of(
                        List.of(Pair.of(SignatureType.FUNFAM, "3.30.1490.330:FF:000001"),
                                Pair.of(SignatureType.FUNFAM, "3.30.1490.330:FF:000002")),
                        List.of(Pair.of(SignatureType.INTER_PRO, "IPR0004")),
                        List.of(Pair.of(SignatureType.INTER_PRO, "IPR0005")),
                        List.of(Pair.of(SignatureType.FUNFAM, "3.30.1490.330:FF:000004"))
                ));

        Rules rules = createTestRules(nonFunfamRule, funfamRule1, funfamRule2, mixedRule);
        Rules filteredRules = rulesFilter.filter(rules);

        Rule expectedMixedRule = createTestRule("4",
                List.of(
                        List.of(Pair.of(SignatureType.INTER_PRO, "IPR0004")),
                        List.of(Pair.of(SignatureType.INTER_PRO, "IPR0005"))
                ));

        expectedMixedRule = Rule.copyOf(mixedRule)
                .withConditions(expectedMixedRule.getConditions())
                .build();

        Rules expectedRules = Rules.copyOf(rules)
                .withRule(nonFunfamRule, expectedMixedRule)
                .build();
        assertThat(filteredRules, equalTo(expectedRules));
    }

    private Rules createTestRules(Rule... rules) {
        return Rules.builder()
                .withName("org.uniprot.arba")
                .withVersion("2025_02")
                .withRule(rules)
                .build();
    }

    private Rule createTestRule(String id, List<List<Pair<SignatureType, String>>> signaturesConditionSet) {
        List<ConjunctiveConditionSet> conditionSets = new ArrayList<>();
        for (List<Pair<SignatureType, String>> signatures : signaturesConditionSet) {
            conditionSets.add(conditionSet(signatures));
        }
        return Rule.builder()
                .withId(id)
                .withMeta(meta(id))
                .withConditions()
                    .addAND(conditionSets)
                    .end()
                .withActions(actions(id))
                .build();
    }

    private ConjunctiveConditionSet conditionSet(List<Pair<SignatureType, String>> signatures) {
        ConjunctiveConditionSet.Builder<Void> conjunctiveConditionSetBuilder = ConjunctiveConditionSet.builder();
        for (Pair<SignatureType, String> signature : signatures) {
            conjunctiveConditionSetBuilder.addCondition(condition(signature));
        }
        return conjunctiveConditionSetBuilder.build();
    }

    private Condition condition(Pair<SignatureType, String> signature) {
        return Condition.builder()
                .addFilter(Filter.builder()
                        .withOn("signature")
                        .addField(Field.builder().withAttribute("type").withValue(signature.getLeft().value()).build())
                        .addField(Field.builder().withAttribute("value").withValue(signature.getRight()).build())
                        .build())
                .build();
    }

    private Actions actions(String id) {
        return Actions.builder()
                .addAction(Action.builder().withType(ActionType.CREATE).withWith(id).build())
                .build();
    }

    private InformationSet meta(String id) {
        return InformationSet.builder()
                .withInformation(Information.builder().withType("CREATOR").withValue(id).build())
                .build();
    }
}