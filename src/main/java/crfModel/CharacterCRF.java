package crfModel;

import evaluate.Corpus;
import evaluate.Test;
import evaluate.config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

/**
 * 新词发现当成分词问题做
 * 命名实体识别当成
 * Created by wan on 4/24/2017.
 */
public class CharacterCRF extends crfppWrapper {
	private static final Logger logger = LoggerFactory.getLogger(CharacterCRF.class);

	public static void main(String... args) {
		String[] corpus = {"data/raw/2000-01-粗标.txt", "data/raw/2000-02-粗标.txt", "data/raw/2000-03-粗标.txt"};
		corpus = new String[]{"data/raw/train.txt"};
		CharacterCRF singleCharacterCRF = new CharacterCRF();

		singleCharacterCRF.train(corpus, config.nw);
		Test.test(Test.readWordList(config.testDataNWAns), singleCharacterCRF.detectNewWord(config.testDataSrc,"tmp/tmp.nw", config.nw));

		singleCharacterCRF.train(corpus, config.nr);
		Test.test(Test.readWordList(config.testDataNRAns), singleCharacterCRF.detectNewWord(config.testDataSrc,
				"tmp/tmp.nr", config.nr));
	}

	public void convertSrc2TestInput(String[] inputFiles, String outputFile, String pattern) {
		if (pattern.equals("nr") || pattern.equals("ns"))
			logger.info("not supported");
		logger.debug("convert {} to {} for {}", inputFiles, outputFile, pattern);
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
			BufferedReader reader;
			String tmp;
			for (String inputFile : inputFiles) {
				reader = new BufferedReader(new FileReader(inputFile));

				if (pattern == config.nw) {
					while ((tmp = reader.readLine()) != null) {
						String[] tmps = tmp.split(config.sepSentenceRegex);
						int offset = 0;
						for (String sentence : tmps) {
							offset += sentence.length() + 1; // offset这里是把去掉的标点符号补上
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
				} // nw

				if (pattern == config.nr) {
						while ((tmp = reader.readLine()) != null) {
						String[] tmps = tmp.split(config.sepSentenceRegex);
						int offset = 0;
						for (String sentence : tmps) {
							offset += sentence.length() + 1; // offset这里是把去掉的标点符号补上
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
				} //nr
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void convert2TrainInput(String[] inputFiles, String pattern) {
		if (pattern.equals("nr") || pattern.equals("ns"))
			logger.info("not supported");
		logger.debug("convert {} to {} for {}", inputFiles, trainData, pattern);
		try {
			PrintWriter writer = new PrintWriter(new FileWriter(trainData));
			for (String inputFile : inputFiles) {
				BufferedReader reader = new BufferedReader(new FileReader(inputFile));
				String tmp;

				if (pattern == config.nr) {
					while ((tmp = reader.readLine()) != null) {
						if (tmp.trim().length() == 0) continue;
						String[] segs = tmp.split(config.sepWordRegex);
						for (String seg : segs) {
							String word = config.removePos(seg);
							String pos = config.getPos(seg);
							if (word.length() == 1) {
								if (pos.equals(pattern))
									writer.println(String.format("%s\t%s", word.charAt(0), label_single));
								else
									writer.println(String.format("%s\t%s", word.charAt(0), label_other));

							} else {
								if (pos.equals(pattern)) {
									writer.println(String.format("%s\t%s", word.charAt(0), label_begin));
									for (int i = 1; i < word.length() - 1; i++) {
										writer.println(String.format("%s\t%s", word.charAt(i), label_meddle));
									}
									writer.println(String.format("%s\t%s", word.charAt(word.length() - 1), label_end));
								} else {
									for (int i = 0; i < word.length(); i++)
										writer.println(String.format("%s\t%s", word.charAt(i), label_other));
								}
							}
						}
						writer.println();
					}
				}// nr

				if (pattern == config.nw) {
					while ((tmp = reader.readLine()) != null) {
						if (tmp.trim().length() == 0) continue;
						String[] segs = tmp.split(config.sepWordRegex);
						for (String seg : segs) {
							String word = config.removePos(seg);
							if (word.length() == 1) {
								writer.println(String.format("%s\t%s", word.charAt(0), label_single));
							} else {
								writer.println(String.format("%s\t%s", word.charAt(0), label_begin));
								for (int i = 1; i < word.length() - 1; i++)
									writer.println(String.format("%s\t%s", word.charAt(i), label_meddle));
								writer.println(String.format("%s\t%s", word.charAt(word.length() - 1), label_end));
							}
						}
						writer.println();
					}
				}// nw

			} // 读取每个文件
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Set<String> convertTestOuput2Res(String inputFile, String newWordFile, String pattern) {
		HashSet<String> newWordList = new HashSet<>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(inputFile));
			PrintWriter writer = new PrintWriter(new FileWriter(newWordFile));
			String tmp;

			if (pattern == config.nw) {
				while ((tmp = reader.readLine()) != null) {
					StringBuilder wordBuffer = new StringBuilder();
					if (tmp.length() == 0)
						continue;
					wordBuffer.append(tmp.split("\t", 2)[0]);
					if (tmp.charAt(tmp.length() - 1) == label_begin) {
						do {
							tmp = reader.readLine();
							wordBuffer.append(tmp.split("\t", 2)[0]);
						} while (tmp.length() > 0 && tmp.charAt(tmp.length() - 1) != label_end);
					}

					String word = wordBuffer.toString();// 这是一个词
					if (Corpus.isNewWord(word) && !newWordList.contains(word)) {
						newWordList.add(word);
						writer.println(word);
					}
				}
			} // nw

			if (pattern == config.nr) {
				while ((tmp = reader.readLine()) != null) {
					StringBuilder wordBuffer = new StringBuilder();
					if (tmp.length() == 0)
						continue;
					wordBuffer.append(tmp.split("\t", 2)[0]);
					char label_head = tmp.charAt(tmp.length() - 1);
					if (tmp.charAt(tmp.length() - 1) == label_begin) {
						do {
							tmp = reader.readLine();
							wordBuffer.append(tmp.split("\t", 2)[0]);
						} while (tmp.length() > 0 && tmp.charAt(tmp.length() - 1) != label_end);
					}

					String word = wordBuffer.toString();
					if (label_head == label_begin || label_head == label_single) //单字名称 和 多字名称
						if (!newWordList.contains(word)) {
							newWordList.add(word);
							writer.println(word);
						}
				}
			} // nr

			writer.close();
		} catch (IOException e) {
			logger.error("err!");
			e.printStackTrace();
		}
		return newWordList;
	}

	private class Feature {
		char character;
		int tf;
		int nameHead;
		int pingyin;
		int nameEnd;

		@Override
		public String toString() {
			return String.join("\t", Character.toString(character));
		}
	}
}
