package evaluate;

import Config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

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
	 * @param
	 */
	public static HashSet<String> extractNewWord(String inputFile) {
		HashSet<String> newWordList = new HashSet<>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(inputFile));
			inputFile = inputFile.replaceAll("^.*/", "");// 保留单独的文件名
			BufferedWriter srcWriter = new BufferedWriter(new FileWriter("data/test/" + inputFile + ".src")),
					ansWriter = new BufferedWriter(new FileWriter("data/test/" + inputFile + ".ans"));
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
			logger.info("{} new words in {}", newWordList.size(), inputFile);
			srcWriter.close();
			ansWriter.close();
			//}
		} catch (java.io.IOException e) {
			logger.error("err!");
			e.printStackTrace();
		}
		return newWordList;
	}

	public static boolean isNewWord(String word) {
		//标点符号，含字母和数字的不算
		if (word.matches(Config.newWordExcludeRegex))
			return false;
		if (!basicWordList.contains(word))
			return true;
		return false;
	}

	public static void shuffleAndSplit(String[] inputFiles, String trainFile, String testFile) {
		try {
			List<String> lines = new ArrayList<>();
			for (String inputfile : inputFiles) {
				BufferedReader reader = new BufferedReader(new FileReader(inputfile));
				String line;
				while ((line = reader.readLine()) != null) {
					lines.add(line);
				}
			}
			Collections.shuffle(lines);
			BufferedWriter writer;
			writer = new BufferedWriter(new FileWriter(testFile));
			int i;
			for (i = 0; i < lines.size() / 5; i++) {
				writer.append(lines.get(i));
				writer.newLine();
			}
			writer = new BufferedWriter(new FileWriter(trainFile));
			for (; i < lines.size(); i++) {
				writer.append(lines.get(i));
				writer.newLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void main(String... args) {
		shuffleAndSplit(Config.newWordFiles, "data/raw/train.txt", "data/raw/test.txt");
		extractNewWord("data/raw/train.txt");
		extractNewWord("data/raw/test.txt");// create test data
	}
}
