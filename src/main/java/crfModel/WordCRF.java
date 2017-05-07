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
		String type = config.nw;
		segementCRF.train(inputFiles, type);
		Test.test(Test.readWordList(Test.getAnswerFile(config.testDataInput, type)), segementCRF.detectNewWord(config.testDataInput,
				"tmp/tmp.nw", type), segementCRF.getClass().getSimpleName());
		/*
		Test.test(Test.readWordList(Test.getAnswerFile(config.testDataInput, config.nr)), segementCRF.detectNewWord(config.testDataInput,
				"tmp/tmp.nw", type), segementCRF.getClass().getSimpleName());
		Test.test(Test.readWordList(Test.getAnswerFile(config.testDataInput, config.ns)), segementCRF.detectNewWord(config.testDataInput,
				"tmp/tmp.nw", type), segementCRF.getClass().getSimpleName());
		*/
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
		String line, goldenSegWithoutTag, srcline;
		try {
			PrintWriter writer = new PrintWriter(new FileWriter(trainData));
			for (String inputFile : inputFiles) {
				reader = new BufferedReader(new FileReader(inputFile));

				if (pattern == config.nw) {
					while ((line = reader.readLine()) != null) {
						goldenSegWithoutTag = line.replaceAll("/([^ ]*|$)", "");// 去掉词性
						srcline = goldenSegWithoutTag.replaceAll(" ", "");

						if (srcline.length() == 0) continue;// 不是空行
						String[] golden = goldenSegWithoutTag.split(config.sepWordRegex);
						List<Term> ansj = parser.parseStr(srcline).getTerms();

						int goldenIndex = 0;
						String gs = golden[0], as = "";
						char label = label_single;
						for (int i = 0; i < ansj.size(); i++) {// 总保证循环体开始之前 gs包含且不等于as，
							//if (gs.length() == 0) logger.debug("{}\n{}", i, line); //这句话还修了一个bug呢
							Term term = ansj.get(i);
							String ansjWord = term.getRealName();
							as += ansjWord;
							if (gs.equals(as)) {
								if (gs.length() == ansjWord.length()) // 正确的单个词
									label = label_single; // 正确的单个词3
								else
									label = label_end; // 新词结尾1
								as = "";
								if (goldenIndex + 1 < golden.length) {
									gs = golden[++goldenIndex];
								}
							} else {
								if (as.length() == ansjWord.length())
									label = label_begin; // 新词开头0
								else
									label = label_meddle; // 新词中部2
								if (!gs.contains(as)) {
									while (!gs.contains(as)) {
										gs += golden[++goldenIndex];
									}
									if (gs.equals(as)) {
										label = label_end;// 这个序列包含了多个词, 但是这个序列并不是新词
										as = "";
										if (goldenIndex + 1 < golden.length) {
											gs = golden[++goldenIndex];
										}
									}
								}

							}
							if (i > 0)
								writer.println(new Feature(ansj.get(i - 1).getRealName(), ansjWord, term.getNatureStr()).toString() + '\t' + label);
							else
								writer.println(new Feature("", ansjWord, term.getNatureStr()).toString() + '\t' + label);

							if (ansjWord.matches(config.sepSentenceRegex))
								writer.println();// 断句换行
						}
						writer.println();
					}
				}// nw

				if (pattern == config.nr || pattern == config.ns) {
					// todo
				} // nr ns
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
				String line;
				while ((line = reader.readLine()) != null) {

					if (line.length() == 0) {
						writer.newLine();
						continue;
						// 空行
					}

					List<Term> list = parser.parseStr(line).getTerms();

					for (int i = 0; i < list.size(); i++) {
						Term term = list.get(i);
						String word = term.getRealName();
						String pos = term.getNatureStr();

						if (i > 0)
							writer.append(new Feature(list.get(i - 1).getRealName(), word, pos).toString());
						else
							writer.append(new Feature("", word, pos).toString());
						writer.newLine();

						if (word.matches(config.sepSentenceRegex))
							writer.newLine();// 句子断开
					}
					writer.newLine();// todo 分割句子
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
			if (pattern == config.nw) {
				while ((tmp = reader.readLine()) != null) {
					StringBuilder wordBuffer = new StringBuilder();
					if (tmp.length() == 0)// 跳过空行
						continue;
					wordPiece = tmp.split("\t", 2)[0]; //第一个词
					wordBuffer.append(wordPiece);
					posOfFirstWord = tmp.split("\t", 4)[2];
					if (tmp.charAt(tmp.length() - 1) == label_begin) {
						do {
							//pos = "0";
							tmp = reader.readLine();
							wordPiece = getWord(tmp);
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
			} // nw

			if (pattern == config.nr || pattern == config.ns) {
				while ((tmp = reader.readLine()) != null) {
					StringBuilder wordBuffer = new StringBuilder();
					if (tmp.length() == 0)
						continue;
					wordPiece = getWord(tmp); //第一个词
					wordBuffer.append(wordPiece);
					char label_head = getLabel(tmp);
					if (tmp.charAt(tmp.length() - 1) == label_begin) {
						do {
							tmp = reader.readLine();
							wordPiece = getWord(tmp);
							wordBuffer.append(wordPiece);
						} while (tmp.length() > 0 && tmp.charAt(tmp.length() - 1) != label_end);
					}
					String word = wordBuffer.toString();
					// todo 去掉末尾的
					//if (!wordPiece.matches(invalidSuffixRegex))
					if (label_head == label_single || label_head == label_begin
							) {
						newWordList.add(word);
						writerNewWord.append(word);
						writerNewWord.newLine();
					}
				}
			} // nr ns

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
					Integer.toString(tf),
					Integer.toString(leftEntropy),
					Integer.toString(rightEntropy),
					Integer.toString(pmi),
					Integer.toString(tfWithPreWord)
			);
		}
	}
}
