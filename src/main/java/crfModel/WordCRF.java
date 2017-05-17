package crfModel;

import ansj.Ansj;
import dataProcess.Corpus;
import dataProcess.WordInfoInCorpus;
import evaluate.Test;
import evaluate.config;
import org.ansj.domain.Term;
import org.ansj.splitWord.Analysis;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by don on 27/04/2017.
 */
public class WordCRF extends CRFModel implements Serializable {
	private static final Logger logger = LoggerFactory.getLogger(CharacterCRF.class);
	static private HashSet<String> wrong = new HashSet<>();
	private WordInfoInCorpus wordInfoInCorpus;
	public Analysis parser;

	{
		config.closeAnsj();
		parser = new ToAnalysis();
		parser.setIsNameRecognition(false);
		parser.setIsNumRecognition(true);
		parser.setIsQuantifierRecognition(false);
		Ansj.segFile(parser, config.totalDataInput, "ansj.txt");
		//wordInfoInCorpus =  new WordInfoInCorpus(config.corpusInput);
	}

	public static void main(String... args) {
		Test.clean();
		WordCRF tmp = new WordCRF();
		tmp.calcMostRecallInAnsj("data/test/test.txt.tagNW", config.nw);
		tmp.calcMostRecallInAnsj(config.testData, config.nr);
		tmp.calcMostRecallInAnsj(config.testData, config.ns);
		String[] inputFiles = {config.trainData};
		WordCRF segementCRF = new WordCRF();
		for (String type : config.supportedType) {//;= config.ns;
			if (type != config.nw) continue;
			segementCRF.train(inputFiles, type);
			Test.test(Test.readWordList(Test.getAnswerFile(config.testDataInput, type)), segementCRF.detectNewWord(config.testDataInput,
					"tmp/tmp." + type, type), segementCRF.getClass().getSimpleName() + " " + type);
		}
	}

	static void debug(int i, List<Term> ansj, int goldenIndex, String[] golden, String[] goldenTag, String gs) {
		//if (true) return;
		if (gs.matches(config.newWordExcludeRegex) || gs.matches(".*\\p{IsDigit}.*"))
			return;
		if (wrong.contains(gs))
			return;
		wrong.add(gs);
		System.err.println(gs);
		int j = i;
		while (!gs.startsWith(ansj.get(j).getRealName())) j--;
		for (int k = j; k <= i; k++)
			System.err.print(ansj.get(k).toString() + " ");
		System.err.println("  [ansj");
		j = goldenIndex;
		while (!gs.startsWith(golden[j])) j--;
		for (int k = j; k <= goldenIndex; k++)
			System.err.print(golden[k] + "/" + goldenTag[k] + " ");
		System.err.println("  [golden");
	}

	public void calcMostRecallInAnsj(String inputFile, String pattern) {
		BufferedReader reader;
		String srcline;
		double mostRecallInTraindata = 0;
		HashSet<String> newWordList = new HashSet<>();
		HashSet<String> validNewWordList = new HashSet<>();
		try {
			newWordList.addAll(Corpus.extractWord(inputFile, pattern));
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
						for (int i = 0; i < ansj.size(); i++) {// 总保证循环体开始之前 gs包含as, 且gs仅包含一个词，
							Term term = ansj.get(i);
							as += term.getRealName();
							if (gs.equals(as)) {
								if (newWordList.contains(gs))
									validNewWordList.add(gs);
								as = "";
								if (k + 1 < golden.length) {
									gs = golden[++k];
								}
							} else {
								if (!gs.contains(as)) {
									while (!gs.contains(as)) {
										gs += golden[++k];
									}
								}
								if (gs.equals(as)) {
									if (newWordList.contains(gs))
										validNewWordList.add(gs);
									as = "";
									if (k + 1 < golden.length) {
										gs = golden[++k];
									}
								}
							}
						}
					}
				}
			}
			mostRecallInTraindata = (double) validNewWordList.size() / newWordList.size();
			logger.info("valid{} total{} mostRecall is {} in {}", validNewWordList.size(), newWordList.size(),
					mostRecallInTraindata, pattern);
		} catch (IOException e) {
			e.printStackTrace();
		}
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
		wordInfoInCorpus =  new WordInfoInCorpus(Corpus.convertToSrc(inputFiles, "tmp/tmp"));// todo
		// 这个为了方便，可能有bug
		try {
			PrintWriter writer = new PrintWriter(new FileWriter(trainData));
			for (String inputFile : inputFiles) {
				reader = new BufferedReader(new FileReader(inputFile));

				while ((line = reader.readLine()) != null) {
					goldenSegWithoutTag = line.replaceAll("/[^ /]+", "");// 去掉词性
					srcline = goldenSegWithoutTag.replaceAll(" ", "");

					if (srcline.length() == 0) continue;// 不是空行
					String[] golden = goldenSegWithoutTag.split(config.sepWordRegex);
					String[] goldenTag = line.replaceAll("[^ ]+/", "").split(config.sepWordRegex);
					List<Term> ansj = parser.parseStr(srcline).getTerms();

					int goldenIndex = 0;
					String gs = golden[0], as = "";
					char label = label_single;
					for (int i = 0; i < ansj.size(); i++) {// 总保证循环体开始之前 gs包含且不等于as，
						//if (gs.length() == 0) logger.debug("{}\n{}", i, line); //这句话还修了一个bug呢
						Term term = ansj.get(i);
						String ansjWord = term.getRealName();
						as += ansjWord;


						if (pattern == config.nw) {
							//BEM S
							if (gs.equals(as)) {
								if (golden[goldenIndex].length() == ansjWord.length()) // 正确的单个词
									label = label_single; // 正确的单个词3
								else {
									label = label_end; // 新词结尾1 或者 未识别序列 结尾
									if (!gs.equals(golden[goldenIndex]))
										debug(i, ansj, goldenIndex, golden, goldenTag, gs);// for debug
								}
								as = "";
								if (goldenIndex + 1 < golden.length) {
									gs = golden[++goldenIndex];
								}
							} else {
								if (gs.contains(as)) {// gs还没被补全
									if (as.length() == ansjWord.length())
										label = label_begin; // 新词开头0
									else
										label = label_meddle; // 新词中部2
								} else { // gs被as包含了
									while (!gs.contains(as)) {
										gs += golden[++goldenIndex];
									}
									if (gs.equals(as)) {
										if (!gs.equals(golden[goldenIndex]))
											debug(i, ansj, goldenIndex, golden, goldenTag, gs);// for debug
										label = label_end;// 这个序列包含了多个词, 但是这个序列并不是新词
										as = "";
										if (goldenIndex + 1 < golden.length) {
											gs = golden[++goldenIndex];
										}
									}
								}
							}
						} // nw

						if (pattern == config.nr || pattern == config.ns) {
							//BEM S O
							if (gs.equals(as)) {
								if (gs.length() == golden[goldenIndex].length() && goldenTag[goldenIndex].matches(pattern)) {
									if (golden[goldenIndex].length() == ansjWord.length()) // 正确的单个词
										label = label_single; // 正确的单个词3
									else
										label = label_end; // 新词结尾1 或者
									if (!gs.equals(golden[goldenIndex]))
										debug(i, ansj, goldenIndex, golden, goldenTag, gs);// for debug
								} else
									label = label_other;
								as = "";
								if (goldenIndex + 1 < golden.length) {
									gs = golden[++goldenIndex];
								}
							} else {
								if (gs.contains(as)) {// gs还没被补全
									if (gs.length() == golden[goldenIndex].length() && goldenTag[goldenIndex].matches(pattern)) {
										if (as.length() == ansjWord.length())
											label = label_begin; // 新词开头0
										else
											label = label_meddle; // 新词中部2
									} else
										label = label_other;
								} else { // gs被as包含了
									while (!gs.contains(as)) {
										gs += golden[++goldenIndex];
									}
									if (gs.equals(as)) {
										label = label_other;// 这个序列包含了多个词, 但是这个序列并不是新词
										as = "";
										if (goldenIndex + 1 < golden.length) {
											gs = golden[++goldenIndex];
										}
									}
								}
							}
						}// nr ns

						if (i > 0)
							writer.println(new Feature(ansj.get(i - 1).getRealName(), ansjWord, term.getNatureStr()).toString() + '\t' + label);
						else
							writer.println(new Feature("", ansjWord, term.getNatureStr()).toString() + '\t' + label);

						if (ansjWord.matches(config.sepSentenceRegex))
							writer.println();// 断句换行
					} //处理分词结果的每个词
					writer.println();
				} // 每一段


			}
			logger.debug("wrong size {}", wrong.size());
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void convertSrc2TestInput(String[] inputFiles, String crfppInput, String pattern) {
		try {
			wordInfoInCorpus =  new WordInfoInCorpus(inputFiles[0]);// todo 这个为了方便，可能有bug
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
							writer.append(new Feature(list.get(i - 1).getRealName(), word, pos).toString() + "\tN");
						else
							writer.append(new Feature("", word, pos).toString() +"\tN");
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
		try (
				BufferedReader reader = new BufferedReader(new FileReader(crfppOutput));
				PrintWriter writerNewWord = new PrintWriter(new FileWriter(resFile));
				PrintWriter writerWordInfo = new PrintWriter(new FileWriter(resFile + ".seginfo"));
		) {
			String line, posOfFirstWord, wordPiece, posOfLastWord, posSeq, wordSeq;
			char labelOfFirstWord = 0;

			while ((line = reader.readLine()) != null) {
				if (line.length() == 0)// 跳过空行
					continue;

				StringBuilder wordBuffer = new StringBuilder();
				WordInfoAppender wordInfo = new WordInfoAppender(line);

				wordPiece = getWord(line); //第一个词
				labelOfFirstWord = getLabel(line);
				posSeq = line.split("\t", 4)[2];
				wordSeq = wordPiece;

				wordBuffer.append(wordPiece);

				if (getLabel(line) == label_begin) {
					do {
						line = reader.readLine();
						if (line.length() == 0) break;
						wordPiece = getWord(line);
						posSeq += "+" + line.split("\t", 4)[2];
						wordSeq += " " + wordPiece;
						wordBuffer.append(wordPiece);
						wordInfo.append(line);
					} while (getLabel(line) != label_end);
				}
				String word = wordBuffer.toString();


				//List<Term> check = checker.parseStr(word).getTerms();
				if (pattern == config.nw) {
					if (Corpus.isNewWord(word, posSeq) && !newWordList.contains(word)
							&& !(posSeq.matches("(m\\+q)|(m)")) // todo 不能以数量词开头
							) {
						//忽略量词
						newWordList.add(word);
						writerNewWord.println(word);
						writerWordInfo.println(wordInfo);
					}
				} // nw

				if (pattern == config.nr || pattern == config.ns) {
					if (labelOfFirstWord == label_single || labelOfFirstWord == label_begin)
						if (!newWordList.contains(word)) {
							newWordList.add(word);
							writerNewWord.println(word);
							writerWordInfo.println(wordInfo);
						}
				} // nr ns
			}

			writerNewWord.close();
			writerWordInfo.close();
		} catch (IOException e) {
			logger.error("err!");
			e.printStackTrace();
		}
		return newWordList;
	}

	class WordInfoAppender {
		StringBuilder[] stringBuilders;

		WordInfoAppender(String line) {
			String[] tmp = line.split("\t");
			stringBuilders = new StringBuilder[tmp.length];
			for (int i = 0; i < tmp.length; i++) {
				stringBuilders[i] = new StringBuilder();
				stringBuilders[i].append(tmp[i]);
			}
		}

		void append(String line) {
			String[] tmp = line.split("\t");
			for (int i = 0; i < tmp.length; i++) {
				stringBuilders[i].append("/");
				stringBuilders[i].append(tmp[i]);
			}
		}

		@Override
		public String toString() {
			StringBuilder tmp = new StringBuilder();
			for (int i = 0; i < stringBuilders.length; i++) {
				tmp.append(stringBuilders[i]);
				tmp.append("  ");
			}
			return tmp.toString();
		}
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
			if (length > config.maxStringLength) // 长度比较长的变短
				length = config.maxStringLength + 1;
			this.pos = pos;
			try {
				tf = wordInfoInCorpus.discreteWordInfo.getTF(word);
				pmi = wordInfoInCorpus.discreteWordInfo.getPMI(word);
				leftEntropy = wordInfoInCorpus.discreteWordInfo.getLE(word);
				rightEntropy = wordInfoInCorpus.discreteWordInfo.getRE(word);
			} catch (NullPointerException e) {
				//length = 0;
				tf = 0;
				pmi = 0;
				leftEntropy = config.levelNum;
				rightEntropy = config.levelNum;
			}
			tfWithPreWord = wordInfoInCorpus.discreteWordInfo.getTF(preWord + word);
		}

		@Override
		public String toString() {
			return String.join("\t",
					word,
					Integer.toString(length),
					pos,
					Integer.toString(tf),
					Integer.toString(pmi),
					Integer.toString(leftEntropy),
					Integer.toString(rightEntropy),
					Integer.toString(tfWithPreWord)
					//word.matches("^型$") ? "T": "F"
			);
		}
	}
}
