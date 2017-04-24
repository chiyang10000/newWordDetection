package evaluate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import singleCharacterCRF.singleCharacterCRF;

import java.io.*;
import java.util.HashSet;

/**
 * Created by wan on 4/7/2017.
 */
public class Corpus {
	public static HashSet<String> basicWordList = new HashSet<>();
	public static HashSet<Character> basicCharacterList = new HashSet<>();
	private static final Logger logger = LoggerFactory.getLogger(Corpus.class);

	static {
		//basic word list comes from 人民日报语料
		String[] basicWordFiles = {"data/2000-01-粗标.txt", "data/2000-02-粗标.txt", "data/2000-03-粗标.txt"};
		for (String basicWordFile : basicWordFiles) {
			try {
				BufferedReader reader = new BufferedReader(new FileReader(basicWordFile));
				String tmp;
				while ((tmp = reader.readLine()) != null) {
					String[] segs = tmp.split(" ");
					for (String word : segs)
						basicWordList.add(word.split("/")[0]);
				}
			} catch (java.io.IOException e) {
				e.printStackTrace();
				System.err.println("reading file error");
			}
		}
		for (String word : basicWordList) {
			for (int i = 0; i < word.length(); i++) {
				basicCharacterList.add(word.charAt(i));
			}
		}
		logger.info("basic word list size: {}", basicWordList.size());
		logger.info("basic character list size: {}", basicCharacterList.size());
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
		}
	}

	/**
	 * 将已分词文档转化为原始未分词语料和对应的新词文件
	 * 放在data文件夹底下
	 *
	 * @param newWordFiles
	 */
	public static void extractNewWord(String... newWordFiles) {
		HashSet<String> newWordList;
		try {
			for (String newWordFile : newWordFiles) {
				newWordList = new HashSet<>();
				BufferedReader reader = new BufferedReader(new FileReader(newWordFile));
				newWordFile = newWordFile.replaceAll("^.*/", "");
				BufferedWriter srcWriter = new BufferedWriter(new FileWriter("data/test/" + newWordFile + ".src")),
						ansWriter = new BufferedWriter(new FileWriter("data/test/" + newWordFile + ".ans"));
				String tmp;
				while ((tmp = reader.readLine()) != null) {
					String[] segs = tmp.split(" ");
					for (String seg : segs) {
						String word = (seg.split("/")[0]);
						srcWriter.append(word);
						if (isNewWord(word)) {
							if (!newWordList.contains(word)) {
								ansWriter.append(word);
								ansWriter.newLine();
							}
							newWordList.add(word);
						}
					}
					srcWriter.newLine();
				}
				logger.info("{} new words in {}", newWordList.size(), newWordFile);
				srcWriter.close();
				ansWriter.close();
			}
		} catch (java.io.IOException e) {
			e.printStackTrace();
		}
	}

	public static boolean isNewWord(String word) {
		//标点符号，含英文和数字的不算
		if (!word.matches(".*[\\pP]+.*") && !word.matches(".*[\\d\\w]+.*"))
			if (!basicWordList.contains(word))
				return true;
		return false;
	}

	public static void main(String... args) {
		String[] basicWordFiles = {"data/2000-01-粗标.txt", "data/2000-02-粗标.txt", "data/2000-03-粗标.txt"};
		String[] newWordFiles = {"data/1_5000_1.segged.txt", "data/1_5000_2.segged.txt", "data/1_5000_3.segged.txt", "data/1_5000_4.segged.txt", "data/1_5000_5.segged.txt"};
		//extractNewWord(newWordFiles);// create test data
		//convertSrcToBEMS("tmp3.txt", new String[]{"data/1_5000_1.segged.txt.src"});
		//convertBEMSToSeg("data/train.bems", "tmp1.txt", "tmp2.txt");
	}
}
