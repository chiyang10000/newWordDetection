package NagaoAlgorithm;

import Config.Config;
import evaluate.Corpus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

public class NagaoAlgorithm {

	static private Logger logger = LoggerFactory.getLogger(NagaoAlgorithm.class);
	private int maxWordLength;

	private List<String> leftPTable;
	private int[] leftLTable;
	private List<String> rightPTable;
	private int[] rightLTable;
	private double wordNumber;// 语料的总字数

	public Map<String, TFNeighbor> wordTFNeighbor;

	//private final static String stopwords = "的很了么呢是嘛个都也比还这于不与才上用就好在和对挺去后没说";
	private final static String stopwords = "的很了么呢是嘛都也于与在";

	public NagaoAlgorithm(int maxWordLength) {
		this.maxWordLength = maxWordLength;
		leftPTable = new ArrayList<String>();
		rightPTable = new ArrayList<String>();
		wordTFNeighbor = new HashMap<String, TFNeighbor>();
	}

	public void addWordInfo(String wordFile, String outputFile) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(wordFile));
			BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
			String tmp;
			while ((tmp = reader.readLine()) != null) {
				//logger.info(tmp);
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

	//according to pTable and lTable, count statistical result: TF, neighbor distribution
	private void countTFNeighbor() {
		//get TF and right neighbor
		for (int pIndex = 0; pIndex < rightPTable.size(); pIndex++) {
			String phrase = rightPTable.get(pIndex);
			for (int length = 1 + rightLTable[pIndex]; length <= maxWordLength && length <= phrase.length(); length++) {
				String word = phrase.substring(0, length);
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
		//System.out.println("Info: [Nagao Algorithm Step 3]: having counted TF and Neighbor");
	}

	//according to wordTFNeighbor, count MI of word
	private double countMI(String word) {
		if (word.length() <= 1) return 0;
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

	private void saveTFNeighborInfoMI(String out) {
	}

	public void scan(String[] inputFiles) {
		String line;
		for (String inputFile : inputFiles) {
			try {
				BufferedReader reader = new BufferedReader(new FileReader(inputFile));
				while ((line = reader.readLine()) != null) {
					// todo 用标点分割每一句。
					addToPTable(line);
				}
				reader.close();
			} catch (IOException e) {
				logger.error("scan [{}] error!", inputFiles);
				e.printStackTrace();
			}
		}
		countLTable();
		countTFNeighbor();
	}

	public void detect(String[] inputs, String out, int thresholdTF, double thresholdMI,
					   double thresholdNeighborEntropy) {
		NagaoAlgorithm nagao = this;
		nagao.scan(inputs);

		try {
			//output words TF, neighbor info, MI
			BufferedWriter bw = new BufferedWriter(new FileWriter(out));
			for (String word : nagao.wordTFNeighbor.keySet()) {
				if (word.length() <= 1 || !Corpus.isNewWord(word))
					continue;
				TFNeighbor tfNeighbor = nagao.wordTFNeighbor.get(word);

				int tf, leftNeighborNumber, rightNeighborNumber;
				double mi;
				tf = tfNeighbor.getTF();
				leftNeighborNumber = tfNeighbor.getLeftNeighborNumber();
				rightNeighborNumber = tfNeighbor.getRightNeighborNumber();
				mi = nagao.countMI(word);

				if (tf > thresholdTF && tfNeighbor.getNeighborEntropy() > thresholdNeighborEntropy && mi >
						thresholdMI) {
					StringBuilder sb = new StringBuilder();
					sb.append(word);
					/*
					sb.append(",").append(tf);
					sb.append(",").append(leftNeighborNumber);
					sb.append(",").append(rightNeighborNumber);
					sb.append(",").append(tfNeighbor.getLeftNeighborEntropy());
					sb.append(",").append(tfNeighbor.getRightNeighborEntropy());
					sb.append(",").append(mi).append("\n");
					*/
					sb.append("\n");
					bw.write(sb.toString());
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