package evaluate;

import NagaoAlgorithm.NagaoAlgorithm;
import ansj.AnsjNlpAnalysis;
import ansj.AnsjToAnalysis;
import crfModel.CharacterCRF;
import crfModel.WordCRF;
import dataProcess.WordInfoInCorpus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;


/**
 * Created by wan on 4/7/2017.
 */
public class Test {
	static final DecimalFormat df = new DecimalFormat("##.000");
	static private final Logger logger = LoggerFactory.getLogger("report");

	static {
		logger.info("---------****----------\n");
		logger.info("shuffle is {}", config.isShuffle);
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

	public static void test(Map<String, String> golden, Map<String, String> ans, String prefix) {
		HashMap<String, Integer> hitCounter = new HashMap<>(), selectCounter = new HashMap<>(), totalCounter = new HashMap<>();
		try {
			PrintWriter pWriter = new PrintWriter(new FileWriter("data/info/" + prefix + ".p"));
			PrintWriter rWriter = new PrintWriter(new FileWriter("data/info/" + prefix + ".r"));
			int sum = golden.size(),
					select = ans.size();
			int hit = 0;
			for (String word : ans.keySet())
				if (golden.keySet().contains(word) || golden.keySet().contains(config.newWordFileter(word))) {
					hit++;
					pWriter.println(word + "\t" + ans.get(word) + "\tyes");
				}
				else
					pWriter.println(word + "\t" + ans.get(word) + "\tno");
			for (String word : golden.keySet())
				if (ans.keySet().contains(word)) {
					rWriter.println(word + "\t" + golden.get(word) + "\tyes");
				}
				else
					rWriter.println(word + "\t" + golden.get(word) + "\tno");
			float p = (float) hit / select * 100;
			float r = (float) hit / sum * 100;
			float f1 = (float) 2.0 * p * r / (p + r);
			logger.info("p {}\tr {}\tf {}  {} select {} hit {} total {}", df.format(p), df.format(r), df.format(f1), prefix, select, hit, sum);
			pWriter.close();
			rWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Map<String, String> readWordList(String inputFile) {
		HashMap<String, String> wordList = new HashMap<>();

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(inputFile));
			String line;
			while ((line = reader.readLine()) != null) {
				String[] tmp = line.split("\\t", 2);
				//tmp[0] = config.newWordFileter(tmp[0]);
				wordList.put(tmp[0], tmp[1]);
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
		clean();

		for (String type : config.supportedType) {
			logger.info("compare test and train in {}", type);
			test(
					readWordList(getAnswerFile(config.trainDataInput, type)),
					readWordList(getAnswerFile(config.testDataInput, type)),
					type);
		}

		WordCRF wordCRF = new WordCRF();
		CharacterCRF characterCRF = new CharacterCRF();
		NagaoAlgorithm nagao = new NagaoAlgorithm(config.maxStringLength);
		AnsjToAnalysis ansjToAnalysis = new AnsjToAnalysis();
		AnsjNlpAnalysis ansjNlpAnalysis = new AnsjNlpAnalysis();

		ArrayList<NewWordDetector> newWordDetectors = new ArrayList<>();
		newWordDetectors.add(nagao);
		newWordDetectors.add(ansjToAnalysis);
		newWordDetectors.add(ansjNlpAnalysis);
		newWordDetectors.add(characterCRF);
		newWordDetectors.add(wordCRF);
		if (config.isTrain) {
			characterCRF.main();
			wordCRF.main();
		}

		String inputFile = config.totalDataInput;
		String outputFile;


		for (String type : new String[]{config.nw, config.nr, config.ns}) {
			String answerFile = getAnswerFile(inputFile, type);
			logger.info("+++++++   {}   ++++++++", answerFile);
			for (NewWordDetector newWordDetector : newWordDetectors) {
				if (newWordDetector != wordCRF) continue;
				outputFile = String.format("tmp/%s.%s", newWordDetector.getClass().getSimpleName(), answerFile.replaceAll(".*/", ""));
				Test.test(readWordList(answerFile), newWordDetector.detectNewWord(inputFile, outputFile, type),
						newWordDetector.getClass().getSimpleName() + "." + type);
			}
		}
		logger.info("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n");
	}

}
