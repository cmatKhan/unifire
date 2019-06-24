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

package uk.ac.ebi.uniprot.urml.core.validation;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;

/**
 * Simple stack to store errors happening during format validations
 *
 * @author Alexandre Renaux
 */
public class ValidationErrorStack implements ValidationErrors {

    private Deque<String> errors;

    public ValidationErrorStack() {
        this.errors = new ArrayDeque<>();
    }

    @Override
    public void add(String error) {
        errors.push(error);
    }

    @Override
    public Collection<String> getErrors() {
        return errors;
    }

    @Override
    public String getLastError() {
        return errors.peek();
    }

    public String format() {
        StringBuilder stringBuilder = new StringBuilder();
        for (String error : errors) {
            stringBuilder.append("\t");
            stringBuilder.append(error);
            stringBuilder.append(";\n");
        }
        return stringBuilder.toString();
    }

    @Override
    public String toString() {
        return "ValidationErrorStack{" + "errors=" + errors + '}';
    }
}
