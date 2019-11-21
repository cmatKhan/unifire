package org.proteininformationresource.pirsr;

import static com.google.common.primitives.Booleans.trueFirst;
import static java.lang.System.exit;

import java.io.File;
import java.util.Comparator;
import java.util.function.Function;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.drools.core.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import uk.ac.ebi.uniprot.urml.input.InputType;

/**
 * Entry point for the PIRSR application
 * 
 * @author Chuming Chen
 *
 */
public class PIRSRApp {

	private static final Logger logger = LoggerFactory.getLogger(PIRSRApp.class);
	private static final InputType DEFAULT_INPUT_TYPE = InputType.INTERPROSCAN_XML;
	private static final String USAGE_SEPARATOR = StringUtils.repeat("-", 100);

	public static void main(String[] args) throws Exception {
		Options options = new Options();
		Option inputFileOption = Option.builder("i").longOpt("input_file").hasArg().argName("INPUT_FILE")
				.desc("Input file (path) containing the proteins to annotate and required data in " + DEFAULT_INPUT_TYPE.getDescription() + " format.")
				.type(File.class).required().build();
		Option pirsrDataDirOption = Option.builder("d").longOpt("pirsr_data_dir").hasArg().argName("PIRSR_DATA_DIR").desc("Directory for PIRSR data.")
				.type(File.class).required().build();
		Option outputDirOption = Option.builder("o").longOpt("output_dir").hasArg().argName("OUTPUT_DIR")
				.desc("Directory for SRHMM hmmalign result and enhanced IPRScan Facts XML file.").type(File.class).required().build();
		Option hmmalignOption = Option.builder("a").longOpt("hmmalign").hasArg().argName("HMMALIGN").desc("Path to hmmalign command.").type(File.class)
				.required().build();
		Option helpOption = Option.builder("h").longOpt("help").desc("Print this usage.").build();

		options.addOption(pirsrDataDirOption);
		options.addOption(inputFileOption);
		options.addOption(hmmalignOption);
		options.addOption(outputDirOption);
		options.addOption(helpOption);

		if (hasHelp(helpOption, args)) {
			displayUsage(options);
			exit(0);
		}

		PIRSRRunner pirsrRunner = null;
		try {
			CommandLineParser parser = new DefaultParser();
			CommandLine cmd = parser.parse(options, args);

			File outputDirectory = parseOption(cmd, outputDirOption, File::new, null);
			File pirsrDataDirectory = parseOption(cmd, pirsrDataDirOption, FileCreatorChecker::createAndCheck, null);
			File inputFactFile = parseOption(cmd, inputFileOption, FileCreatorChecker::createAndCheck, null);
			File hmmalignCommand = parseOption(cmd, hmmalignOption, FileCreatorChecker::createAndCheck, null);
			
			pirsrRunner = new PIRSRRunner(pirsrDataDirectory, inputFactFile, outputDirectory, hmmalignCommand);
		} catch (Exception e) {
			logger.error(e.getMessage());
			displayUsage(options);
			exit(1);
		}
		pirsrRunner.run();
	}

	private static void displayUsage(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.setOptionComparator(Comparator.comparing(Option::isRequired, trueFirst()).thenComparing(Option::hasArg, trueFirst())
				.thenComparing(Option::hasLongOpt, trueFirst()).thenComparing(Option::getOpt));
		formatter.setWidth(100);
		formatter.setDescPadding(5);
		formatter.setLeftPadding(5);
		formatter.printHelp("pirsr", USAGE_SEPARATOR, options, USAGE_SEPARATOR, true);
	}

	private static boolean hasHelp(final Option help, final String[] args) throws ParseException {
		Options options = new Options();
		options.addOption(help);
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = parser.parse(options, args, true);
		return cmd.hasOption(help.getOpt());
	}
	
	private static <T> T parseOption(CommandLine commandLine, Option option, Function<String, T> creator, T defaultObject)
            throws MissingArgumentException {
        if (commandLine.hasOption(option.getOpt())){
            String optionValue = commandLine.getOptionValue(option.getOpt());
            if (Strings.isNullOrEmpty(optionValue)){
                throw new MissingArgumentException(option);
            } else {
                try {
                    return creator.apply(optionValue);
                } catch (Exception e){
                    throw new IllegalArgumentException(String.format("Wrong argument for option -%s. %s",
                            option.getOpt(), e.getMessage()), e);
                }
            }
        } else {
            return defaultObject;
        }
    }

    private static class FileCreatorChecker {
        static File createAndCheck(String path) {
            File file = new File(path);
            if (file.exists()){
                return file;
            } else {
                throw new IllegalArgumentException(String.format("No such file or directory: %s", file));
            }
        }
    }

}
