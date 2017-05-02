package crfModel;

import Config.Config;
import NagaoAlgorithm.NagaoAlgorithm;
import NagaoAlgorithm.TFNeighbor;
import evaluate.Corpus;
import org.ansj.domain.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashSet;
import java.util.List;

/**
 * Created by don on 27/04/2017.
 */
public class SegementationCRF extends crfppWrapper implements Serializable {
	private static final Logger logger = LoggerFactory.getLogger(SingleCharacterCRF.class);
	public NagaoAlgorithm nagao;
	public double mostRecallInTraindata;

	/**
	 * @param corpusFiles 没有分词信息的原始文件, 作为统计词频和信息熵的语料库
	 */
	public SegementationCRF(String... corpusFiles) {

		if (Config.isNagaoLoadedFromFile) {
			nagao = NagaoAlgorithm.loadFromFile();
		} else {
			nagao = new NagaoAlgorithm(Config.maxNagaoLength);
			nagao.scan(corpusFiles);
			nagao.countTFNeighbor(null);
			nagao.calcDiscreteTFNeighbor(nagao.wordTFNeighbor.keySet(), Config.levelNum);
			if (Config.isNagaoSavedIntoFile)
				nagao.saveIntoFile();
		}

		template = "data/crf-template/SegmentCRF.template";
		model = "data/model/SegmentCRF.model";
		trainData = "tmp/SegmentCRF.crf";
	}

	public static void main(String... args) {
		String[] inputFiles = {"data/raw/train.txt"};
		SegementationCRF segementCRF = new SegementationCRF("data/test/train.txt.src", "data/test/test.txt.src");
		//segementCRF.convert2TrainInput(inputFiles);
		segementCRF.train(inputFiles);
		//segementCRF.convertSrc2TestInput(new String[]{"data/test/test.txt.src"}, "tmp/xxx.txt");
	}

	/**
	 * 正确的单个词是2
	 * 新词是0 3 1
	 * 标注分词后的文件，有可能某些新词不能由已分割的词合并出来
	 *
	 * @param inputFiles 同时设置mostHitInTrainData
	 */
	@Override
	public void convert2TrainInput(String[] inputFiles) {
		logger.info("levelNum is {}", Config.levelNum);
		BufferedReader reader;
		String srcline;
		mostRecallInTraindata = 0;
		HashSet<String> newWordList = new HashSet<>();
		HashSet<String> validNewWordList = new HashSet<>();
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(trainData));
			for (String inputFile : inputFiles) {
				newWordList.addAll(Corpus.extractNewWordNotInCorpus(inputFile));
				reader = new BufferedReader(new FileReader(inputFile));
				while ((srcline = reader.readLine()) != null) {
					srcline = srcline.replaceAll("/([^ ]*|$)", "");// 去掉词性
					for (String line : srcline.split(Config.sepSentenceRegex)) {
						line = line.trim();
						if (line.length() > 0) {
							String[] golden = line.split(Config.sepWordRegex);
							List<Term> ansj = Config.parser.parseStr(line.replace(" ", "")).getTerms();

							int k = 0;
							String gs = golden[0], as = "";
							int label = 1;
							for (int i = 0; i < ansj.size(); i++) {// 总保证循环体开始之前 gs包含as, 且gs仅包含一个词，
								//if (gs.length() == 0) logger.debug("{}\n{}", i, line); //这句话还修了一个bug呢
								Term term = ansj.get(i);
								as += term.getRealName();
								if (gs.equals(as)) {
									if (newWordList.contains(gs))
										validNewWordList.add(gs);
									if (gs.length() == term.getRealName().length()) // 正确的单个词
										label = 1; // 正确的单个词2
									else
										label = 1; // 新词结尾1
									as = "";
									if (k + 1 < golden.length) {
										gs = golden[++k];
									}
								} else {
									if (as.length() == term.getRealName().length())
										label = 0; // 新词开头0
									else
										label = 0; // 新词中部3
									if (!gs.contains(as)) {
										//logger.debug("--- {} {} ！！！", as, gs);
										while (!gs.contains(as)) {
											gs += golden[++k];
											//	logger.debug("--- {} {} ！！！", as, gs);
										}
									}
									if (gs.equals(as)) {
										if (newWordList.contains(gs))
											validNewWordList.add(gs);
										label = 1;// 这个序列包含了多个词, 但是这个序列并不是新词
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
			mostRecallInTraindata = (double) validNewWordList.size() / newWordList.size();
			logger.info("invaid{} total{}", validNewWordList.size(), newWordList.size());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void convertSrc2TestInput(String[] inputFiles, String crfppInput) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(crfppInput));
			for (String inputFile : inputFiles) {
				BufferedReader reader = new BufferedReader(new FileReader(inputFile));
				String srcline;
				while ((srcline = reader.readLine()) != null) {
					for (String line : srcline.split(Config.sepSentenceRegex)) {
						if (line.length() == 0) {
							writer.newLine();
							continue;
						}
						List<Term> list = Config.parser.parseStr(line).getTerms();
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

	/**
	 * @param inputFile 已分词文件
	 */
	void calcMostHits(String inputFile) {

	}

	@Override
	public void convertTestOuput2Res(String crfppOutput, String resFile) {
		HashSet<String> newWordList = new HashSet<>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(crfppOutput));
			BufferedWriter writerNewWord = new BufferedWriter(new FileWriter(resFile));
			String tmp, pos, wordPiece;
			while ((tmp = reader.readLine()) != null) {
				StringBuilder wordBuffer = new StringBuilder();
				if (tmp.length() == 0)
					continue;
				wordPiece = tmp.split("\t", 2)[0];
				wordBuffer.append(wordPiece);
				pos = tmp.split("\t", 4)[2];
				if (tmp.charAt(tmp.length() - 1) == '0') {
					do {
						//pos = "0";
						tmp = reader.readLine();
						wordPiece = tmp.split("\t", 2)[0];
						wordBuffer.append(wordPiece);
					} while (tmp.length() > 0 && tmp.charAt(tmp.length() - 1) != '1');
				}
				String word = wordBuffer.toString();
				// todo 去掉末尾的
				//if (!wordPiece.matches(Config.invalidSuffixRegex))
				if (Corpus.isNewWord(word) && !newWordList.contains(word)
					///			&& !pos.equals("m") // todo 不能以数量词开头
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
	}

	class Feature {
		String word;
		int length;
		String pos;
		int leftEntropy;
		int rightEntropy;
		int tf;
		int mi;
		int tfWithPreWord;

		Feature(String preWord, String word, String pos) {
			this.word = word;
			length = word.length();
			if (length > Config.maxNagaoLength)
				length = Config.maxNagaoLength + 1;
			this.pos = pos;
			try {
				TFNeighbor tfNeighbor = nagao.wordTFNeighbor.get(word);
				leftEntropy = nagao.discreteTFNeighbor.getLE(tfNeighbor.getLeftNeighborEntropy());
				rightEntropy = nagao.discreteTFNeighbor.getRE(tfNeighbor.getRightNeighborEntropy());
				tf = nagao.discreteTFNeighbor.getTF(tfNeighbor.getTF());
				mi = nagao.discreteTFNeighbor.getMI(nagao.countMI(word));
			} catch (NullPointerException e) {
				//length = 0;
				tf = 0;
				mi = 0;
				leftEntropy = Config.levelNum;
				rightEntropy = Config.levelNum;
			}
			tfWithPreWord = nagao.discreteTFNeighbor.getTF(nagao.getTF(preWord + word));
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
					Integer.toString(mi),
					Integer.toString(tfWithPreWord)
			);
		}
	}
}
