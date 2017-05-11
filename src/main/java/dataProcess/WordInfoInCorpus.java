package dataProcess;

import com.googlecode.concurrenttrees.radix.ConcurrentRadixTree;
import com.googlecode.concurrenttrees.radix.RadixTree;
import com.googlecode.concurrenttrees.radix.node.concrete.DefaultCharArrayNodeFactory;
import dict.build.FastBuilder;
import evaluate.RunSystemCommand;
import evaluate.config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by don on 08/05/2017.
 */
public class WordInfoInCorpus {
	private static final Logger logger = LoggerFactory.getLogger(WordInfoInCorpus.class);
	String corpus, corpusInput;
	public DiscreteWordInfo discreteWordInfo;
	public ExactWordInfo exactWordInfo = new ExactWordInfo();
	private RadixTree<WordInfo> wordInfo = new ConcurrentRadixTree<>(new DefaultCharArrayNodeFactory());

	public static void main(String... args) {
		//RunSystemCommand.run("rm data/corpus/*.words*");
		clean();
		new WordInfoInCorpus(config.renmingribao);
	}

	public WordInfoInCorpus(String corpusInput) {
		this.corpusInput = corpusInput;
		corpus = "data/corpus/" + corpusInput.replaceAll(".*/", "");
		while (!loadWordInfo()) {
			calcWordInfo();
		}
	}

	public static void clean() {
		RunSystemCommand.run("find data/corpus -name *.data | xargs rm");
		RunSystemCommand.run("find data/corpus -name merge* | xargs rm");
	}

	public void addWordInfo(String wordFile, String outputFile) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(wordFile));
			BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
			BufferedWriter writerDis = new BufferedWriter(new FileWriter(outputFile + "Dis"));
			String line;
			while ((line = reader.readLine()) != null) {
				String[] tmp = line.split("\\s+");
				String word = tmp[0];
				int tf = 1;
				double mi = 10000, le = 10000, re = 10000;
				tf = exactWordInfo.getTF(word);
				mi = exactWordInfo.getPMI(word);
				le = exactWordInfo.getLE(word);
				re = exactWordInfo.getRE(word);
				writer.append(String.format("%s\t%d\t%f\t%f\t%f", line, tf, mi, le, re));
				writerDis.append(String.format("%s\t%d\t%d\t%d\t%d", line,
						discreteWordInfo.getTF(word), discreteWordInfo.getPMI(word), discreteWordInfo.getLE(word), discreteWordInfo.getRE(word)));
				writer.newLine();
				writerDis.newLine();
			}
			writer.close();
			writerDis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 读入所有词的信息
	 */
	private boolean loadWordInfo() {
		if (new File(corpus + ".words").exists()) {
			ArrayList<Integer> tfList = new ArrayList();
			ArrayList<Double> leList = new ArrayList<>(), reList = new ArrayList<>(), pmiList = new ArrayList();
			try {
				logger.debug("Reading word info into corpus");
				BufferedReader reader = new BufferedReader(new FileReader(corpus + ".words"));
				String line;
				while ((line = reader.readLine()) != null) {
					String seg[] = line.split("\t");
					int tf = Integer.parseInt(seg[1]);
					if (tf > 1) //只离散出现频率大于1的
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
			return true;
		} // file exist!
		else
			return false;
	}

	private void calcWordInfo() {
		logger.debug("Calc word info into corpus ...");
		Corpus.convertToSrc(new String[]{corpusInput}, "tmp/tmp");// 去掉词性
		ConvertHalfWidthToFullWidth.convertFileToFulll("tmp/tmp", corpus); // 全角半角的转换
		FastBuilder builder = new FastBuilder();
		String left, right, entropyfile, rawpath = corpus;

		right = builder.genFreqRight(rawpath, 10, 10 * 1024);
		left = builder.genLeft(rawpath, 10, 10 * 1024);
		entropyfile = builder.mergeEntropy(right, left);

		builder.extractWords(right, entropyfile, rawpath.replaceAll(".*[/\\\\]", ""));
		clean();
	}

	class WordInfo {
		int tf;
		double le, re, pmi;

		WordInfo(int tf, double pmi, double le, double re) {
			this.tf = tf;
			this.pmi = pmi;
			this.le = le;
			this.re = re;
		}
	}

	class ExactWordInfo {
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
				return Double.NaN;
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

	public class DiscreteWordInfo {
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
			// tf为1的算一类
			int value = exactWordInfo.getTF(word);
			if (value == 0)
				return -1;
			if (value == 1)
				return -2;
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
