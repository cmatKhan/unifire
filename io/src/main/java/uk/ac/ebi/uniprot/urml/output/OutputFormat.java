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

package uk.ac.ebi.uniprot.urml.output;

import uk.ac.ebi.uniprot.urml.core.utils.SelectorEnum;

import java.util.Arrays;

/**
 * Enum of supported output formats
 *
 * @author Alexandre Renaux
 */
public enum OutputFormat implements SelectorEnum {

    ANNOTATION_TSV("TSV", "Tab-Separated Values"),
    ANNOTATION_XML("XML", "URML Fact XML");

    private final String code;
    private final String description;

    OutputFormat(String code, String description) {
        this.code = code;
        this.description = description;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public static OutputFormat fromCode(String code) {
        for (OutputFormat outputFormat : OutputFormat.values()) {
            if (outputFormat.getCode().equals(code)){
                return outputFormat;
            }
        }
        throw new IllegalArgumentException(String.format("Unsupported output format %s. Supported format are: %s", code,
                Arrays.toString(Arrays.stream(OutputFormat.values()).map(OutputFormat::getCode).toArray())));
    }
}
