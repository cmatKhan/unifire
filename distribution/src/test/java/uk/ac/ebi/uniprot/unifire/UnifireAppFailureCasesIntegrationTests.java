/*
 * Copyright (c) 2018 European Molecular Biology Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package uk.ac.ebi.uniprot.unifire;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

/**
 * @author Vishal Joshi
 */
class UnifireAppFailureCasesIntegrationTests {

    private final PrintStream standardOut = System.out;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @ParameterizedTest
    @MethodSource("invalidOrIncompleteArguments")
    void testShouldVerifyThatIfRequiredArgumentsAreNotPassedTheAppFailsWithAnError(String inCompleteArguments,
                                                                                   String missingArgument) throws Exception {
        //given

        //when
        String[] argsArray = inCompleteArguments.split(" ");
        UniFireApp.main(argsArray);

        //then
        String expected = "Missing required option: " + missingArgument + " \n" +
                "--------------------------------------------\n" +
                "usage: unifire -i <INPUT_FILE> -o <OUTPUT_FILE> -r <RULE_URML_FILE> [-f <OUTPUT_FORMAT>] [-n\n" +
                "       <INPUT_CHUNK_SIZE>] [-s <INPUT_SOURCE>] [-t <TEMPLATE_FACTS>] [-m <MAX_MEMORY>] [-h]\n" +
                "--------------------------------------------\n" +
                "     -i,--input <INPUT_FILE>                Input file (path) containing the proteins to annotate\n" +
                "                                            and required data, in the format specified by the -s\n" +
                "                                            option.\n" +
                "     -o,--output <OUTPUT_FILE>              Output file (path) containing predictions in the format\n" +
                "                                            specified in the -f option.\n" +
                "     -r,--rules <RULE_URML_FILE>            Rule base file (path) provided by UniProt (e.g UniRule\n" +
                "                                            or ARBA) (format: URML).\n" +
                "     -f,--output-format <OUTPUT_FORMAT>     Output file format. Supported formats are:\n" +
                "                                            - TSV (Tab-Separated Values)\n" +
                "                                            - XML (URML Fact XML)\n" +
                "                                            (default: TSV).\n" +
                "     -n,--chunksize <INPUT_CHUNK_SIZE>      Chunk size (number of proteins) to be batch processed\n" +
                "                                            simultaneously\n" +
                "                                            (default: 1000).\n" +
                "     -s,--input-source <INPUT_SOURCE>       Input source type. Supported input sources are:\n" +
                "                                            - InterProScan (InterProScan Output XML)\n" +
                "                                            - UniParc (UniParc XML)\n" +
                "                                            - XML (Input Fact XML)\n" +
                "                                            (default: InterProScan).\n" +
                "     -t,--templates <TEMPLATE_FACTS>        UniRule template sequence matches, provided by UniProt\n" +
                "                                            (format: Fact Model XML).\n" +
                "     -m <MAX_MEMORY>                        Max size of the memory allocation pool in MB (JVM -Xmx)\n" +
                "                                            (default: 4096 MB).\n" +
                "     -h,--help                              Print this usage.\n" +
                "--------------------------------------------";
        assertThat(outputStreamCaptor.toString().trim(), containsString(expected));

    }

    static Stream<Arguments> invalidOrIncompleteArguments() {
        return Stream.of(
                Arguments.of("-i /fasta/file/path -r /rules/file/path", "o"),
                Arguments.of("-i /fasta/file/path -o /output/file/path", "r"),
                Arguments.of("-r /rules/file/path -o /output/file/path", "i")
        );
    }

    @AfterEach
    void tearDown() {
        System.setOut(standardOut);
    }
}
