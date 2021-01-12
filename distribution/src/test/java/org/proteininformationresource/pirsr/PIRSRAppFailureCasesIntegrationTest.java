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

package org.proteininformationresource.pirsr;

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
class PIRSRAppFailureCasesIntegrationTest {

    private final PrintStream standardOut = System.out;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    @BeforeEach
    public void setUp() {
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @ParameterizedTest
    @MethodSource("invalidOrIncompleteArguments")
    void testShouldVerifyThatIfRequiredArgumentsAreNotPassedTheAppFailsWithAnError(String inCompleteArguments,
                                                                                   String missingArgument) throws Exception {
        //when
        String[] argsArray = inCompleteArguments.split(" ");
        PIRSRApp.main(argsArray);

        //then
        String expected = "Missing required option: "+missingArgument+" \n" +
                "usage: pirsr -a <HMMALIGN> -d <PIRSR_DATA_DIR> -i <INPUT_FILE> -o <OUTPUT_DIR> [-t <INPUT_TYPE>]\n" +
                "       [-h]\n" +
                "----------------------------------------------------------------------------------------------------\n" +
                "     -a,--hmmalign <HMMALIGN>                 Path to hmmalign command.\n" +
                "     -d,--pirsr_data_dir <PIRSR_DATA_DIR>     Directory for PIRSR data.\n" +
                "     -i,--input_file <INPUT_FILE>             Input file (path) containing the proteins to annotate\n" +
                "                                              and required data in InterProScan Output XML format.\n" +
                "     -o,--output_dir <OUTPUT_DIR>             Directory for SRHMM hmmalign result and enhanced\n" +
                "                                              IPRScan Facts XML file.\n" +
                "     -t,--input_type <INPUT_TYPE>             Type of the input file provided by -i option.\n" +
                "                                              Supported Input types are\n" +
                "                                              - InterProScan (InterProScan Output XML)\n" +
                "                                              - XML (Input Fact XML)\n" +
                "                                              (default: InterProScan)\n" +
                "     -h,--help                                Print this usage.\n" +
                "----------------------------------------------------------------------------------------------------";
        assertThat(outputStreamCaptor.toString().trim(), containsString(expected));
    }

    private static Stream<Arguments> invalidOrIncompleteArguments() {
        return Stream.of(
                Arguments.of("-i /fasta/file/path -d /pirsr/data/dir -a /hmmalign/path", "o"),
                Arguments.of("-i /fasta/file/path -d /pirsr/data/dir -o /output/dir", "a"),
                Arguments.of("-i /fasta/file/path -a /hmm/align -o /output/dir", "d"),
                Arguments.of("-d /pirsr/data/dir -a /hmm/align -o /output/dir", "i")
        );
    }

    @AfterEach
    public void tearDown() {
        System.setOut(standardOut);
    }

}