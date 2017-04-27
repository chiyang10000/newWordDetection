package crfModel;

import Config.Config;
import NagaoAlgorithm.NagaoAlgorithm;
import NagaoAlgorithm.TFNeighbor;
import evaluate.Corpus;
import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.BaseAnalysis;
import org.ansj.util.MyStaticValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashSet;
import java.util.List;

/**
 * Created by don on 27/04/2017.
 */
public class SegementCRF extends crfppWrapper {
	private static final Logger logger = LoggerFactory.getLogger(SingleCharacterCRF.class);
	NagaoAlgorithm nagao;
	HashSet<String> wordList;

	/**
	 * @param corpusFiles 没有分词信息的原始文件
	 */
	public SegementCRF(String... corpusFiles) {
		nagao = new NagaoAlgorithm(9);
		nagao.scan(corpusFiles);

		wordList = new HashSet<>();
		MyStaticValue.isRealName = true;
		try {
			for (String corpusFile : corpusFiles) {
				BufferedReader reader = new BufferedReader(new FileReader(corpusFile));
				String line;
				while ((line = reader.readLine()) != null) {
					List<Term> list = BaseAnalysis.parse(line).getTerms();
					for (Term term : list) {
						if (!term.getRealName().matches(Config.sepSentenceRegex))
							wordList.add(term.getRealName());
					}
				}
			}
		} catch (java.io.IOException e) {
			logger.error("count word using ansj error");
			e.printStackTrace();
		}

		nagao.countTFNeighbor(null);
		nagao.calcDiscreteTFNeighbor(wordList, 10);
		template = "data/crf-template/SegmentCRF.template";
		model = "data/model/SegmentCRF.model";
		trainData = "tmp/SegmentCRF.crf";
	}

	public static void main(String... args) {
		String[] inputFiles = {"data/raw/train.txt"};
		SegementCRF segementCRF = new SegementCRF("data/test/train.txt.src", "data/test/test.txt.src");
		segementCRF.convert2TrainInput(inputFiles, segementCRF.trainData);
		segementCRF.train();
		//segementCRF.convertSrc2TestInput(new String[]{"data/test/test.txt.src"}, "tmp/xxx.txt");
	}

	/**
	 *
	 * @param inputFiles
	 * @param trainFile
	 */
	@Override
	public void convert2TrainInput(String[] inputFiles, String trainFile) {
		BufferedReader reader;
		String line;
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(trainFile));
			for (String inputFile: inputFiles){
				reader = new BufferedReader(new FileReader(inputFile));
				while ( (line = reader.readLine()) != null) {
					String tmp = line.replaceAll("/([^ ]*|$)", "");
					String[] golden = tmp.split(" ");
					List<Term> ansj = BaseAnalysis.parse(tmp.replace(" ", "")).getTerms();

					int k = 0;
					String gs = golden[0], as = "";
					int label = 1;
					for (Term term: ansj) {
						as += term.getRealName();
						if (gs.equals(as)) {
							label = 1;
							as = "";
							if (k + 1 < golden.length)
							gs = golden[++k];
						}
						else {
							label = 0;
							//logger.debug("xxx {} {} ！！！", as, gs);
							while (!gs.contains(as)) {
								gs += golden[++k];
								//logger.debug("--- {} {} ！！！", as, gs);
							}
							if (gs.equals(as)) {
								label = 1;
								as = "";
								if (k + 1 < golden.length)
									gs = golden[++k];
							}
						}
						writer.append(new Feature(term.getRealName(), term.getNatureStr()).toString() + '\t' + label);
						writer.newLine();
					}
					writer.newLine();
				}
			}
			writer.close();
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
				String line;
				while ((line = reader.readLine()) != null) {
					if (line.length() == 0) {
						writer.newLine();
						continue;
					}
					List<Term> list = BaseAnalysis.parse(line).getTerms();
					for (Term term : list) {
						writer.append(new Feature(term.getRealName(), term.getNatureStr()).toString());
						writer.newLine();
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
	public void convertTestOuput2Res(String crfppOutput, String resFile) {
		HashSet<String> newWordList = new HashSet<>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(crfppOutput));
			BufferedWriter writerNewWord = new BufferedWriter(new FileWriter(resFile));
			String tmp;
			while ((tmp = reader.readLine()) != null) {
				StringBuilder wordBuffer = new StringBuilder();
				if (tmp.length() == 0)
					continue;
				wordBuffer.append(tmp.split("\t", 2)[0]);
				if (tmp.charAt(tmp.length() - 1) == '0') {
					do {
						tmp = reader.readLine();
						wordBuffer.append(tmp.split("\t", 2)[0]);
					} while (tmp.length() > 0 && tmp.charAt(tmp.length() - 1) != '1');
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

	class Feature {
		String word;
		int length;
		String pos;
		int leftEntropy;
		int rightEntropy;
		int tf;
		int mi;

		Feature(String word, String pos) {
			this.word = word;
			length = word.length();
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
				leftEntropy = 0;
				rightEntropy = 0;
			}
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
					Integer.toString(mi)
			);
		}
	}
}
