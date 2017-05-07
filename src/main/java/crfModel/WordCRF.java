package crfModel;

import evaluate.Corpus;
import evaluate.Test;
import evaluate.config;
import org.ansj.domain.Term;
import org.ansj.splitWord.Analysis;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.ansj.util.MyStaticValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by don on 27/04/2017.
 */
public class WordCRF extends crfppWrapper implements Serializable {
	private static final Logger logger = LoggerFactory.getLogger(CharacterCRF.class);
	public static Analysis parser;

	static {
		//config.isLoadCorpus = true;
		Corpus.loadWordInfo();
		MyStaticValue.isRealName = true;// ansj不进行大小写转换
		MyStaticValue.isNumRecognition = true;
		MyStaticValue.isQuantifierRecognition = false;
		parser = new ToAnalysis();
	}

	public static void main(String... args) {
		String[] inputFiles = {config.trainData};
		WordCRF segementCRF = new WordCRF();
		segementCRF.train(inputFiles, config.nw);
		Test.test(Test.readWordList(config.getAnswerFile(config.testDataInput, config.nw)), segementCRF.detectNewWord(config.testDataInput,
				"tmp/tmp.nw", config.nw), segementCRF.getClass().getSimpleName());
	}

	/**
	 * 正确的单个词是s
	 * 新词是bme
	 * 标注分词后的文件，有可能某些新词不能由已分割的词合并出来
	 *
	 * @param inputFiles 同时设置mostHitInTrainData
	 */
	@Override
	public void convert2TrainInput(String[] inputFiles, String pattern) {
		logger.info("levelNum is {}", config.levelNum);
		BufferedReader reader;
		String srcline;
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(trainData));
			for (String inputFile : inputFiles) {
				reader = new BufferedReader(new FileReader(inputFile));
				while ((srcline = reader.readLine()) != null) {
					srcline = srcline.replaceAll("/([^ ]*|$)", "");// 去掉词性
					for (String line : srcline.split(config.sepSentenceRegex)) {
						line = line.trim();
						if (line.length() > 0) {
							String[] golden = line.split(config.sepWordRegex);
							List<Term> ansj = parser.parseStr(line.replace(" ", "")).getTerms();

							int k = 0;
							String gs = golden[0], as = "";
							char label = label_single;
							for (int i = 0; i < ansj.size(); i++) {// 总保证循环体开始之前 gs包含as, 且gs仅包含一个词，
								//if (gs.length() == 0) logger.debug("{}\n{}", i, line); //这句话还修了一个bug呢
								Term term = ansj.get(i);
								as += term.getRealName();
								if (gs.equals(as)) {
									if (gs.length() == term.getRealName().length()) // 正确的单个词
										label = label_single; // 正确的单个词3
									else
										label = label_end; // 新词结尾1
									as = "";
									if (k + 1 < golden.length) {
										gs = golden[++k];
									}
								} else {
									if (as.length() == term.getRealName().length())
										label = label_begin; // 新词开头0
									else
										label = label_meddle; // 新词中部2
									if (!gs.contains(as)) {
										//logger.debug("--- {} {} ！！！", as, gs);
										while (!gs.contains(as)) {
											gs += golden[++k];
											//	logger.debug("--- {} {} ！！！", as, gs);
										}
									}
									if (gs.equals(as)) {
										label = label_end;// 这个序列包含了多个词, 但是这个序列并不是新词
										as = "";
										if (k + 1 < golden.length) {
											gs = golden[++k];
										}
									}
								}
								if (i > 0)
									writer.append(new Feature(ansj.get(i - 1).getRealName(), term.getRealName(), term
											.getNatureStr()).toString
											() +
											'\t' + label);
								else
									writer.append(new Feature("", term.getRealName(), term
											.getNatureStr()).toString
											() +
											'\t' + label);

								writer.newLine();
							}
							writer.newLine();
						}
					}
				}
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void convertSrc2TestInput(String[] inputFiles, String crfppInput, String pattern) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(crfppInput));
			for (String inputFile : inputFiles) {
				BufferedReader reader = new BufferedReader(new FileReader(inputFile));
				String srcline;
				while ((srcline = reader.readLine()) != null) {
					for (String line : srcline.split(config.sepSentenceRegex)) {
						if (line.length() == 0) {
							writer.newLine();
							continue;
						}
						List<Term> list = parser.parseStr(line).getTerms();
						for (int i = 0; i < list.size(); i++) {
							Term term = list.get(i);
							if (i > 0)
								writer.append(new Feature(list.get(i - 1).getRealName(), term.getRealName(), term
										.getNatureStr()).toString
										());
							else
								writer.append(new Feature("", term.getRealName(), term.getNatureStr()).toString
										());
							writer.newLine();
						}
						writer.newLine();// todo 分割句子
					}
				}
			}
			writer.close();
		} catch (java.io.IOException e) {
			logger.error("count word using ansj error");
			e.printStackTrace();
		}
	}

	@Override
	public Set<String> convertTestOuput2Res(String crfppOutput, String resFile, String pattern) {
		HashSet<String> newWordList = new HashSet<>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(crfppOutput));
			BufferedWriter writerNewWord = new BufferedWriter(new FileWriter(resFile));
			String tmp, posOfFirstWord, wordPiece;
			while ((tmp = reader.readLine()) != null) {
				StringBuilder wordBuffer = new StringBuilder();
				if (tmp.length() == 0)
					continue;
				wordPiece = tmp.split("\t", 2)[0]; //第一个词
				wordBuffer.append(wordPiece);
				posOfFirstWord = tmp.split("\t", 4)[2];
				if (tmp.charAt(tmp.length() - 1) == label_begin) {
					do {
						//pos = "0";
						tmp = reader.readLine();
						wordPiece = tmp.split("\t", 2)[0];
						wordBuffer.append(wordPiece);
					} while (tmp.length() > 0 && tmp.charAt(tmp.length() - 1) != label_end);
				}
				String word = wordBuffer.toString();
				// todo 去掉末尾的
				//if (!wordPiece.matches(invalidSuffixRegex))
				if (Corpus.isNewWord(word) && !newWordList.contains(word)
						&& !posOfFirstWord.equals("m") && !posOfFirstWord.equals("t") // todo 不能以数量词开头
						) {
					//忽略量词
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
		return newWordList;
	}

	class Feature {
		String word;
		int length;
		String pos;
		int leftEntropy;
		int rightEntropy;
		int tf;
		int pmi;
		int tfWithPreWord;

		Feature(String preWord, String word, String pos) {
			this.word = word;
			length = word.length();
			if (length > config.maxNagaoLength) // 长度比较长的变短
				length = config.maxNagaoLength + 1;
			this.pos = pos;
			try {
				tf = Corpus.discreteWordInfo.getTF(word);
				pmi = Corpus.discreteWordInfo.getPMI(word);
				leftEntropy = Corpus.discreteWordInfo.getLE(word);
				rightEntropy = Corpus.discreteWordInfo.getRE(word);
			} catch (NullPointerException e) {
				//length = 0;
				tf = 0;
				pmi = 0;
				leftEntropy = config.levelNum;
				rightEntropy = config.levelNum;
			}
			tfWithPreWord = Corpus.discreteWordInfo.getTF(preWord + word);
		}

		@Override
		public String toString() {
			return String.join("\t",
					word,
					Integer.toString(length),
					pos,
					Integer.toString(leftEntropy),
					Integer.toString(rightEntropy),
					Integer.toString(tf),
					Integer.toString(pmi),
					Integer.toString(tfWithPreWord)
			);
		}
	}
}
