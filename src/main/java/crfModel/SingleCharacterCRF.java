package crfModel;

import Config.Config;
import evaluate.Corpus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashSet;

/**
 * Created by wan on 4/24/2017.
 */
public class SingleCharacterCRF extends crfppWrapper {
	private static final Logger logger = LoggerFactory.getLogger(SingleCharacterCRF.class);

	{
		template = "data/crf-template/SingleCharacterCRF.template";
		model = "data/model/SingleCharacterCRF.model";
		trainData = "tmp/SingleCharacterCRF.crf";
	}

	public static void main(String... args) {
		String[] corpus = {"data/raw/2000-01-粗标.txt", "data/raw/2000-02-粗标.txt", "data/raw/2000-03-粗标.txt"};
		corpus = new String[]{"data/raw/train.txt"};
		SingleCharacterCRF singleCharacterCRF = new SingleCharacterCRF();
		//singleCharacterCRF.convert2TrainInput(corpus);
		singleCharacterCRF.train(corpus);
	}

	public void convertSrc2TestInput(String[] inputFiles, String outputFile) {
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
						// todo 考虑没有逗号和句号的行
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

	public void convert2TrainInput(String[] inputFiles) {
		logger.debug("convert {} to {}", inputFiles, trainData);
		try {
			for (String inputFile : inputFiles) {
				BufferedReader reader = new BufferedReader(new FileReader(inputFile));
				BufferedWriter writer = new BufferedWriter(new FileWriter(trainData));
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

	public void convertTestOuput2Res(String inputFile, String newWordFile) {
		HashSet<String> newWordList = new HashSet<>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(inputFile));
			BufferedWriter writerNewWord = new BufferedWriter(new FileWriter(newWordFile));
			String tmp;
			while ((tmp = reader.readLine()) != null) {
				StringBuilder wordBuffer = new StringBuilder();
				if (tmp.length() == 0)
					continue;
				wordBuffer.append(tmp.split("\t", 2)[0]);
				if (tmp.charAt(tmp.length() - 1) == 'B') {
					do {
						tmp = reader.readLine();
						wordBuffer.append(tmp.split("\t", 2)[0]);
					} while (tmp.length() > 0 && tmp.charAt(tmp.length() - 1) != 'E');
				}

				String word = wordBuffer.toString();
				if (Corpus.isNewWord(word) && !newWordList.contains(word)) {
					newWordList.add(word);
					writerNewWord.append(word);
					writerNewWord.newLine();
				}
			}
			writerNewWord.close();
		} catch (IOException e) {
			logger.error("err!");
			e.printStackTrace();
		}
	}
}
