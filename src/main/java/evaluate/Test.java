package evaluate;

import NagaoAlgorithm.NagaoAlgorithm;
import ansj.AnsjNlpAnalysis;
import ansj.AnsjToAnalysis;
import crfModel.CharacterCRF;
import crfModel.WordCRF;
import dataProcess.WordInfoInCorpus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


/**
 * Created by wan on 4/7/2017.
 */
public class Test {
	static final DecimalFormat df = new DecimalFormat("##.000");
	static private final Logger logger = LoggerFactory.getLogger("report");

	static {
		logger.info("---------****----------\n");
		if (config.isAnsjFeatureOpen)
			config.openAnsj();
		logger.info("ansj feature open is {}", config.isAnsjFeatureOpen);
		logger.info("shuffle is {}", config.isShuffle);
		logger.info("corpus is {}", config.corpusInput);
		logger.info("word filter is {} ", config.isNewWordFilter);
		logger.info("exclude new word pattern {}", config.newWordExcludeRegex);
	}

	/**
	 * 传入一个已分词好的文件。
	 */
	public static void testOnSeg(String inputFile) {
		// todo
	}

	public static String getAnswerFile(String inputFile, String pattern) {
		return "data/test/ans/" + inputFile.replaceAll(".*/", "") + "." + pattern;
	}

	public static void test(Set<String> golden, Set<String> ans, String prefix) {
		HashMap<String, Integer> hitCounter = new HashMap<>(), selectCounter = new HashMap<>(), totalCounter = new HashMap<>();
		for (String type: config.newWordType.keySet()) {
			hitCounter.put(type, 0);
			selectCounter.put(type, 0);
			totalCounter.put(type, 0);
		}
		int sum = golden.size(),
				select = ans.size();
		int hit = 0;
		for (String word : ans)
			if (golden.contains(word))
				hit++;
		float p = (float) hit / select * 100;
		float r = (float) hit / sum * 100;
		float f1 = (float) 2.0 * p * r / (p + r);
		logger.info("p  {}\tr  {}\tf  {}  {} select {} hit {} in total {}", df.format(p), df.format(r), df.format(f1), prefix, select, hit, sum);
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

	public static void clean() {
		RunSystemCommand.run("find tmp -type f | grep -v gitignore | xargs rm");
	}

	public static void main(String... args) {
		WordInfoInCorpus wordInfoInCorpus = new WordInfoInCorpus(config.corpusInput);
		clean();
		wordInfoInCorpus.addWordInfo(getAnswerFile(config.totalDataInput, config.nw), "new.info");

		for (String type : config.supportedType) {
			logger.info("compare test and train in {}", type);
			test(
					readWordList(getAnswerFile(config.trainDataInput, type)),
					readWordList(getAnswerFile(config.testDataInput, type)),
					type);
		}

		WordCRF segementationCRF = new WordCRF();
		CharacterCRF singleCharacterCRF = new CharacterCRF();
		NagaoAlgorithm nagao = new NagaoAlgorithm(config.maxNagaoLength);
		AnsjToAnalysis ansjToAnalysis = new AnsjToAnalysis();
		AnsjNlpAnalysis ansjNlpAnalysis = new AnsjNlpAnalysis();

		ArrayList<NewWordDetector> newWordDetectors = new ArrayList<>();
		newWordDetectors.add(nagao);
		newWordDetectors.add(ansjToAnalysis);
		newWordDetectors.add(ansjNlpAnalysis);
		newWordDetectors.add(singleCharacterCRF);
		newWordDetectors.add(segementationCRF);

		String inputFile = config.testDataInput;
		String outputFile;


		for (String type : new String[]{config.nw, config.nr, config.ns}) {
			String answerFile = getAnswerFile(inputFile, type);
			wordInfoInCorpus.addWordInfo(answerFile, "tmp/" + type + ".info");
			logger.info("+++++++   {}   ++++++++", answerFile);
			for (NewWordDetector newWordDetector : newWordDetectors) {
				//if (newWordDetector != nagao) continue;
				outputFile = String.format("tmp/%s.%s", newWordDetector.getClass().getSimpleName(), answerFile.replaceAll(".*/", ""));
				Test.test(readWordList(answerFile), newWordDetector.detectNewWord(inputFile, outputFile, type), newWordDetector.getClass().getSimpleName());
				wordInfoInCorpus.addWordInfo(outputFile, outputFile + ".info");
			}
		}
		logger.info("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n");
	}

}
