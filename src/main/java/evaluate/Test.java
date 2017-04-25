package evaluate;

import NagaoAlgorithm.NagaoAlgorithm;
import singleCharacterCRF.singleCharacterCRF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashSet;


/**
 * Created by wan on 4/7/2017.
 */
public class Test {
	static final Logger logger = LoggerFactory.getLogger("report");

	public static void testOnSeg() {

	}

	public static void test(String golden, String ans) {
		int hit = 0, select = 0, sum = 0;
		HashSet<String> goldenAnswer = new HashSet<>();
		BufferedReader reader;
		String word;
		try {
			reader = new BufferedReader(new FileReader(golden));
			while ((word = reader.readLine()) != null) {
				goldenAnswer.add(word);
			}
			sum = goldenAnswer.size();
			reader = new BufferedReader(new FileReader(ans));
			while ((word = reader.readLine()) != null) {
				select++;
				if (goldenAnswer.contains(word))
					hit++;
			}
		} catch (java.io.IOException e) {
			e.printStackTrace();
		}
		//System.out.println("evaluate on " + golden);
		logger.info("select " + select + " hit " + hit + " total " + sum);
		float p = (float) hit / select;
		float r = (float) hit / sum;
		logger.info("precision: " + p + "\trecall: " + r + "\tF-measure: " + 2.0 / (1.0 / p + 1.0 / r));

	}

	public static void main(String... args) {
		int counter = 0;
		File[] files = new File("data/test").listFiles();
		for (File file : files)
			if (file.getName().matches(".*\\.src")) {
				String inputFile = file.getAbsolutePath();
				String answerFile = inputFile.replace(".src", ".ans");
				String outputFile;

				counter++;
				logger.info("Test {}", counter);

				/*
				outputFile = String.format("tmp/%s_%s", "NagaoAlgorithm", inputFile.replaceAll("^.*\\\\", ""));
				NagaoAlgorithm.detect(new String[]{inputFile}, outputFile, "data/basicWordList.txt", 10, "10,3,3,5");
				Test.test(answerFile, outputFile);
				*/

				System.out.println("-------");

				outputFile = String.format("tmp/%s_%s", "singleCharacterCRF.", file.getName());
				singleCharacterCRF.detect(inputFile, outputFile);
				Test.test(answerFile, outputFile);

				System.out.println();
				//if (counter >0) break;
			}
	}
}
