package crfModel;

import Feature.*;
import dataProcess.Corpus;
import evaluate.Test;
import evaluate.config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

/**
 * 新词发现当成分词问题做
 * 命名实体识别当成
 * Created by wan on 4/24/2017.
 */
public class CharacterCRF extends CRFModel {
	private static final Logger logger = LoggerFactory.getLogger(CharacterCRF.class);

	public static void main(String... args) {
		String al = "";
		if (args.length >0 ) {
			config.isCRFsuite = true;
			config.algorithm = args[0];
			al = args[0];
		}
		String[] corpus = new String[]{config.trainData};
		CharacterCRF characterCRF = new CharacterCRF();

		for (String type : config.supportedType) {
			//if (type != config.ns) continue;
			characterCRF.train(corpus, type);
			Test.test(Test.readWordList(Test.getAnswerFile(config.testDataInput, type)), characterCRF.detectNewWord
					(config.testDataInput, "tmp/tmp." + type, type), characterCRF.getClass().getSimpleName()
					+ "." + type + " " + al);
		}
	}

	public void convertSrc2TestInput(String[] inputFiles, String outputFile, String pattern) {
		logger.debug("convert {} to {} for {}", inputFiles, outputFile, pattern);
		try {
			PrintWriter writer = new PrintWriter(new FileWriter(outputFile));
			BufferedReader reader;
			String line;
			for (String inputFile : inputFiles) {
				reader = new BufferedReader(new FileReader(inputFile));

				while ((line = reader.readLine()) != null) {
					if (line.length() == 0) continue;
					List<String> features = CharacterFeature.getRes(line);
					for (String feature : features) {
						writer.println(feature + "\tN");
						if (getWord(feature).matches(config.sepSentenceRegex))// 断句
							writer.println();
					}
				}

			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void convert2TrainInput(String[] inputFiles, String pattern) {
		logger.debug("convert {} to {} for {}", inputFiles, trainData, pattern);
		try {
			PrintWriter writer = new PrintWriter(new FileWriter(trainData));
			for (String inputFile : inputFiles) {
				BufferedReader reader = new BufferedReader(new FileReader(inputFile));
				String tmp;

				if (pattern == config.nr || pattern == config.ns) {
					while ((tmp = reader.readLine()) != null) {
						if (tmp.trim().length() == 0) continue;
						String[] segs = tmp.split(config.sepWordRegex);
						int index = 0;
						List<String> features = CharacterFeature.getRes(tmp.replaceAll("/[^ ]+", "").replaceAll(" ",
								""));
						for (String seg : segs) {

							String word = config.removePos(seg);
							String pos = config.getPos(seg);
							if (word.length() == 1) {
								if (pos.equals(pattern))
									writer.println(String.format("%s\t%s", features.get(index++), label_single));
								else
									writer.println(String.format("%s\t%s", features.get(index++), label_other));

							} else {
								if (pos.equals(pattern)) {
									writer.println(String.format("%s\t%s", features.get(index++), label_begin));
									for (int i = 1; i < word.length() - 1; i++) {
										writer.println(String.format("%s\t%s", features.get(index++), label_meddle));
									}
									writer.println(String.format("%s\t%s", features.get(index++), label_end));
								} else {
									for (int i = 0; i < word.length(); i++)
										writer.println(String.format("%s\t%s", features.get(index++), label_other));
								}
							}
							if (word.matches(config.sepSentenceRegex))
								writer.println();
						}
						writer.println();
					}
				}// nr

				if (pattern == config.nw) {
					while ((tmp = reader.readLine()) != null) {
						tmp = tmp.trim();
						if (tmp.length() == 0) continue;
						String[] segs = tmp.split(config.sepWordRegex);
						int index = 0;
						List<String> features = CharacterFeature.getRes(tmp.replaceAll("/[^ /]+", "").replaceAll(" +",
								""));
						for (String seg : segs) {
							String word = config.removePos(seg);
							try {
								if (word.length() == 1) {
									writer.println(String.format("%s\t%s", features.get(index++), label_single));
								} else {
									writer.println(String.format("%s\t%s", features.get(index++), label_begin));
									for (int i = 1; i < word.length() - 1; i++)
										writer.println(String.format("%s\t%s", features.get(index++), label_meddle));
									writer.println(String.format("%s\t%s", features.get(index++), label_end));
								}
							} catch (IndexOutOfBoundsException e) {
								System.err.println("---" + word);
								System.err.println(index);
								System.err.println(tmp.replaceAll("/[^ /]+", "").replaceAll(" +", "").length());
								System.err.println(segs.length);
								System.err.println(tmp);
							}
							if (word.matches(config.sepSentenceRegex))
								writer.println();
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

	public Map<String, String> convertTestOuput2Res(String inputFile, String newWordFile, String pattern) {
		HashMap<String, String> newWordList = new HashMap<>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(inputFile));
			PrintWriter writer = new PrintWriter(new FileWriter(newWordFile));
			String line;

			if (pattern == config.nw) {
				while ((line = reader.readLine()) != null) {
					if (line.length() == 0)
						continue;
					StringBuilder wordBuffer = new StringBuilder();
					FieldAppender fieldAppender = new FieldAppender(line);
					wordBuffer.append(line.split("\t", 2)[0]);
					if (line.charAt(line.length() - 1) == label_begin) {
						do {
							line = reader.readLine();
							fieldAppender = new FieldAppender(line);
							wordBuffer.append(line.split("\t", 2)[0]);
						} while (line.length() > 0 && line.charAt(line.length() - 1) != label_end);
					}

					String word = wordBuffer.toString();// 这是一个词
					if (Corpus.isNewWord(word, null) && !newWordList.keySet().contains(word)) {
						newWordList.put(word, fieldAppender.toString());
						writer.println(word +"\t" + fieldAppender);
					}
				}
			} // nw

			if (pattern == config.nr || pattern == config.ns) {
				while ((line = reader.readLine()) != null) {
					if (line.length() == 0)
						continue;
					StringBuilder wordBuffer = new StringBuilder();
					FieldAppender fieldAppender = new FieldAppender(line);
					wordBuffer.append(line.split("\t", 2)[0]);
					char label_head = line.charAt(line.length() - 1);
					if (line.charAt(line.length() - 1) == label_begin) {
						do {
							line = reader.readLine();
							fieldAppender = new FieldAppender(line);
							wordBuffer.append(line.split("\t", 2)[0]);
						} while (line.length() > 0 && line.charAt(line.length() - 1) != label_end);
					}

					String word = wordBuffer.toString();
					if (label_head == label_begin || label_head == label_single) //单字名称 和 多字名称
						if (!newWordList.keySet().contains(word)) {
							newWordList.put(word, fieldAppender.toString());
							writer.println(word +"\t" + fieldAppender);
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
