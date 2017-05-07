package evaluate;

import com.googlecode.concurrenttrees.radix.ConcurrentRadixTree;
import com.googlecode.concurrenttrees.radix.RadixTree;
import com.googlecode.concurrenttrees.radix.node.concrete.DefaultCharArrayNodeFactory;
import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

/**
 * Created by wan on 4/7/2017.
 */
public class Corpus {
	private static final Logger logger = LoggerFactory.getLogger(Corpus.class);
	public static HashSet<String> basicWordList = new HashSet<>();
	public static HashSet<Character> basicCharacterList = new HashSet<>();
	public static RadixTree<WordInfo> wordInfo = new ConcurrentRadixTree<>(new DefaultCharArrayNodeFactory());
	public static DiscreteWordInfo discreteWordInfo;
	public static ExactWordInfo exactWordInfo = new ExactWordInfo();

	static {
		if (!new File("data/basicWordList.txt").exists()) {
			logger.info("Scanning word from file ...");
			for (String basicWordFile : config.basicWordFiles) {
				try {
					BufferedReader reader = new BufferedReader(new FileReader(basicWordFile));
					String tmp;
					while ((tmp = reader.readLine()) != null) {
						String[] segs = tmp.split(config.sepWordRegex);
						for (String word : segs)
							basicWordList.add(word.split(config.sepPosRegex)[0]);
					}
				} catch (java.io.IOException e) {
					e.printStackTrace();
					logger.error("Reading {} err!", basicWordFile);
				}
			}
			for (String word : basicWordList) {
				for (int i = 0; i < word.length(); i++) {
					basicCharacterList.add(word.charAt(i));
				}
			}
			logger.info("Basic word list size: {}", basicWordList.size());
			logger.info("Basic character list size: {}", basicCharacterList.size());
			BufferedWriter writer = null;
			try {
				writer = new BufferedWriter(new FileWriter("data/basicWordList.txt"));
				for (String word : basicWordList) {
					writer.append(word);
					writer.newLine();
				}
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
				logger.error("IO err");
			}
		} else {
			try {
				logger.info("Reading word from file ...");
				BufferedReader reader = new BufferedReader(new FileReader("data/basicWordList.txt"));
				String tmp;
				while ((tmp = reader.readLine()) != null) {
					basicWordList.add(tmp);
				}
				logger.info("Basic word list size: {}", basicWordList.size());
				logger.info("Basic character list size: {}", basicCharacterList.size());
			} catch (java.io.IOException e) {
				e.printStackTrace();
				logger.error("Reading {} err!", "data/basicWordList.txt");
			}
		}
	}

	/**
	 * 读入所有词的信息
	 */
	public static void loadWordInfo() {
		if (new File(config.corpusFile).exists()) {
			ArrayList<Integer> tfList = new ArrayList();
			ArrayList<Double> leList = new ArrayList<>(), reList = new ArrayList<>(), pmiList = new ArrayList();
			try {
				logger.debug("Reading word info into corpus");
				BufferedReader reader = new BufferedReader(new FileReader(config.corpusFile));
				String line;
				while ((line = reader.readLine()) != null) {
					String seg[] = line.split("\t");
					int tf = Integer.parseInt(seg[1]);
					tfList.add(tf);
					double pmi = Double.parseDouble(seg[2]);
					if (!Double.isNaN(pmi))
						pmiList.add(pmi);
					double le = Double.parseDouble(seg[3]);
					if (le > 0)
						leList.add(le);
					double re = Double.parseDouble(seg[4]);
					if (re > 0)
						reList.add(re);
					WordInfo tmp = new WordInfo(tf, pmi, le, re);
					wordInfo.put(seg[0], tmp);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			logger.info("{} strings in the corpus", wordInfo.size());
			discreteWordInfo = new DiscreteWordInfo(config.levelNum, tfList, pmiList, leList, reList);
		}
	}

	/**
	 * 将已分词文档转化为原始未分词语料和对应的新词文件
	 * 放在data/test文件夹底下
	 *
	 * @param
	 */
	public static String tagNW(String inputFile) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(inputFile));
			inputFile = inputFile.replaceAll("^.*/", "");// 保留单独的文件名
			BufferedWriter writer = new BufferedWriter(new FileWriter("data/test/" + inputFile + ".tagNW"));
			String tmp;
			while ((tmp = reader.readLine()) != null) {
				String[] segs = tmp.split(config.sepWordRegex);
				for (String seg : segs) {
					String word = config.removePos(seg);
					String pos = config.getPos(seg);
					if (!pos.equals("m") && !pos.equals("t"))
						if (isNewWord(word)) {
							writer.append(word + "/nw ");
						} else {
							writer.append(seg + " ");
						}
				}
				writer.newLine();
			}
			writer.close();
			//}
		} catch (java.io.IOException e) {
			logger.error("IO err!");
			e.printStackTrace();
		}
		return "data/test/" + inputFile + ".tagNW";
	}

	public static HashSet<String> extractWord(String inputFile, String pattern) {
		HashSet<String> wordList = new HashSet<>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(inputFile));
			inputFile = inputFile.replaceAll("^.*/", "");// 保留单独的文件名
			inputFile = inputFile.replaceAll("\\.tagNW", "");
			BufferedWriter writer = new BufferedWriter(new FileWriter(config.getAnswerFile(inputFile + ".src", pattern)));
			String line = null;
			while ((line = reader.readLine()) != null) {
				if (line.length() == 0) continue;
				for (String w : line.split(config.sepWordRegex)) {
					String[] tmp = w.split(config.sepPosRegex);
					try {
						if (tmp[1].equals(pattern) && !wordList.contains(tmp[0])) {
							writer.append(tmp[0]);
							wordList.add(tmp[0]);
							writer.newLine();
						}
					} catch (Exception e) {
						logger.debug("untagged {}", line);
					}
				}
			}
			logger.info("{} {} in {}", wordList.size(), pattern, inputFile);
			writer.close();
		} catch (IOException e) {
			logger.error("err");
		}
		return wordList;
	}

	/**
	 * 找出分词器没用正确分出来的词的数量
	 *
	 * @param inputFile
	 * @return
	 */
	@Deprecated
	public static HashSet<String> extractNewWordNotInSegmentation(String inputFile) {
		HashSet<String> newWordList = new HashSet<>();
		HashSet<String> goldenWordList = new HashSet<>();
		HashSet<String> segWordList = new HashSet<>();

		try {
			BufferedReader reader = new BufferedReader(new FileReader(inputFile));
			inputFile = inputFile.replaceAll("^.*/", "");// 保留单独的文件名
			BufferedWriter srcWriter = new BufferedWriter(new FileWriter("data/test/" + inputFile + ".src")),
					ansWriter = new BufferedWriter(new FileWriter("data/test/" + inputFile + ".seg.ans"));
			String tmp;
			while ((tmp = reader.readLine()) != null) {
				String[] segs = tmp.split(config.sepWordRegex);
				for (String seg : segs) {
					String word = (seg.split(config.sepPosRegex)[0]);
					goldenWordList.add(word);
					srcWriter.append(word);
				}
				for (Term term : new ToAnalysis().parseStr(tmp.replaceAll("/([^ ]*|$)", "").replaceAll(" ", ""))) {
					segWordList.add(term.getRealName());
				}
				srcWriter.newLine();
			}
			for (String word : goldenWordList)
				if (!segWordList.contains(word))
					if (isNewWord(word))
						newWordList.add(word);
			logger.info("{} new words in {}, not in segmentation", newWordList.size(), inputFile);
			for (String word : newWordList) {
				if (word.matches(config.newWordExcludeRegex))
					continue;
				ansWriter.append(word);
				ansWriter.newLine();
			}
			ansWriter.close();
			srcWriter.close();
		} catch (java.io.IOException e) {
			logger.error("IO err!");
			e.printStackTrace();
		}

		return newWordList;
	}

	public static boolean isNewWord(String word) {
		//标点符号，含字母和数字的不算
		if (word.matches(config.newWordExcludeRegex)
				|| word.matches("第?[几两数一二三四五六七八九十].*")// 去掉某些数量词
				)
			return false;
		if (!basicWordList.contains(word))
			return true;
		return false;
	}

	/**
	 * 以行为单位打乱
	 *
	 * @param inputFiles
	 * @param trainFile
	 * @param testFile
	 */
	public static void shuffleAndSplit(String[] inputFiles, String trainFile, String testFile, String totalFile) {
		try {
			int totalSize = 0;
			int currentSize = 0;
			List<String> lines = new ArrayList<>();
			BufferedWriter writer = new BufferedWriter(new FileWriter(totalFile));
			for (String inputfile : inputFiles) {
				BufferedReader reader = new BufferedReader(new FileReader(inputfile));
				String line;
				while ((line = reader.readLine()) != null) {
					lines.add(line);
					writer.append(line);
					writer.newLine();
					totalSize += line.length();
				}
			}
			writer.close();
			if (config.isShuffle)
				Collections.shuffle(lines); // todo no shuffle
			writer = new BufferedWriter(new FileWriter(testFile));
			int i;
			for (i = 0; currentSize < totalSize / config.testSize; i++) {
				writer.append(lines.get(i));
				currentSize += lines.get(i).length();
				writer.newLine();
			}
			writer.close();
			writer = new BufferedWriter(new FileWriter(trainFile));
			for (; i < lines.size(); i++) {
				writer.append(lines.get(i));
				writer.newLine();
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	static void convertToSrc(String[] inputFiles, String outputFile) {
		BufferedReader reader = null;
		boolean last = false, curr;
		int word = 0, article = 0;
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
			for (String inputFile : inputFiles) {
				reader = new BufferedReader(new FileReader(inputFile));
				String line;
				while ((line = reader.readLine()) != null) {
					if (line.length() == 0) continue;
					line = line.replaceAll("/[^ ]+", "");
					line = line.replaceAll(" +", "");
					curr = line.substring(line.length() - 1).matches("[稿\\pP&&[^】]]");
					if (last && !curr) {
						writer.newLine();
						article++;
					}
					last = curr;
					writer.append(line);
					writer.newLine();
					word += line.length();
				}
			}
			writer.close();
			logger.info("{} article {} characters in {}", article, word, outputFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void addWordInfo(String wordFile, String outputFile) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(wordFile));
			BufferedWriter writer = new BufferedWriter(new FileWriter(wordFile + ".nagao"));
			String tmp;
			while ((tmp = reader.readLine()) != null) {
				int tf = 1;
				double mi = 10000, entropy = 10000, le = 10000, re = 10000;
				tf = exactWordInfo.getTF(tmp);
				mi = exactWordInfo.getPMI(tmp);
				le = exactWordInfo.getLE(tmp);
				re = exactWordInfo.getRE(tmp);
				writer.append(String.format("%s\t%d\t%f\t%f\t%f\t%f", tmp, tf, mi, le, re, entropy));
				writer.newLine();
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 从数据集中提取新词，分割为训练集和测试集
	 *
	 * @param args
	 */
	public static void main(String... args) throws IOException {
		ConvertHalfWidthToFullWidth.convertFileToFulll(config.news, config.newWordFile);
		shuffleAndSplit(config.newWordFiles, config.trainData, config.testData, config.totalData);
		//convertToSrc(config.basicWordFiles, "data/corpus/renminribao.txt");
		convertToSrc(config.newWordFiles, "data/corpus/train.txt");
		convertToSrc(new String[]{config.testData}, config.testDataInput);
		convertToSrc(new String[]{config.trainData}, config.trainDataInput);
		convertToSrc(new String[]{config.totalData}, config.totalDataInput);
		extractWord(tagNW(config.trainData), config.nw);
		extractWord(tagNW(config.testData), config.nw);
		extractWord(tagNW(config.totalData), config.nw);
		extractWord(config.trainData, config.ns);
		extractWord(config.testData, config.ns);
		extractWord(config.trainData, config.nr);
		extractWord(config.testData, config.nr);
	}

	static class WordInfo {
		int tf;
		double le, re, pmi;

		WordInfo(int tf, double pmi, double le, double re) {
			this.tf = tf;
			this.pmi = pmi;
			this.le = le;
			this.re = re;
		}
	}

	static class ExactWordInfo {
		int getTF(String word) {
			WordInfo tmp = wordInfo.getValueForExactKey(word);
			if (tmp == null)
				return 0;
			return tmp.tf;
		}

		double getPMI(String word) {
			WordInfo tmp = wordInfo.getValueForExactKey(word);
			// 假设没出现过的pmi一定很高
			if (tmp == null)
				return 100;
			return tmp.pmi;
		}

		double getLE(String word) {
			WordInfo tmp = wordInfo.getValueForExactKey(word);
			if (tmp == null)
				return 0;
			return tmp.le;
		}

		double getRE(String word) {
			WordInfo tmp = wordInfo.getValueForExactKey(word);
			if (tmp == null)
				return 0;
			return tmp.re;
		}
	}

	public static class DiscreteWordInfo {
		double mi[], tf[], le[], re[];

		/**
		 * pmi不是NaN, entropy大于0
		 * todo pmi不是NaN
		 */
		public DiscreteWordInfo(int levelNum, List<Integer> tf_array, List<Double> pmi_array, List<Double> le_array,
								List<Double> re_array) {


			Integer[] tmp_tf = new Integer[tf_array.size()];
			tmp_tf = tf_array.toArray(tmp_tf);
			Arrays.sort(tmp_tf);

			Double[] tmp_pmi = new Double[pmi_array.size()];
			tmp_pmi = pmi_array.toArray(tmp_pmi);
			Arrays.sort(tmp_pmi);

			Double[] tmp_le = new Double[le_array.size()];
			tmp_le = le_array.toArray(tmp_le);
			Arrays.sort(tmp_le);

			Double[] tmp_re = new Double[re_array.size()];
			tmp_re = re_array.toArray(tmp_re);
			Arrays.sort(tmp_re);
			logger.info("tf {} pmi {}  le {} re {}", tmp_tf.length, tmp_pmi.length, tmp_le.length, tmp_re.length);

			mi = new double[levelNum + 1];
			tf = new double[levelNum + 1];
			le = new double[levelNum + 1];
			re = new double[levelNum + 1];
			for (int i = 0; i < levelNum; i++) {
				mi[i] = tmp_pmi[i * tmp_pmi.length / levelNum];
				tf[i] = tmp_pmi[i * tmp_tf.length / levelNum];
				le[i] = tmp_pmi[i * tmp_le.length / levelNum];
				re[i] = tmp_pmi[i * tmp_re.length / levelNum];
			}
			//边界处理
			mi[levelNum] = Double.MAX_VALUE;
			tf[levelNum] = Double.MAX_VALUE;
			le[levelNum] = Double.MAX_VALUE;
			re[levelNum] = Double.MAX_VALUE;
		}

		public int getPMI(String word) {
			double value = exactWordInfo.getPMI(word);
			//pmi为0算一类
			if (Double.isNaN(value))
				return -1;
			int i = 0;
			while (mi[++i] < value) ;
			return i - 1;
		}

		public int getTF(String word) {
			// tf为0算一类
			int value = exactWordInfo.getTF(word);
			if (value == 0)
				return -1;
			int i = 0;
			while (tf[++i] < value) ;
			return i - 1;
		}

		//左右熵为0的算作一类
		public int getLE(String word) {
			double value = exactWordInfo.getLE(word);
			if (Math.abs(value) == 0.0)
				return -1;
			int i = 0;
			while (le[++i] < value) ;
			return i - 1;
		}

		public int getRE(String word) {
			double value = exactWordInfo.getRE(word);
			if (Math.abs(value) == 0.0)
				return -1;
			int i = 0;
			while (re[++i] < value) ;
			return i - 1;
		}
	}
}
