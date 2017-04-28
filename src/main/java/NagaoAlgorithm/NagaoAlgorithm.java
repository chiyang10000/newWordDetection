package NagaoAlgorithm;

import Config.Config;
import evaluate.Corpus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

public class NagaoAlgorithm implements Serializable {
	// todo 应该把String换成一种基于指针的比较

	//private final static String stopwords = "的很了么呢是嘛个都也比还这于不与才上用就好在和对挺去后没说";
	static private final String stopwords = "的很了么呢是嘛都也于与在";
	static private Logger logger = LoggerFactory.getLogger(NagaoAlgorithm.class);
	public Map<String, TFNeighbor> wordTFNeighbor;
	public DiscreteTFNeighbor discreteTFNeighbor;
	private int maxWordLength;
	private List<String> leftPTable;
	private int[] leftLTable;
	private List<String> rightPTable;
	private int[] rightLTable;
	private double wordNumber;// 语料的总字数

	public NagaoAlgorithm(int maxWordLength) {
		this.maxWordLength = maxWordLength;
		leftPTable = new ArrayList<>();
		rightPTable = new ArrayList<>();
		wordTFNeighbor = new HashMap<String, TFNeighbor>();
	}

	public int getTF(String word) {
		if (wordTFNeighbor.containsKey(word))
			return wordTFNeighbor.get(word).getTF();
		return 1;
	}

	public static load

	public void calcDiscreteTFNeighbor(Set<String> wordList, int levelNum) {
		logger.debug("Running ...");
		double[] mi = new double[wordList.size()];
		double[] tf = new double[wordList.size()];
		double[] le = new double[wordList.size()];
		double[] re = new double[wordList.size()];
		int i = 0;
		for (String word : wordList) {
			TFNeighbor tfNeighbor = wordTFNeighbor.get(word);
			try {
				mi[i] = countMI(word);
				tf[i] = tfNeighbor.getTF();
				le[i] = tfNeighbor.getLeftNeighborEntropy();
				re[i] = tfNeighbor.getRightNeighborEntropy();
			} catch (NullPointerException e) {//长度过长或者被当做分隔符的词
				mi[i] = 0;
				tf[i] = 0;
				le[i] = 0;
				re[i] = 0;
				//logger.debug("<{}> is not counted in nagao algorithm", word);
			}
			i++;
		}
		discreteTFNeighbor = new DiscreteTFNeighbor(levelNum, mi, tf, le, re);
	}

	/**
	 * 添加词频信息
	 *
	 * @param wordFile
	 * @param outputFile
	 */
	public void addWordInfo(String wordFile, String outputFile) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(wordFile));
			BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
			String tmp;
			while ((tmp = reader.readLine()) != null) {
				int tf = 0;
				if (wordTFNeighbor.containsKey(tmp))
					tf = wordTFNeighbor.get(tmp).getTF();
				writer.append(String.format("%s\t%d", tmp, tf));
				writer.newLine();
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//reverse phrase
	private String reverse(String phrase) {
		return new StringBuffer(phrase).reverse().toString();
	}

	//co-prefix length of s1 and s2
	private int coPrefixLength(String s1, String s2) {
		int coPrefixLength = 0;
		for (int i = 0; i < Math.min(s1.length(), s2.length()); i++) {
			if (s1.charAt(i) == s2.charAt(i)) coPrefixLength++;
			else break;
		}
		return coPrefixLength;
	}

	//add substring of line to pTable
	private void addToPTable(String line) {
		//split line according to consecutive none Chinese character
		String[] phrases = line.split(Config.sepSentenceRegex);// + "|[" + stopwords + "]");
		//String[] phrases = {line};//line.split("[^\u4E00-\u9FA5]+|[" + stopwords + "]");
		for (String phrase : phrases) {
			for (int i = 0; i < phrase.length(); i++)
				rightPTable.add(phrase.substring(i));

			String reversePhrase = reverse(phrase);
			for (int i = 0; i < reversePhrase.length(); i++)
				leftPTable.add(reversePhrase.substring(i));

			wordNumber += phrase.length();
		}
	}

	//count lTable
	private void countLTable() {
		logger.debug("Running...");
		Collections.sort(rightPTable);
		rightLTable = new int[rightPTable.size()];
		rightLTable[0] = 0;
		for (int i = 1; i < rightPTable.size(); i++)
			rightLTable[i] = coPrefixLength(rightPTable.get(i - 1), rightPTable.get(i));

		Collections.sort(leftPTable);
		leftLTable = new int[leftPTable.size()];
		leftLTable[0] = 0;
		for (int i = 1; i < leftPTable.size(); i++)
			leftLTable[i] = coPrefixLength(leftPTable.get(i - 1), leftPTable.get(i));
	}

	/**
	 * nagao算法计算串频和左右信息熵
	 *
	 * @param wordlist 非null的话统计这个词表里面的词, 但是这样可能没法算MI
	 */
	public void countTFNeighbor(HashSet<String> wordlist) {
		//get TF and right neighbor
		for (int pIndex = 0; pIndex < rightPTable.size(); pIndex++) {
			String phrase = rightPTable.get(pIndex);
			for (int length = 1 + rightLTable[pIndex]; length <= maxWordLength && length <= phrase.length();
				 length++) {
				String word = phrase.substring(0, length);
				if (wordlist != null && !wordlist.contains(word))
					continue;
				TFNeighbor tfNeighbor = new TFNeighbor();
				tfNeighbor.incrementTF();
				if (phrase.length() > length)
					tfNeighbor.addToRightNeighbor(phrase.charAt(length));
				for (int lIndex = pIndex + 1; lIndex < rightLTable.length; lIndex++) {
					if (rightLTable[lIndex] >= length) {
						tfNeighbor.incrementTF();
						String coPhrase = rightPTable.get(lIndex);
						if (coPhrase.length() > length)
							tfNeighbor.addToRightNeighbor(coPhrase.charAt(length));
					} else break;
				}
				wordTFNeighbor.put(word, tfNeighbor);
			}
		}
		//get left neighbor
		for (int pIndex = 0; pIndex < leftPTable.size(); pIndex++) {
			String phrase = leftPTable.get(pIndex);
			for (int length = 1 + leftLTable[pIndex]; length <= maxWordLength && length <= phrase.length(); length++) {
				String word = reverse(phrase.substring(0, length));// 翻转了两次得到原来的词
				if (wordlist != null && !wordlist.contains(word))
					continue;
				TFNeighbor tfNeighbor = wordTFNeighbor.get(word);
				if (phrase.length() > length)
					tfNeighbor.addToLeftNeighbor(phrase.charAt(length));
				for (int lIndex = pIndex + 1; lIndex < leftLTable.length; lIndex++) {
					if (leftLTable[lIndex] >= length) {
						String coPhrase = leftPTable.get(lIndex);
						if (coPhrase.length() > length)
							tfNeighbor.addToLeftNeighbor(coPhrase.charAt(length));
					} else break;
				}
			}
		}
	}

	//according to wordTFNeighbor, count MI of word
	public double countMI(String word) {
		if (word.length() <= 1) // 单字词
			return 0;
		double coProbability = wordTFNeighbor.get(word).getTF() / wordNumber;
		List<Double> mi = new ArrayList<>(word.length());
		for (int pos = 1; pos < word.length(); pos++) {
			String leftPart = word.substring(0, pos);
			String rightPart = word.substring(pos);
			double leftProbability = wordTFNeighbor.get(leftPart).getTF() / wordNumber;
			double rightProbability = wordTFNeighbor.get(rightPart).getTF() / wordNumber;
			mi.add(coProbability / (leftProbability * rightProbability));
		}
		return Collections.min(mi);
	}

	public void scan(String[] inputFiles) {
		String line;
		logger.debug("Running...");
		for (String inputFile : inputFiles) {
			try {
				BufferedReader reader = new BufferedReader(new FileReader(inputFile));
				while ((line = reader.readLine()) != null) {
					addToPTable(line);
				}
				reader.close();
			} catch (IOException e) {
				logger.error("IO err: ", inputFiles);
				e.printStackTrace();
			}
		}
		countLTable();
	}

	public void detect(String[] inputs, String out, int thresholdTF, double thresholdMI,
					   double thresholdNeighborEntropy) {
		scan(inputs);
		countTFNeighbor(null);

		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(out));
			for (String word : wordTFNeighbor.keySet()) {
				if (word.length() <= 1 || !Corpus.isNewWord(word))
					continue;
				TFNeighbor tfNeighbor = wordTFNeighbor.get(word);

				int tf;
				double mi;
				tf = tfNeighbor.getTF();
				mi = countMI(word);

				if (tf > thresholdTF && tfNeighbor.getNeighborEntropy() > thresholdNeighborEntropy && mi >
						thresholdMI) {
					bw.append(word);
					bw.newLine();
				}
			}
			bw.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void setMaxWordLength(int maxWordLength) {
		this.maxWordLength = maxWordLength;
	}
}