package evaluate;

import NagaoAlgorithm.NagaoAlgorithm;
import ansj.AnsjNlp;
import ansj.AnsjTo;
import crfModel.charBased;
import crfModel.wordBased;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by wan on 4/7/2017.
 */
public class Test {
	static final DecimalFormat df = new DecimalFormat("##.000");
	static final Logger logger = LoggerFactory.getLogger("report");

	static {
		logger.debug("---------****----------\n");
		logger.debug("shuffle is {}", config.isShuffle);
		logger.debug("word filter is {} ", config.isNewWordFilter);
		logger.debug("exclude new word pattern {}", config.newWordExcludeRegex);
	}

	/**
	 * 传入一个已分词好的文件。
	 */
	public static void testOnSeg(String inputFile) {
		// todo
	}

	public static double test(Map<String, String> golden, Map<String, String> ans,
							  Ner type, String method, String tool) {
		sum = golden.size();
		select = ans.size();
		hit = 0;
		try {
			PrintWriter pWriter = new PrintWriter(new FileWriter(String.join(".","info/"+type.name, method, tool, "p")));
			PrintWriter rWriter = new PrintWriter(new FileWriter(String.join(".","info/"+type.name, method, tool, "r")));
			for (String word : ans.keySet())
				if (golden.keySet().contains(word) || golden.keySet().contains(config.newWordFileter(word))) {
					hit++;
					pWriter.println(word + "\t" + ans.get(word) + "\tTrue");
				} else
					pWriter.println(word + "\t" + ans.get(word) + "\tFalse");
			for (String word : golden.keySet())
				if (ans.keySet().contains(word)) {
					rWriter.println(word + "\t" + golden.get(word) + "\tTrue");
				} else
					rWriter.println(word + "\t" + golden.get(word) + "\tFalse");
			pWriter.close();
			rWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		p = (float) hit / select * 100;
		r = (float) hit / sum * 100;
		f1 = (float) 2.0 * p * r / (p + r);
		if (hit == 0)
			return 100;
		if (!tool.equals("counter"))
		logger.info("{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}",
				df.format(f1), df.format(p), df.format(r),
				df.format(type.oov),
				hit, select, sum,
				type.name, method, tool, config.comment
		);
		return 100 - p;
	}

	public static double p, r, f1;
	public static int hit, select, sum;

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
		RunSystemCommand.run("rm tmp/*");
		RunSystemCommand.run("rm data/info/*");
	}

	public static void main(String... args) {
		if (args.length > 0) {
			config.isCRFsuite = true;
			config.algorithm = args[0] + config.algorithm;
		}
		clean();
		Ner.calcOOV();

		wordBased wordBased = new wordBased();
		charBased charBased = new charBased();
		AnsjTo ansjTo = new AnsjTo();
		AnsjNlp ansjNlp = new AnsjNlp();

		ArrayList<NewWordDetector> newWordDetectors = new ArrayList<>();
		//newWordDetectors.add(nagao);
		newWordDetectors.add(ansjTo);
		newWordDetectors.add(ansjNlp);
		newWordDetectors.add(charBased);
		newWordDetectors.add(wordBased);

		String inputFile = config.getInputFile(config.testData);
		String outputFile;


		for (Ner nerType : Ner.supported) {
			String answerFile = config.getAnswerFile(config.testData, nerType);
			logger.debug("+++++++   {}   ++++++++", answerFile);
			for (NewWordDetector newWordDetector : newWordDetectors) {
				outputFile = String.format("tmp/%s.%s", newWordDetector.getClass().getSimpleName(), answerFile
						.replaceAll(".*/", ""));
				Test.test(readWordList(answerFile),
						newWordDetector.detectNewWord(inputFile, outputFile, nerType),
						nerType, newWordDetector.getClass().getSimpleName(),
						newWordDetector.getClass().getName().contains("CRF") ? (config.isCRFsuite ? "ap": "crf") : "ansj");
			}
		}
		logger.debug("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n");
	}

}
