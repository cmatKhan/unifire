/*
 *  Copyright (c) 2018 European Molecular Biology Laboratory
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package uk.ac.ebi.uniprot.urml.engine.drools.compiler;

import uk.ac.ebi.uniprot.urml.core.model.facts.reflection.FactModelHelper;
import uk.ac.ebi.uniprot.urml.core.model.facts.reflection.FactModelReflectionException;
import uk.ac.ebi.uniprot.urml.core.xml.schema.URMLConstants;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.namespace.QName;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uniprot.urml.rules.*;

/**
 * Transpiles URML formatted rules to the Drools Rule Language (DRL)
 *
 * @author Alexandre Renaux
 */
public class URMLToDroolsTranspiler {

    private static final Logger logger = LoggerFactory.getLogger(URMLToDroolsTranspiler.class);

    enum DroolsComparator {
        EQUALS("==", "!="),
        GREATER_THAN_OR_EQ(">=", "<"),
        LESS_THAN_OR_EQ("<=", ">"),
        MATCHES("matches", "not matches"),
        CONTAINS("contains", "not contains");

        protected String positive;
        protected String negative;

        DroolsComparator(String positive, String negative){
            this.positive = positive;
            this.negative = negative;
        }

        String getValue(boolean isNegative){
            return isNegative ? negative : positive;
        }
    }


    private final static String RULE_START = "rule";
    private final static String RULE_END = "end";
    private final static String OR = "or";
    private final static String LHS_KEYWORD = "when";
    private final static String RHS_KEYWORD = "then";
    private final static String QUOTE = "\"";
    private final static String INDENT_UNIT = "  ";
    private final static String NEW_LINE = "\n";
    private final static String UNIFICATION = ":=";

    private PrintWriter writer;

    public URMLToDroolsTranspiler(OutputStream outputStream) {
        this.writer = new PrintWriter(outputStream);
    }

    public void translate(Rules rules) {
        logger.debug("Transpiling URML to Drools...");
        writer.write("package " + rules.getName() + ";");
        newLines(2);
        writer.write("import " + URMLConstants.URML_FACT_MODEL_PKG + ".*;");
        newLines(1);
        writer.write("import java.util.List;");
        newLines(2);
        rules.getRule().forEach(this::translate);
        writer.close();
    }

    private void translate(Rule rule){
        writer.write(RULE_START + " ");
        writer.write(enquote(rule.getId()));
        if (rule.getExtends() != null) {
            writer.write(" " + "extends" + " ");
            writer.write(enquote(rule.getExtends().getId()));
        }
        newLines(1);
        writer.write("dialect \"mvel\"");
        newLines(1);
        writer.write("no-loop true");
        newLines(1);
        if (rule.getProcedural()){
            writer.write("salience -10");
            newLines(1);
        }
        writer.write(LHS_KEYWORD);
        newLines(1);
        translate(rule.getConditions());
        newLines(1);
        writer.write(RHS_KEYWORD);
        newLines(1);
        translate(rule.getActions());
        writer.write(RULE_END);
        newLines(2);
    }

    private void translate(DisjunctiveConditionSet disjunctiveConditionSet) {
        boolean firstIteration = true;
        for (ConjunctiveConditionSet conjunctiveConditionSet : disjunctiveConditionSet.getAND()) {
            if(!firstIteration){
                writer.write(NEW_LINE);
                indent(1);
                writer.write(OR);
                writer.write(NEW_LINE);
            }
            indent(1);
            open_parenthesis();
            writer.write(NEW_LINE);
            translate(conjunctiveConditionSet);
            indent(1);
            close_parenthesis();
            firstIteration = false;
        }
    }

    private void translate(ConjunctiveConditionSet conjunctiveConditionSet) {
        boolean firstIteration = true;
        for (Condition condition : conjunctiveConditionSet.getCondition()) {
            if (!firstIteration){
                indent(2);
                writer.write("and");
                newLines(1);
            }
            translate(condition);
            firstIteration = false;
        }
    }

    private void translate(Actions actions) {
        Set<String> actionFactIds = new HashSet<>();
        actions.getAction().forEach(a -> translate(a, actionFactIds));
    }

    private void translate(Action action, Set<String> actionFactIds){
        action.getFact().forEach(f -> translate(f, action, actionFactIds));
    }

    private String extractPackage(String uri){
        try {
            return new URI(uri).getHost();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private void translate(RuleFact ruleFact, Action action,  Set<String> actionFactIds){
        indent(2);

        switch (action.getType()){
            case DECLARE:
                writer.write(ruleFact.getType().getLocalPart());
                writer.write(enspace(ruleFact.getId()));
                writer.write("= ");
                if (ruleFact.getCall() == null) {
                    translateToBuildStatement(ruleFact, action.getWith(), actionFactIds);
                } else {
                    translate(ruleFact.getCall());
                }
                actionFactIds.add(ruleFact.getId());
                break;
            case CREATE:
                writer.write("insertLogical");
                open_parenthesis();
                if(ruleFact.getCall() == null) {
                    translateToBuildStatement(ruleFact, action.getWith(), actionFactIds);
                } else {
                    translate(ruleFact.getCall());
                }
                close_parenthesis();
                break;
            case UPDATE:
                writer.write("update");
                open_parenthesis();
                writer.write("$"+ruleFact.getId());
                close_parenthesis();
                newLines(1);
                indent(2);
                if (ruleFact.getCall() == null){
                    translateToSetStatements(ruleFact, actionFactIds);
                } else {
                    translate(ruleFact.getCall());
                }
                break;
            case REMOVE:
                writer.write("retract");
                open_parenthesis();
                writer.write("$"+ruleFact.getId());
                close_parenthesis();
                break;
            default:
                throw new IllegalStateException("Unhandled action type "+action.getType());
        }
        writer.write(";");
        newLines(1);
    }

    private void translateToSetStatements(RuleFact ruleFact, Set<String> actionFactIds){
        boolean isRHSVariable = actionFactIds.contains(ruleFact.getId());
        if (!isRHSVariable){
            writer.write("$");
        }
        for (Field field : ruleFact.getField()) {
            writer.write(ruleFact.getId());
            writer.write(".set");
            writer.write(capitalize_first_letter(field.getAttribute()));
            open_parenthesis();
            if (field.getIsReference()){
                translate("$"+field.getValue(), Object.class);
            } else {
                translate(field.getValue(), getJavaType(ruleFact.getType(), field.getAttribute()));
            }
            close_parenthesis();
            writer.write(";");
            newLines(1);
        }
    }

    private void translateToBuildStatement(RuleFact ruleFact, List<String> wiredFields, Set<String> actionFactIds) {
        writer.write(ruleFact.getType().getLocalPart());
        writer.write(".builder()");
        for (String wired : ruleFact.getWith()) {
            translate(wired, actionFactIds);
        }
        for (String wired : wiredFields){
            translate(wired, actionFactIds);
        }
        ruleFact.getField().forEach(f -> translate(f, ruleFact));
        writer.write(".build()");
    }

    private void translate(String wiredReference, Set<String> actionFactIds) {
        writer.write(".with");

        String attribute;
        String value;
        if (wiredReference.contains(":")){
            String[] attributeId = wiredReference.split(":");
            attribute = attributeId[0];
            value = attributeId[1];
        } else {
            attribute = wiredReference;
            value = wiredReference;
        }
        writer.write(capitalize_first_letter(attribute));
        open_parenthesis();
        boolean isRHSVariable = actionFactIds.contains(value);
        boolean isSimpleString = value.startsWith("'") && value.endsWith("'");
        if (isSimpleString){
            writer.write(value.replace("'", "\""));
        } else {
            if (!isRHSVariable){
                writer.write("$");
            }
            writer.write(value);
        }
        close_parenthesis();
    }

    private void translate(ProceduralAttachment call) {
        writer.write(extractPackage(call.getUri()));
        writer.write("."+call.getProcedure());
        open_parenthesis();
        boolean firstIteration = true;
        for (ProcedureArgument procedureArgument : call.getArguments().getArgument()){
            if (!firstIteration){
                writer.write(", ");
            }
            if (procedureArgument.getIsReference()){
                writer.write("$");
            }
            writer.write(procedureArgument.getValue());
            firstIteration = false;
        }
        close_parenthesis();
    }

    private void translate(Field field, RuleFact ruleFact) {
        writer.write(".with");
        writer.write(capitalize_first_letter(field.getAttribute()));
        open_parenthesis();
        if (field.getIsReference()){
            translate("$"+field.getValue(), Object.class);
        } else {
            translate(escapeQuotes(field.getValue()), getJavaType(ruleFact.getType(), field.getAttribute()));
        }
        close_parenthesis();
    }

    private String escapeQuotes(String string){
        return string.replace("\"", "\\\"");
    }

    private void translate(Condition condition) {
        indent(3);
        if (condition.getBind() != null){
            writer.write("$"+condition.getBind());
            writer.write(enspace(UNIFICATION));
        } else {
            if (condition.getExists()){
                writer.write("exists ");
            } else {
                writer.write("not ");
            }
        }
        if (condition.getCollect()){
            writer.write("List() from collect (");
        }
        writer.write(condition.getOn().getLocalPart());
        open_parenthesis();
        boolean firstIteration = true;
        for (String bindingEqualityToAttribute : condition.getWith()) {
            if (!firstIteration){
                writer.write(", ");
            }
            String attribute;
            String bindingId;
            if (bindingEqualityToAttribute.contains(":")){
                String[] attributeId = bindingEqualityToAttribute.split(":");
                attribute = attributeId[0];
                bindingId = attributeId[1];
            } else {
                attribute = bindingEqualityToAttribute;
                bindingId = bindingEqualityToAttribute;
            }
            writer.write(attribute);
            writer.write(enspace(DroolsComparator.EQUALS.positive));
            writer.write("$"+bindingId);
            firstIteration = false;
        }
        for (String bindingAttributeEqualityToSelf : condition.getOf()){
            if (!firstIteration){
                writer.write(", ");
            }
            String attribute;
            String bindingId;
            if (bindingAttributeEqualityToSelf.contains(":")){
                String[] attributeId = bindingAttributeEqualityToSelf.split(":");
                attribute = attributeId[0];
                bindingId = attributeId[1];
            } else {
                bindingId = bindingAttributeEqualityToSelf;
                attribute = condition.getOn().getLocalPart().toLowerCase();
            }
            writer.write("this");
            writer.write(enspace(DroolsComparator.EQUALS.positive));
            writer.write("$"+bindingId+"."+attribute);
            firstIteration = false;
        }
        for (Filter filter : condition.getFilter()) {
            if (!firstIteration){
                writer.write(", ");
            }
            translate(condition.getOn(), filter);
            firstIteration = false;
        }

        close_parenthesis();

        if (condition.getCollect()){
            close_parenthesis();
        }
        newLines(1);
    }

    private void translate(QName on, Filter filter) {
        if (filter.getContains() != null) {
            translate(on, filter.getContains(), filter, DroolsComparator.CONTAINS);
        } else if (filter.getIn() != null){
            translate(on, filter.getIn(), filter, DroolsComparator.EQUALS);
        } else if (filter.getRange() != null){
            translate(on, filter.getRange(), filter);
        } else if (!filter.getField().isEmpty()){
            translate(on, filter.getField(), filter);
        } else if (filter.getValue() != null){
            translate(on, filter.getValue(), filter);
        } else if (filter.getRef() != null){
            translate(on, "$"+filter.getRef(), filter);
        } else if (filter.getStartsWith() != null) {
            translate(on, filter.getStartsWith(), filter);
        } else if (filter.getMatches() != null) {
            translate(on, filter.getMatches(), filter);
        } else {
            writer.write(filter.getOn());
            writer.write(enspace(DroolsComparator.EQUALS.positive));
            writer.write(String.valueOf(!filter.getNegative()));
        }
    }

    private void translate(QName on, Filter constraint, String attribute,  DroolsComparator comparator , String value){
        String fullAttribute = constraint.getOn();
        if (attribute != null){
            fullAttribute = fullAttribute + "." + attribute;
        }
        translateNullSafeAttribute(fullAttribute);
        writer.write(enspace(comparator.getValue(constraint.getNegative())));
        translate(value, getJavaType(on, fullAttribute));
    }

    private void translateNullSafeAttribute(String fullAttribute) {
        StringBuilder attrBuilder = new StringBuilder();
        String[] splittedAttributes = fullAttribute.split("\\.");
        for (int i = 0; i < splittedAttributes.length - 1 ; i++) {
            attrBuilder.append(splittedAttributes[i]).append("!").append(".");
        }
        attrBuilder.append(splittedAttributes[splittedAttributes.length-1]);
        writer.write(attrBuilder.toString());
    }

    private void translate(QName on, Filter constraint, DroolsComparator comparator , String value){
        translate(on, constraint, null, comparator, value);
    }

    private void translate(QName on, StartsWith startsWith, Filter constraint) {
        translate(on, constraint, DroolsComparator.MATCHES, startsWith.getValue()+".*");
    }

    private void translate(QName on, Matches matches, Filter constraint){
        translate(on, constraint, DroolsComparator.MATCHES, matches.getValue());
    }

    private void translate(QName on, String value, Filter constraint) {
        translate(on, constraint, DroolsComparator.EQUALS, value);
    }

    private void translate(QName on, SimpleValue value, Filter constraint) {
        translate(on, constraint, DroolsComparator.EQUALS, value.getValue());
    }

    private void translate(QName on, List<Field> fields, Filter constraint){
        boolean firstIteration = true;
        for (Field field : fields) {
            if (!firstIteration){
                writer.write(", ");
            }
            String value = field.getIsReference() ? "$"+field.getValue() : field.getValue();
            translate(on, constraint, field.getAttribute(), DroolsComparator.EQUALS, value);
            firstIteration = false;
        }
    }

    private void translate(QName on, Range range, Filter constraint){
        if (range.isSetStart()) {
            translate(on, constraint, DroolsComparator.GREATER_THAN_OR_EQ, String.valueOf(range.getStart()));
        }
        if (range.isSetStart() && range.isSetEnd()) {
            writer.write(", ");
        }
        if (range.isSetEnd()) {
            translate(on, constraint, DroolsComparator.LESS_THAN_OR_EQ, String.valueOf(range.getEnd()));
        }
    }

    private void translate(QName on, MultiValue multiValue, Filter constraint, DroolsComparator comparator){
        open_parenthesis();
        boolean firstIteration = true;
        for (SimpleValue value : multiValue.getValue()) {
            if (!firstIteration){
                boolean orRelation = LogicalOperator.ANY.equals(multiValue.getOperator());
                writer.write(enspace(orRelation ? "||" : "&&"));
            }
            translate(on, constraint, comparator, value.getValue());
            firstIteration = false;
        }
        close_parenthesis();
    }

    private void translate(String value, Class<?> javaType){
        if (value.startsWith("$") || "null".equals(value)){
            writer.write(value);
        } else if (Enum.class.isAssignableFrom(javaType)){
           writer.write(javaType.getSimpleName()+".fromValue(\""+value+"\")");
        } else if (ClassUtils.isAssignable(javaType, Number.class) && StringUtils.isNumeric(value)) {
            writer.write(value);
        } else if (ClassUtils.isAssignable(javaType, Boolean.class)){
            writer.write(value);
        } else if (String.class.isAssignableFrom(javaType)){
            writer.write(enquote(value));
        } else {
            throw new IllegalArgumentException("Unsupported value "+value+ " of type "+javaType);
        }
    }

    private String enquote(String input){
        return wrap(input, QUOTE);
    }

    private String enspace(String input){
        return wrap(input, " ");
    }

    private String wrap(String input, String wrapping){
        return wrapping+input+wrapping;
    }

    private void indent(int times){
        writer.write(StringUtils.repeat(INDENT_UNIT, times));
    }

    private void newLines(int times){
        writer.write(StringUtils.repeat(NEW_LINE, times));
    }

    private void open_parenthesis(){
        writer.write('(');
    }

    private void close_parenthesis(){
        writer.write(')');
    }

    private String capitalize_first_letter(String input){
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    private Class<?> getJavaType(QName on, String fullAttribute) {
        try {
            return FactModelHelper.getFactAttribute(on, fullAttribute.split("\\.")).getAttributeType();
        } catch (FactModelReflectionException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
