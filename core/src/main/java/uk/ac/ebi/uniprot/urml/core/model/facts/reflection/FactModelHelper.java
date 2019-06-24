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

package uk.ac.ebi.uniprot.urml.core.model.facts.reflection;

import uk.ac.ebi.uniprot.urml.core.UniFireRuntimeException;
import uk.ac.ebi.uniprot.urml.core.xml.schema.URMLConstants;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;
import org.reflections.Reflections;
import org.uniprot.urml.facts.Fact;
import org.uniprot.urml.facts.ObjectFactory;

/**
 * Helper class giving information about {@link Fact} classes and their attributes.
 *
 * @author Alexandre Renaux
 */
public class FactModelHelper {

    private FactModelHelper() {
        throw new IllegalStateException("Utility Class FactModelHelper is not supposed to be instantiated");
    }

    private static Map<QName, FactModelClass> qNameToFactClass;
    private static Map<Class<?>, FactModelClass> classToFactClass;

    static {
        initFactModelReflection();
    }

    public static boolean isFactClass(QName qName) {
        return qNameToFactClass.containsKey(qName);
    }

    public static FactModelClass getFactClass(QName qName) {
        FactModelClass factModelClass = qNameToFactClass.get(qName);
        if (factModelClass != null){
            return factModelClass;
        } else {
            throw new FactModelReflectionException("Unexpected qualified name, not part of the fact model: "+ qName);
        }
    }

    public static FactModelClass getFactClass(Class<?> clazz){
        return classToFactClass.get(clazz);
    }

    public static QName getQName(FactModelClass factClass){
        return new QName(factClass.getNamespace(), factClass.getName(), URMLConstants.URML_FACT_NAMESPACE_PREFIX);
    }

    @SuppressWarnings("unchecked")
    public static QName getQName(FactModelAttribute attribute){
        if (attribute.isRelational()){
            Class<? extends Fact> attributeClass = (Class<? extends Fact>) attribute.getAttributeType();
            return getQName(getFactClass(attributeClass));
        } else {
            throw new IllegalArgumentException("Cannot get QName of non fact type attribute.");
        }
    }

    public static FactModelAttribute getFactAttribute(QName qName, String... nestedAttributes) {
        FactModelClass factClass = getFactClass(qName);
        if (nestedAttributes.length == 1){
            return factClass.getAttribute(nestedAttributes[0]);
        } else if (nestedAttributes.length > 1){
            Class<?> nestedFactClass = factClass.getJavaType();
            for (int i = 0; i < nestedAttributes.length - 1; i++) {
                nestedFactClass = getFactClass(nestedFactClass).getAttribute(nestedAttributes[i]).getAttributeType();
            }
            return getFactClass(nestedFactClass).getAttribute(nestedAttributes[nestedAttributes.length-1]);
        } else {
            throw new IllegalArgumentException("Attribute array cannot be empty.");
        }
    }

    public static QName getQName(Class<? extends Fact> factClass){
        String namespace = ObjectFactory.class.getPackage().getAnnotation(XmlSchema.class).namespace();
        return new QName(namespace, factClass.getSimpleName(), "fact");
    }

    private static void initFactModelReflection() {
        qNameToFactClass = new HashMap<>();
        classToFactClass = new HashMap<>();

        Reflections classReflections = new Reflections(URMLConstants.URML_FACT_MODEL_PKG);
        Set<Class<?>> concreteClasses = classReflections.getTypesAnnotatedWith(XmlType.class);
        if (concreteClasses.isEmpty()) {
            throw new UniFireRuntimeException("No concreteClasses found in that package");
        }

        for (Class<?> concreteFactClass : concreteClasses) {
            String namespace = concreteFactClass.getPackage().getAnnotation(XmlSchema.class).namespace();
            QName qName = new QName(namespace, concreteFactClass.getSimpleName(), URMLConstants.URML_FACT_NAMESPACE_PREFIX);
            Map<String, FactModelAttribute> factAttributes = new HashMap<>();
            List<Field> fieldList = getFields(concreteFactClass);
            fieldList.addAll(Arrays.asList(concreteFactClass.getSuperclass().getDeclaredFields()));
            for (Field field : fieldList) {
                field.setAccessible(true);
                if (!field.isSynthetic()) {
                    FactModelAttribute factAttribute;
                    if (field.getGenericType() instanceof Class) {
                        factAttribute = new FactModelAttribute(field.getType(), field.getName(), false);
                    } else {
                        ParameterizedType genericType = (ParameterizedType) field.getGenericType();
                        boolean isCollection = Collection.class.isAssignableFrom((Class) genericType.getRawType());
                        Type actualType = genericType.getActualTypeArguments()[0];
                        if (isCollection && actualType instanceof Class) {
                            factAttribute = new FactModelAttribute((Class) actualType, field.getName(), true);
                        } else {
                            throw new IllegalStateException("Attribute "+field.getName()+" from class " +
                                    concreteFactClass + " is neither a simple actualType nor a simple collection");
                        }
                    }
                    factAttributes.put(field.getName(), factAttribute);
                }
            }
            FactModelClass factClass = new FactModelClass(concreteFactClass, namespace, factAttributes);
            qNameToFactClass.put(qName, factClass);
            classToFactClass.put(concreteFactClass, factClass);
        }
    }

    private static List<Field> getFields(Class clazz) {
        List<Field> fields = new ArrayList<>();
        while (clazz != Object.class) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        return fields;
    }


}
