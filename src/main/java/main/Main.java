package main;

/**
 * Created by wan on 5/26/2017.
 */

import crfModel.charBased;
import crfModel.wordBased;
import evaluate.Ner;
import org.apache.commons.cli.*;

public class Main {


	public static void main(String[] args) throws Exception {

		Options options = new Options();

		/*
		Option detectType_option = new Option("t", "type", true, "detect type");
		detectType_option.setRequired(true);
		options.addOption(detectType_option);
		*/

		Option inputFile_option = new Option("i", "input", true, "input file");
		inputFile_option.setRequired(true);
		options.addOption(inputFile_option);

		/*
		Option outputFile_option = new Option("o", "output", true, "output file");
		outputFile_option.setRequired(true);
		options.addOption(outputFile_option);
		*/

		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();
		CommandLine cmd;

		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			System.err.println(e.getMessage());
			formatter.printHelp("parameter", options);
			System.exit(1);
			return;
		}

		String inputFilePath = cmd.getOptionValue("input");
		System.out.println(inputFilePath);
		charBased ner = new charBased();
		wordBased newWord = new wordBased();
		ner.detectNewWord(inputFilePath, "per.txt", Ner.nr);
		ner.detectNewWord(inputFilePath, "loc.txt", Ner.ns);
		ner.detectNewWord(inputFilePath, "org.txt", Ner.nt);
		newWord.detectNewWord(inputFilePath, "new.txt", Ner.nw);
	}

}

