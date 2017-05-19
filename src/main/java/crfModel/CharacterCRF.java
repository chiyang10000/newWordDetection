package crfModel;

import Feature.CharacterFeature;
import evaluate.Ner;
import evaluate.Test;
import evaluate.config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;

/**
 * 新词发现当成分词问题做
 * 命名实体识别当成
 * Created by wan on 4/24/2017.
 */
public class CharacterCRF extends CRFModel {
	private static final Logger logger = LoggerFactory.getLogger(CharacterCRF.class);

	public static void main(String... args) {
		String al = "";
		if (args.length > 0) {
			config.isCRFsuite = true;
			config.algorithm = args[0];
			al = args[0];
		}
		String[] corpus = new String[]{config.trainData};
		CharacterCRF characterCRF = new CharacterCRF();

		for (Ner ner : Ner.supported) {
			//if (ner != Ner.nw) continue;
			characterCRF.train(corpus, ner);
			Test.test(Test.readWordList(config.getAnswerFile(config.testDataInput, ner)),
					characterCRF.detectNewWord (config.testDataInput, "tmp/tmp." + ner, ner),
					characterCRF.getClass().getSimpleName() + "." + ner.pattern + " " + al);
		}
	}

	@Override
	public void convertSrc2TestInput(String[] inputFiles, String outputFile, Ner ner) {
		logger.debug("convert {} to {} for {}", inputFiles, outputFile, ner.pattern);
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

	@Override
	public void convert2TrainInput(String[] inputFiles, Ner ner) {
		logger.debug("convert {} to {} for {}", inputFiles, trainData, ner.pattern);
		try {
			PrintWriter writer = new PrintWriter(new FileWriter(trainData));
			for (String inputFile : inputFiles) {
				BufferedReader reader = new BufferedReader(new FileReader(inputFile));
				String tmp;

				if (ner != ner.nw) {
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
								if (pos.contains(ner.pattern))
									writer.println(String.format("%s\t%s", features.get(index++), ner.label + label_single));//bio 还是bemsio
								else
									writer.println(String.format("%s\t%s", features.get(index++), label_other));

							} else {
								if (pos.contains(ner.pattern)) {
									writer.println(String.format("%s\t%s", features.get(index++), ner.label + label_begin));
									for (int i = 1; i < word.length() - 1; i++) {
										writer.println(String.format("%s\t%s", features.get(index++), ner.label + label_meddle));// bio 还是bemsio
									}
									writer.println(String.format("%s\t%s", features.get(index++), ner.label + label_end));
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

				if (ner == ner.nw) {
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
