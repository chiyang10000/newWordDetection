package evaluate;

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
	public static double p, r, f1;
	public static int hit, select, sum;

	static {
		logger.debug("---------****----------\n");
		logger.debug("shuffle is {}", config.isShuffle);
		logger.debug("word filter is {} ", config.isNewWordFilter);
		logger.debug("exclude new word pattern {}", config.newWordExcludeRegex);
	}

	public static double test(Map<String, String> golden, Map<String, String> ans,
							  Ner type, String method, String tool) {
		sum = golden.size();
		select = ans.size();
		hit = 0;
		try {
			PrintWriter pWriter = new PrintWriter(new File(String.join(".", "info/" + type.name, method, tool, "p")), "utf8");
			PrintWriter rWriter = new PrintWriter(new File(String.join(".", "info/" + type.name, method, tool, "r")));
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

	public static HashMap<String, String> readWordList(String inputFile) {
		HashMap<String, String> wordList = new HashMap<>();

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(inputFile));
			String line;
			while ((line = reader.readLine()) != null) {
				String[] tmp = line.split("\\t", 2);
				//tmp[0] = config.newWordFileter(tmp[0]);
				wordList.put(config.newWordFileter(tmp[0]), tmp[1]);
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
			config.algorithmInCRFSuite = args[0] + config.algorithmInCRFSuite;
		}
		//clean();
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

		String testData = config.totalData;
		String outputFile;
/*
		HashMap<String, String> tmp = readWordList("tmp/tmp.new");
		HashMap<String, String> tmp1 = readWordList("tmp/tmp.per");
		HashMap<String, String> tmp2 = readWordList("tmp/tmp.loc");
		HashMap<String, String> tmp3 = readWordList("tmp/tmp.org");
		//for (String t: tmp1.keySet()) tmp.put(t,"nr");
		//for (String t: tmp2.keySet()) tmp.put(t,"ns");
		for (String t: tmp3.keySet()) tmp.put(t,"nt");
		test(readWordList(config.getAnswerFile(config.testData, Ner.nw)), tmp, Ner.nw, "混合", "新词加实体识别");

		test(readWordList(config.getAnswerFile(config.testData, Ner.nr)), tmp, Ner.nr, "混合", "新词加实体识别");
		test(readWordList(config.getAnswerFile(config.testData, Ner.ns)), tmp, Ner.ns, "混合", "新词加实体识别");
		test(readWordList(config.getAnswerFile(config.testData, Ner.nt)), tmp, Ner.nt, "混合", "新词加实体识别");
		*/

		for (Ner nerType : Ner.supported) {
			String answerFile = config.getAnswerFile(testData, nerType);
			logger.debug("+++++++   {}   ++++++++", answerFile);
			for (NewWordDetector newWordDetector : newWordDetectors) {
				outputFile = String.format("tmp/%s.%s", newWordDetector.getClass().getSimpleName(), answerFile.replaceAll(".*/", ""));
				Test.test(readWordList(answerFile),
						newWordDetector.detectNewWord(testData, outputFile, nerType),
						nerType, newWordDetector.getClass().getSimpleName(),
						newWordDetector.getClass().getName().contains("CRF") ? (config.isCRFsuite ? config.algorithmInCRFSuite : "crf++") : "ansj");
			}
		}
		logger.debug("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n");
	}

}
