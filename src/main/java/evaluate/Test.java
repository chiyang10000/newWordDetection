package evaluate;

import NagaoAlgorithm.NagaoAlgorithm;
import ansj.Ansj;
import crfModel.WordCRF;
import crfModel.CharacterCRF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;


/**
 * Created by wan on 4/7/2017.
 */
public class Test {
	static final Logger logger = LoggerFactory.getLogger("report");

	/**
	 * 传入一个已分词好的文件。
	 */
	public static void testOnSeg(String inputFile) {
		// todo
	}

	public static void test(Set<String> golden, Set<String> ans) {
		int sum = golden.size(),
				select = ans.size();
		int hit = 0;
		for (String word : ans)
			if (golden.contains(word))
				hit++;
		float p = (float) hit / select;
		float r = (float) hit / sum;
		float f1 = (float) 2.0 * p * r / (p + r);
		logger.info("p   {}   r   {}   f   {}    select {} hit {} in total {}", p, r, f1, select, hit, sum);
	}

	public static Set<String> readWordList(String inputFile) {
		HashSet<String> wordList = new HashSet<>();

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(inputFile));
			String line;
			while ((line = reader.readLine()) != null) {
				wordList.add(line.split("\\s+")[0]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		logger.debug("word list size of {} is {}", inputFile, wordList.size());
		return wordList;
	}

	public static void main(String... args) {
		logger.info("compare test and train in nw");
		test(readWordList(config.trainDataNWAns), readWordList(config.testDataNWAns));
		logger.info("compare test and train in nr");
		test(readWordList(config.trainDataNRAns), readWordList(config.testDataNRAns));
		logger.info("compare test and train in ns");
		test(readWordList(config.trainDataNSAns), readWordList(config.testDataNSAns));
		//Corpus.
		File[] files = new File("data/test").listFiles();

		WordCRF segementationCRF = new WordCRF("data/test/train.txt.src", "data/test/test.txt.src");
		//segementationCRF.train(new String[]{"data/raw/train.txt"});
		CharacterCRF singleCharacterCRF = new CharacterCRF();
		NagaoAlgorithm nagao = new NagaoAlgorithm(config.maxNagaoLength);
		//singleCharacterCRF.train(new String[]{"data/raw/train.txt"});
		Ansj ansj = new Ansj();
		ArrayList<NewWordDetector> newWordDetectors = new ArrayList<NewWordDetector>();
		newWordDetectors.add(singleCharacterCRF);
		newWordDetectors.add(ansj);
		newWordDetectors.add(segementationCRF);
		newWordDetectors.add(nagao);

		for (File file : files)
			if (file.getName().matches(".*\\.src") && !file.getName().matches(".*train.*")) {
				String inputFile = file.getAbsolutePath();
				String answerNewWordFile = inputFile.replace(".src", ".nw.ans");
				String answerNrFile = inputFile.replace(".src", ".nr.ans");
				String answerNsFile = inputFile.replace(".src", ".ns.ans");
				String outputFile;
				logger.info("Test on {}", file.getName());
				segementationCRF.nagao.addWordInfo(answerNewWordFile, "tmp/" + file.getName() + ".nw.ans");
/*
				logger.info("test name ansj");
				ansj.detectName(inputFile, "tmp0.ans");
				test(answerNrFile, "tmp0.ans");

				logger.info("test place ansj");
				ansj.detectPlace(inputFile, "tmp1.ans");
				test(answerNsFile, "tmp1.ans");
				*/

				//segementationCRF.convert2TrainInput(new String[]{"data/raw/" + file.getName().replace(".ans", "")});
				//logger.info("Most recall of ansj is {}", segementationCRF.mostRecallInTraindata);

				for (NewWordDetector newWordDetector : newWordDetectors) {
					//if (newWordDetector != segementationCRF) continue;
					outputFile = String.format("tmp/%s.%s", newWordDetector.getClass().getName(), file.getName());
					logger.info("Test on new word {}", newWordDetector.getClass().getCanonicalName());
					Test.test(readWordList(answerNewWordFile), newWordDetector.detectNewWord(inputFile, outputFile,
							"nw"));
					segementationCRF.nagao.addWordInfo(outputFile, outputFile + ".txt");

					/*
					logger.info("Test on name {}", newWordDetector.getClass().getCanonicalName());
					newWordDetector.detectNewWord(inputFile, outputFile);
					segementationCRF.nagao.addWordInfo(outputFile, outputFile + ".txt");
					Test.test(answerNewWordFile, outputFile);
					*/
				}

				logger.info("-----------------------------------");

			}
	}
}
