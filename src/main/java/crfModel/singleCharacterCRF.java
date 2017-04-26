package crfModel;

import Config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * Created by wan on 4/24/2017.
 */
public class singleCharacterCRF extends crfppWrapper {
	private static final Logger logger = LoggerFactory.getLogger(singleCharacterCRF.class);
	static String trainData = "tmp/crfModel.crf";
	static String template = "data/crf-template/crfModel.template";
	static String model = "data/model/singleCharacterCRF.model";

	public static void detect(String inputFile, String outputFile) {
		String bemsInputFile = inputFile + ".txt";
		String bemsOutputFile = "tmp/crfModel.bems.out.txt";
		convertSrcToBEMS(new String[]{inputFile}, bemsInputFile);
		decode(model, bemsInputFile, bemsOutputFile);
		convertBEMSToSeg(bemsOutputFile, "tmp/crfModel.txt", outputFile);
	}

	public static void convertSrcToBEMS(String[] inputFiles, String outputFile) {
		logger.debug("convert {} to {}", inputFiles, outputFile);
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
			BufferedReader reader;
			String tmp;
			for (String inputFile : inputFiles) {
				reader = new BufferedReader(new FileReader(inputFile));
				while ((tmp = reader.readLine()) != null) {
					String[] tmps = tmp.split(Config.sepSentenceRegex);
					int offset = 0;
					for (String sentence : tmps) {
						offset += sentence.length() + 1;
						//考虑没有逗号和句号的行
						for (int i = 0; i < sentence.length(); i++) {
							writer.append(sentence.charAt(i));
							writer.newLine();
						}
						if (offset - 1 < tmp.length()) {
							writer.newLine();
							writer.append(tmp.charAt(offset - 1));
							writer.newLine();
						}
						writer.newLine();
					}
				}
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param inputFiles
	 */
	public static void convertToTrainBEMS(String[] inputFiles, String outputFile) {
		logger.debug("convert {} to {}", inputFiles, outputFile);
		try {
			for (String inputFile : inputFiles) {
				BufferedReader reader = new BufferedReader(new FileReader(inputFile));
				BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
				String tmp;
				while ((tmp = reader.readLine()) != null) {
					if (tmp.trim().length() == 0) continue;
					String[] segs = tmp.split(" +");
					for (String seg : segs) {
						//System.out.println(seg);
						String word = (seg.split("/")[0]);
						if (word.length() == 0) {
							writer.newLine();
							writer.append(String.format("%s\t%s", '/', 'S'));
							writer.newLine();
							writer.newLine();
						}// 两个斜线
						else if (word.length() == 1) {
							if (word.matches(Config.sepSentenceRegex))
								writer.newLine();
							writer.append(String.format("%s\t%s", word.charAt(0), 'S'));
							writer.newLine();
							if (word.matches(Config.sepSentenceRegex))
								writer.newLine();
						} else {
							writer.append(String.format("%s\t%s", word.charAt(0), 'B'));
							writer.newLine();
							for (int i = 1; i < word.length() - 1; i++) {
								writer.append(String.format("%s\t%s", word.charAt(i), 'M'));
								writer.newLine();
							}
							writer.append(String.format("%s\t%s", word.charAt(word.length() - 1), 'E'));
							writer.newLine();
						}
					}
					writer.append(' ');
					writer.newLine();
				}
				writer.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String... args) {

		String[] basicWordFiles = {"data/raw/2000-01-粗标.txt", "data/raw/2000-02-粗标.txt", "data/raw/2000-03-粗标.txt"};
		convertToTrainBEMS(basicWordFiles, trainData);

		train(template, trainData, model);
	}
}
