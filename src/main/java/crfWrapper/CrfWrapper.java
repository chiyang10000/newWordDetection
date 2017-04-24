package crfWrapper;

import evaluate.Corpus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashSet;

/**
 * Created by wan on 4/24/2017.
 */
abstract public class CrfWrapper {
	private static Logger logger = LoggerFactory.getLogger(CrfWrapper.class);
	static String model = new File("data/model/singleCharacterCRF.model").getAbsolutePath();
	static String crf_test = new File("lib/crf_test").getAbsolutePath();
	static String shell = "";
	static {
		if (System.getProperty("os.name").contains("Win") ) {
			shell = "cmd /c";
			crf_test += ".exe";
		}
		//windows 和 linux这里有区别
		if (!new File(crf_test).exists())
			logger.error("{} not exits!", crf_test);
		if (!new File(model).exists())
			logger.error("{} not exits!", model);

	}
	public static void decode(String bemsInputFile, String bemsOutputFile) {
		String cmd = String.join(" ", shell, crf_test, "-m", model, bemsInputFile, ">", bemsOutputFile);
			try {
				Process pro = Runtime.getRuntime().exec(cmd);
				InputStream in = pro.getErrorStream();
				BufferedReader read = new BufferedReader(new InputStreamReader(in));
				pro.waitFor();
				if (pro.exitValue() == 1)//p.exitValue()==0表示正常结束，1：非正常结束
					System.err.println("run crf_test fails");
				String line = null;
				while ((line = read.readLine()) != null) {
					System.out.println(line);
				}
				in.close();
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
	}
	public static void convertBEMSToSeg(String inputFile, String segFile, String newWordFile) {
		//write segFile and new word File
		HashSet<String> newWordList = new HashSet<>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(inputFile));
			BufferedWriter writerSeg = new BufferedWriter(new FileWriter(segFile));
			BufferedWriter writerNewWord = new BufferedWriter(new FileWriter(newWordFile));
			String tmp;
			while ( (tmp = reader.readLine()) !=null) {
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
					}while (tmp.length() > 0 && tmp.charAt(tmp.length() - 1) != 'E');
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
