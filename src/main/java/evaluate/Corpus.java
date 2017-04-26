package evaluate;

import Config.Config;
import NagaoAlgorithm.NagaoAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.MessageFormat;
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
		for (String basicWordFile : Config.basicWordFiles) {
			try {
				BufferedReader reader = new BufferedReader(new FileReader(basicWordFile));
				String tmp;
				while ((tmp = reader.readLine()) != null) {
					String[] segs = tmp.split(Config.sepWordRegex);
					for (String word : segs)
						basicWordList.add(word.split(Config.sepPosRegex)[0]);
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
			logger.info("err");
		}
	}

	/**
	 * 将已分词文档转化为原始未分词语料和对应的新词文件
	 * 放在data文件夹底下
	 *
	 * @param newWordFiles
	 */
	public static void extractNewWord(String[] newWordFiles) {
		HashSet<String> newWordList;
		try {
			for (String newWordFile : newWordFiles) {
				newWordList = new HashSet<>();
				BufferedReader reader = new BufferedReader(new FileReader(newWordFile));
				newWordFile = newWordFile.replaceAll("^.*/", "");// 保留单独的文件名
				BufferedWriter srcWriter = new BufferedWriter(new FileWriter("data/test/" + newWordFile + ".src")),
						ansWriter = new BufferedWriter(new FileWriter("data/test/" + newWordFile + ".ans"));
				String tmp;
				while ((tmp = reader.readLine()) != null) {
					String[] segs = tmp.split(Config.sepWordRegex);
					for (String seg : segs) {
						String word = (seg.split(Config.sepPosRegex)[0]);
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
			logger.error("err!");
			e.printStackTrace();
		}
	}

	public static boolean isNewWord(String word) {
		//标点符号，含字母和数字的不算
		if (word.matches(Config.newWordExcludeRegex))
			return false;
		if (!basicWordList.contains(word))
			return true;
		return false;
	}

	public static void addWordInfo(String wordFile, String outputFile, NagaoAlgorithm nago) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(wordFile));
			BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
			String tmp;
			while ((tmp = reader.readLine()) != null) {
				logger.info(tmp);
				writer.append(String.format("%s\t%d", tmp, nago.wordTFNeighbor.get(tmp).getTF()));
				writer.newLine();
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String... args) {
		extractNewWord(Config.newWordFiles);// create test data
	}
}
