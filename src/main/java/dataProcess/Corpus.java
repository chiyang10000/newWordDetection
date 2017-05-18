package dataProcess;

import evaluate.RunSystemCommand;
import evaluate.Test;
import evaluate.config;
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
	public static Set<String> basicWordList = new HashSet<>();
	public static CounterMap basicWordListCounter = new CounterMap();
	public static HashSet<Character> basicCharacterList = new HashSet<>();

	static {
		if (!new File(config.basicWordListFile).exists()) {
			logger.info("Scanning word list from {}...", config.basicWordFile);
				try {
					BufferedReader reader = new BufferedReader(new FileReader(config.basicWordFile));
					String tmp;
					while ((tmp = reader.readLine()) != null) {
						String[] segs = tmp.split(config.sepWordRegex);
						for (String word : segs) {
							word = config.removePos(word);
							if (!word.matches(config.newWordExcludeRegex))
							basicWordListCounter.incr(word);
						}
					}
				} catch (java.io.IOException e) {
					e.printStackTrace();
					logger.error("Reading word list from {} err!", config.basicWordFile);
				}
			basicWordList = basicWordListCounter.countAll().keySet();
			for (String word : basicWordList) {
				for (int i = 0; i < word.length(); i++) {
					basicCharacterList.add(word.charAt(i));
				}
			}
			logger.info("Basic word list size: {}", basicWordList.size());
			logger.info("Basic character list size: {}", basicCharacterList.size());
			basicWordListCounter.output(config.basicWordListFile);
		} else {
			logger.info("Reading word lits from {} ...", config.basicWordListFile);
			basicWordList = Test.readWordList(config.basicWordListFile).keySet();
			logger.info("Basic word list size: {}", basicWordList.size());
		}
	}

	static void clean() {
		RunSystemCommand.run("rm data/corpus/*.words");
		RunSystemCommand.run("find data/test -type f | xargs rm");
	}

	public static HashSet<String> extractWord(String inputFile, String pattern) {
		WordInfoInCorpus wordInfoInCorpus = new WordInfoInCorpus(config.totalDataInput);
		HashSet<String> wordList = new HashSet<>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(inputFile));
			inputFile = inputFile.replaceAll("^.*/", "");// 保留单独的文件名
			inputFile = inputFile.replaceAll("\\.tagNW", "");
			BufferedWriter writer = new BufferedWriter(new FileWriter(Test.getAnswerFile(inputFile + ".src",
					pattern)));
			String line = null;
			while ((line = reader.readLine()) != null) {
				if (line.trim().length() == 0) continue;
				for (String w : line.split(config.sepWordRegex)) {
					String word = config.removePos(w);
					String pos = config.getPos(w);
					try {
						//System.err.println(line);
						if (
								(pattern != config.nw && pos.equals(pattern) && !word.matches(config
										.newWordExcludeRegex)
										|| pattern == config.nw && isNewWord(word, pos)
								)
										&& !wordList.contains(word)) {
							writer.append(
									wordInfoInCorpus.addWordInfo(word + "\t" + config.category(word) + "\t" + word
											.length() + "\t" + pos));
							wordList.add(word);
							writer.newLine();
						}
					} catch (IOException e) {
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
					String word = (config.removePos(seg));
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
					if (isNewWord(word, null))
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

	public static boolean isNewWord(String word, String pos) {
		if (word.length() <= 1)
			return false;
		//标点符号，含字母和数字的不算
		//if (pos != null)
		//if (pos.matches("[tmq]")) return false;// todo 去除数量词 和 时间词

		word = config.newWordFileter(word);
		if (word.matches(config.newWordExcludeRegex)
				)
			return false;
		if (!basicWordList.contains(word))
			return true;
		return false;
	}

	/**
	 * 以行为单位打乱
	 *
	 * @param inputFile
	 * @param trainFile
	 * @param testFile
	 */
	public static void shuffleAndSplit(String inputFile, String trainFile, String testFile, String totalFile) {
		try {
			boolean last = false, curr;
			int totalSize = 0;
			int currentSize = 0;
			List<String> article = new ArrayList<>();
			BufferedWriter writer = new BufferedWriter(new FileWriter(totalFile));

				BufferedReader reader = new BufferedReader(new FileReader(inputFile));
				String line;
				StringBuilder buffer = new StringBuilder();
				while ((line = reader.readLine()) != null) {
					if (line.length() == 0) continue;
					curr = line.substring(line.length() - 3).replaceAll("/.*", "").matches(".*[稿\\pP&&[^】]]");
					if (last && !curr) {
						article.add(buffer.toString());
						writer.append(buffer.toString());
						totalSize += buffer.length();
						writer.newLine();
						buffer = new StringBuilder();
					}
					buffer.append(line);
					buffer.append("\n");
					last = curr;
				}
				//没清空的
				article.add(buffer.toString());
				writer.append(buffer.toString());
			writer.close();
			if (config.isShuffle) {
				logger.info("article size {}", article.size());
				Collections.shuffle(article); // todo no shuffle
				RunSystemCommand.run("rm data/model/*.model");
			}
			writer = new BufferedWriter(new FileWriter(testFile));
			int i;
			for (i = 0; currentSize < totalSize / config.testSize; i++) {
				writer.append(article.get(i));
				currentSize += article.get(i).length();
				writer.newLine();
			}
			writer.close();
			writer = new BufferedWriter(new FileWriter(trainFile));
			for (; i < article.size(); i++) {
				writer.append(article.get(i));
				writer.newLine();
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static String convertToSrc(String[] inputFiles, String outputFile) {
		BufferedReader reader = null;
		int word = 0, article = 0;
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
			for (String inputFile : inputFiles) {
				reader = new BufferedReader(new FileReader(inputFile));
				String line;
				while ((line = reader.readLine()) != null) {
					if (line.length() == 0) {
						article++;
						writer.newLine();
						continue;
					}
					line = line.replaceAll("/[^ ]+", "");
					line = line.replaceAll(" +", "");
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
		return outputFile;
	}

	/**
	 * 从数据集中提取新词，分割为训练集和测试集
	 *
	 * @param args
	 */
	public static void main(String... args) throws IOException {


		clean();
		ConvertHalfWidthToFullWidth.convertFileToFulllKeepPos(config.news, config.newWordFile);
		shuffleAndSplit(config.newWordFile, config.trainData, config.testData, config.totalData);

		convertToSrc(new String[]{config.testData}, config.testDataInput);
		convertToSrc(new String[]{config.trainData}, config.trainDataInput);
		convertToSrc(new String[]{config.totalData}, config.totalDataInput);

		for (String type : config.supportedType) {
			extractWord(config.trainData, type);
			extractWord(config.testData, type);
			extractWord(config.totalData, type);
		}
	}

}
