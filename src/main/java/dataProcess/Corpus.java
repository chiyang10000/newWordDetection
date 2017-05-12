package dataProcess;

import evaluate.RunSystemCommand;
import evaluate.Test;
import evaluate.config;
import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;
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
	private static final Logger logger = LoggerFactory.getLogger(Corpus.class);
	public static HashSet<String> basicWordList = new HashSet<>();
	public static HashSet<Character> basicCharacterList = new HashSet<>();

	static {
		if (!new File(config.basicWordListFile).exists()) {
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
				writer = new BufferedWriter(new FileWriter(config.basicWordListFile));
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
				BufferedReader reader = new BufferedReader(new FileReader(config.basicWordListFile));
				String tmp;
				while ((tmp = reader.readLine()) != null) {
					basicWordList.add(tmp);
				}
				logger.info("Basic word list size: {}", basicWordList.size());
				logger.info("Basic character list size: {}", basicCharacterList.size());
			} catch (java.io.IOException e) {
				e.printStackTrace();
				logger.error("Reading {} err!", config.basicWordListFile);
			}
		}
	}

	static void clean() {
		RunSystemCommand.run("find data/test -type f | grep -v gitignore | xargs rm");
	}

	/**
	 * 将已分词文档转化为原始未分词语料和对应的新词文件
	 * 放在data/test文件夹底下
	 *
	 * @param
	 */
	@Deprecated
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
					word = config.newWordFileter(word);
					String pos = config.getPos(seg);
						if (isNewWord(word, pos)) {
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



	static private String category(String word) {
		for (String type: config.newWordType.keySet())
			if (word.matches(config.newWordType.get(type)))
				return type;
		return "none";
	}

	public static HashSet<String> extractWord(String inputFile, String pattern) {
		HashSet<String> wordList = new HashSet<>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(inputFile));
			inputFile = inputFile.replaceAll("^.*/", "");// 保留单独的文件名
			inputFile = inputFile.replaceAll("\\.tagNW", "");
			BufferedWriter writer = new BufferedWriter(new FileWriter(Test.getAnswerFile(inputFile + ".src", pattern)));
			String line = null;
			while ((line = reader.readLine()) != null) {
				if (line.length() == 0) continue;
				for (String w : line.split(config.sepWordRegex)) {
					String[] tmp = w.split(config.sepPosRegex);
					try {
						//System.err.println(line);
						if ((pattern != config.nw && tmp[1].equals(pattern) || pattern == config.nw && isNewWord(tmp[0], tmp[1])) && !wordList.contains(tmp[0])) {

							//System.err.println(line);
							writer.append(tmp[0] + "\t" +category(tmp[0]) + "\t" + tmp[0].length() + "\t" + tmp[1]);
							wordList.add(tmp[0]);
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
		//标点符号，含字母和数字的不算
		word = config.newWordFileter(word);
		if (pos != null)
		if (pos.matches("[tmq]")) return false;// todo 去除数量词 和 时间词

		if (word.matches(config.newWordExcludeRegex)
			//|| word.matches("第?[几两数一二三四五六七八九十].*")// 去掉某些数量词
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
			if (config.isShuffle) {
				Collections.shuffle(lines); // todo no shuffle
				RunSystemCommand.run("rm data/model/*.model");
			}
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

	/**
	 * 从数据集中提取新词，分割为训练集和测试集
	 *
	 * @param args
	 */
	public static void main(String... args) throws IOException {
		clean();

		ConvertHalfWidthToFullWidth.convertFileToFulll(config.news, config.newWordFile);
		shuffleAndSplit(config.newWordFiles, config.trainData, config.testData, config.totalData);

		convertToSrc(new String[]{config.testData}, config.testDataInput);
		convertToSrc(new String[]{config.trainData}, config.trainDataInput);
		convertToSrc(new String[]{config.totalData}, config.totalDataInput);

		for (String type: config.supportedType) {
			extractWord(config.trainData, type);
			extractWord(config.testData, type);
			extractWord(config.totalData, type);
		}
	}

}
