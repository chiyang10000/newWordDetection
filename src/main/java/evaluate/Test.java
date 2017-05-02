package evaluate;

import ansj.Ansj;
import crfModel.SegementationCRF;
import crfModel.SingleCharacterCRF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;


/**
 * Created by wan on 4/7/2017.
 */
public class Test {
	static final Logger logger = LoggerFactory.getLogger("report");

	public static void testOnSeg() {
		// todo
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
		logger.info("select " + select + " hit " + hit + " total " + sum);
		float p = (float) hit / select;
		float r = (float) hit / sum;
		logger.info("precision: " + p + "\trecall: " + r + "\tF-measure: " + 2.0 / (1.0 / p + 1.0 / r));

	}

	public static void main(String... args) {
		File[] files = new File("data/test").listFiles();

		SegementationCRF segementationCRF = new SegementationCRF("data/test/train.txt.src", "data/test/test.txt.src");
		segementationCRF.train(new String[]{"data/raw/train.txt"});
		SingleCharacterCRF singleCharacterCRF = new SingleCharacterCRF();
		singleCharacterCRF.train(new String[]{"data/raw/train.txt"});
		Ansj ansj = new Ansj();
		ArrayList<NewWordDetector> newWordDetectors = new ArrayList<NewWordDetector>();
		newWordDetectors.add(singleCharacterCRF);
		newWordDetectors.add(ansj);
		newWordDetectors.add(segementationCRF);

		for (File file : files)
			if (file.getName().matches(".*\\.src") && !file.getName().matches(".*train.*")) {
				String inputFile = file.getAbsolutePath();
				String answerFile = inputFile.replace(".src", ".corpus.ans");
				String outputFile;
				logger.info("Test on {}", file.getName());
				segementationCRF.nagao.addWordInfo(answerFile, "tmp/" + file.getName() + ".corpus.ans");

				/*
				outputFile = String.format("tmp/%s%s", "NagaoAlgorithm.", file.getName());
				NagaoAlgorithm nagao = new NagaoAlgorithm(10);
				nagao.detect(new String[]{inputFile}, outputFile, 10, 3, 1.5);
				Test.test(answerFile, outputFile);
				*/

				segementationCRF.convert2TrainInput(new String[]{"data/raw/" + file.getName().replace(".src", "")});
				logger.info("Most recall of ansj is {}", segementationCRF.mostRecallInTraindata);
				for (NewWordDetector newWordDetector : newWordDetectors) {
					//if (newWordDetector != segementationCRF) continue;
					outputFile = String.format("tmp/%s.%s", newWordDetector.getClass().getName(), file.getName());
					logger.info("Test on {}", newWordDetector.getClass().getCanonicalName());
					newWordDetector.detect(inputFile, outputFile);
					segementationCRF.nagao.addWordInfo(outputFile, outputFile + ".txt");
					Test.test(answerFile, outputFile);
				}

				logger.info("-----------------------------------");

			}
	}
}
