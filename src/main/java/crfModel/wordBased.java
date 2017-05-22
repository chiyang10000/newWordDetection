package crfModel;

import ansj.Ansj;
import com.github.stuxuhai.jpinyin.PinyinException;
import com.github.stuxuhai.jpinyin.PinyinFormat;
import com.github.stuxuhai.jpinyin.PinyinHelper;
import dataProcess.Corpus;
import dataProcess.WordInfoInCorpus;
import dataProcess.posPattern;
import evaluate.Ner;
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

/**
 * Created by don on 27/04/2017.
 */
public class wordBased extends CRFModel implements Serializable {
	private static final Logger logger = LoggerFactory.getLogger(charBased.class);
	static private HashSet<String> wrong = new HashSet<>();
	private WordInfoInCorpus wordInfoInCorpus;
	public Analysis parser;

	{
		parser = new ToAnalysis();
		parser.setIsNameRecognition(false);
		parser.setIsNumRecognition(false);
		parser.setIsQuantifierRecognition(false);
		Ansj.segFile(parser, config.getInputFile(config.totalData), "tmp/wordCRF.ansj.txt");
	}

	public static void main(String... args) {
		Ner.calcOOV();
		if (args.length > 0) {
			config.isCRFsuite = true;
			config.algorithm = args[0] + config.algorithm;
		}
		String[] corpus = new String[]{config.trainData};
		Test.clean();
		wordBased wordBased = new wordBased();
		//config.wordInfoInCorpus_total = new WordInfoInCorpus(config.totalDataInput);
		if (config.trainModel.contains(Ner.ner.name))
			wordBased.train(corpus, Ner.ner);
		String data = config.testData;
		for (Ner ner : Ner.supported) {//;= config.ns;
			if (config.trainModel.contains(ner.name))
				wordBased.train(corpus, ner);
			if (!config.testModel.contains(ner.name))
				continue;
			wordBased.calcMostRecallInAnsj(config.testData, ner);
			Test.test(Test.readWordList(config.getAnswerFile(data, ner)),
					wordBased.detectNewWord(config.getInputFile(data), "tmp/tmp." + ner.name, ner),
					ner, wordBased.getClass().getSimpleName(), (config.isCRFsuite ? "ap" : "crf")
			);
		}
	}

	static void debug(int i, List<Term> ansj, int goldenIndex, String[] golden, String[] goldenTag, String gs) {
		if (true) return;
		if (gs.matches(config.newWordExcludeRegex))
			return;
		if (wrong.contains(gs))
			return;
		wrong.add(gs);
		logger.warn(gs);
		int j = i;
		StringBuffer buffer = new StringBuffer();
		while (!gs.startsWith(ansj.get(j).getRealName())) j--;
		for (int k = j; k <= i; k++)
			buffer.append(ansj.get(k).toString() + " ");
		logger.warn("{}  [ansj", buffer);
		buffer = new StringBuffer();
		j = goldenIndex;
		while (!gs.startsWith(golden[j])) j--;
		for (int k = j; k <= goldenIndex; k++)
			buffer.append(golden[k] + "/" + goldenTag[k] + " ");
		logger.warn("{}  [golden", buffer);
	}

	public void calcMostRecallInAnsj(String inputFile, Ner ner) {
		BufferedReader reader;
		String srcline;
		double mostRecallInTraindata = 0;
		HashSet<String> newWordList = new HashSet<>();
		HashSet<String> validNewWordList = new HashSet<>();
		try {
			newWordList.addAll(Test.readWordList(config.getAnswerFile(inputFile, ner)).keySet());
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
					mostRecallInTraindata, ner.pattern);
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
	public void convert2TrainInput(String[] inputFiles, Ner ner) {
		logger.info("levelNum is {}", config.levelNum);
		BufferedReader reader;
		String line, goldenSegWithoutTag, srcline;
		wordInfoInCorpus = new WordInfoInCorpus(Corpus.convertToSrc(inputFiles, "tmp/tmp.train"));// todo
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
					String label = label_single;
					for (int i = 0; i < ansj.size(); i++) {// 总保证循环体开始之前 gs包含且不等于as，
						//if (gs.length() == 0) logger.debug("{}\n{}", i, line); //这句话还修了一个bug呢
						Term term = ansj.get(i);
						String ansjWord = term.getRealName();
						as += ansjWord;


						if (ner == ner.nw) {
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

						if (ner != ner.nw) {
							//BEM S O
							if (gs.equals(as)) {
								if (gs.length() == golden[goldenIndex].length() && goldenTag[goldenIndex].matches(ner
										.pattern)) {
									if (golden[goldenIndex].length() == ansjWord.length()) // 正确的单个词
										label = goldenTag[goldenIndex] + label_single; // 正确的单个词3
									else
										label = goldenTag[goldenIndex] + label_end; // 新词结尾1
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
									if (gs.length() == golden[goldenIndex].length() && goldenTag[goldenIndex].matches
											(ner.pattern)) {
										if (as.length() == ansjWord.length())
											label = goldenTag[goldenIndex] + label_begin; // 新词开头0
										else
											label = goldenTag[goldenIndex] + label_meddle; // 新词中部2
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

						String prePos = "^", preprePos = "^", preWord = "";
						if (i >= 2)
							preprePos = ansj.get(i - 2).getNatureStr();
						if (i >= 1) {
							prePos = ansj.get(i - 1).getNatureStr();
							preWord = ansj.get(i - 1).getRealName();
						}
						writer.println(new WordFeature(preWord, ansjWord, preprePos, prePos, term.getNatureStr())
								.toString() + '\t' + label);

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
	public void convertSrc2TestInput(String[] inputFiles, String crfppInput, Ner ner) {
		try {
			wordInfoInCorpus = new WordInfoInCorpus(inputFiles[0]);// todo 这个为了方便，可能有bug
			PrintWriter writer = new PrintWriter(new FileWriter(crfppInput));

			for (String inputFile : inputFiles) {
				BufferedReader reader = new BufferedReader(new FileReader(inputFile));
				String line;
				while ((line = reader.readLine()) != null) {

					if (line.length() == 0) {
						writer.println();
						continue;
						// 空行
					}

					List<Term> ansj = parser.parseStr(line).getTerms();

					for (int i = 0; i < ansj.size(); i++) {
						Term term = ansj.get(i);
						String word = term.getRealName();
						String pos = term.getNatureStr();
						String prePos = "^", preprePos = "^", preWord = "";
						if (i >= 2)
							preprePos = ansj.get(i - 2).getNatureStr();
						if (i >= 1) {
							prePos = ansj.get(i - 1).getNatureStr();
							preWord = ansj.get(i - 1).getRealName();
						}
						writer.println(
								new WordFeature(preWord, word, preprePos, prePos, pos).toString() + "\tN");

						if (word.matches(config.sepSentenceRegex))
							writer.println();// 句子断开
					}
					writer.println();// todo 分割句子
				}
			}
			writer.close();
		} catch (java.io.IOException e) {
			logger.error("count word using ansj error");
			e.printStackTrace();
		}
	}

	class WordFeature {
		String word;
		int length;
		String pos;
		int leftEntropy;
		int rightEntropy;
		int tf;
		int pmi;
		int tfWithPreWord;
		String posWindow;

		WordFeature(String preWord, String word, String preprePos, String prePos, String Pos) {
			this.word = word;
			if (Pos.equals("null"))
				Pos = "*";
			if (prePos.equals("null"))
				prePos = "*";
			if (preprePos.equals("null"))
				preprePos = "*";
			posWindow = String.join("/", preprePos.substring(0, 1), prePos.substring(0, 1), Pos.substring(0, 1));
			length = word.length();
			if (length > config.maxStringLength) // 长度比较长的变短
				length = config.maxStringLength + 1;
			this.pos = Pos;
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
			String pinying = "";
			try {
				pinying = PinyinHelper.convertToPinyinString(word, "", PinyinFormat.WITHOUT_TONE);
			} catch (PinyinException e) {
				e.printStackTrace();
			}
			return String.join("\t",
					word,
					Integer.toString(length),
					pos,
					Integer.toString(tf),
					Integer.toString(pmi),
					Integer.toString(leftEntropy),
					Integer.toString(rightEntropy),
					Integer.toString(tfWithPreWord),
					posPattern.renminribao.isDefined(posWindow) ? "T" : "F",
					pinying
			);
		}
	}
}