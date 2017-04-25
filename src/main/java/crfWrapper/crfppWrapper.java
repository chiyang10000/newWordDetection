package crfWrapper;

import evaluate.Corpus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashSet;

/**
 * Created by wan on 4/24/2017.
 */
abstract public class crfppWrapper {
	private static Logger logger = LoggerFactory.getLogger(crfppWrapper.class);
	static String model = new File("data/model/singleCharacterCRF.model").getAbsolutePath();
	static String crf_test = new File("lib/crfpp/crf_test").getAbsolutePath();
	static String crf_learn = new File("lib/crfpp/crf_learn").getAbsolutePath();
	static String shell = "";

	static {
		if (System.getProperty("os.name").contains("Win")) {
			shell = "cmd /c";
			crf_test += ".exe";
			crf_learn += ".exe";
		}
		//windows 和 linux这里有区别
		if (!new File(crf_test).exists())
			logger.error("{} not exits!", crf_test);
		if (!new File(model).exists())
			logger.error("{} not exits!", model);

	}

	private static void runCommand(String cmd) {
		try {
			logger.debug("Run command: [{}]", cmd);
			Process pro = Runtime.getRuntime().exec(cmd);
			InputStream in = pro.getInputStream();
			BufferedReader read = new BufferedReader(new InputStreamReader(in));
			String line = null;
			while ((line = read.readLine()) != null) {
				System.err.println(line);
			}
			in.close();
			pro.waitFor();
			in = pro.getErrorStream();
			read = new BufferedReader(new InputStreamReader(in));
			while ((line = read.readLine()) != null) {
				System.err.println(line);
			}
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void decode(String bemsInputFile, String bemsOutputFile) {
		String cmd = String.join(" ", shell, crf_test, "-m", model, bemsInputFile, "-o", bemsOutputFile);
		runCommand(cmd);
	}

	public static void train(String template, String trainData, String model) {
		String cmd = String.join(" ", shell, crf_learn, template, trainData, model, "-t");
		runCommand(cmd);
	}

	public static void convertBEMSToSeg(String inputFile, String segFile, String newWordFile) {
		//write segFile and new word File
		HashSet<String> newWordList = new HashSet<>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(inputFile));
			BufferedWriter writerSeg = new BufferedWriter(new FileWriter(segFile));
			BufferedWriter writerNewWord = new BufferedWriter(new FileWriter(newWordFile));
			String tmp;
			while ((tmp = reader.readLine()) != null) {
				if (tmp.length() == 0) {
					writerSeg.newLine();
					continue;
				}
				StringBuilder wordBuffer = new StringBuilder();
				wordBuffer.append(tmp.split("\t", 2)[0]);
				if (tmp.charAt(tmp.length() - 1) == 'B') {
					do {
						tmp = reader.readLine();
						wordBuffer.append(tmp.split("\t", 2)[0]);
					} while (tmp.length() > 0 && tmp.charAt(tmp.length() - 1) != 'E');
				}

				String word = wordBuffer.toString();
				writerSeg.append(word + ' ');
				if (Corpus.isNewWord(word) && !newWordList.contains(word)) {
					newWordList.add(word);
					writerNewWord.append(word);
					writerNewWord.newLine();
				}
			}
			writerNewWord.close();
			writerSeg.close();
		} catch (java.io.IOException e) {
			e.printStackTrace();
		}
	}
}
