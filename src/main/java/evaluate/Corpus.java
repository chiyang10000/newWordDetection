package evaluate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
		if (!word.matches("[\\pP\\p{Punct}]") && !word.matches(".*[\\d\\w]+.*"))
			if (!basicWordList.contains(word))
				return true;
		return false;
	}

	/**
	 * @param inputFiles
	 */
	public static void convertToTrainBEMS(String outputFile, String... inputFiles) {
		try {
			for (String inputFile : inputFiles) {
				BufferedReader reader = new BufferedReader(new FileReader(inputFile));
				BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
				String tmp;
				while ((tmp = reader.readLine()) != null) {
					if (tmp.trim().length() == 0) continue;
					String[] segs = tmp.split(" +");
					for (String seg : segs) {
						//System.out.println(seg);
						String word = (seg.split("/")[0]);
						if (word.length() == 0) {
							writer.append(String.format("%s\t%s", '/', 'S'));
							writer.newLine();
						}// 两个斜线
						else if (word.length() == 1) {
							writer.append(String.format("%s\t%s", word.charAt(0), 'S'));
							writer.newLine();
						} else {
							writer.append(String.format("%s\t%s", word.charAt(0), 'B'));
							writer.newLine();
							for (int i = 1; i < word.length() - 1; i++) {
								writer.append(String.format("%s\t%s", word.charAt(i), 'M'));
								writer.newLine();
							}
							writer.append(String.format("%s\t%s", word.charAt(word.length() - 1), 'E'));
							writer.newLine();
						}
					}
					writer.append(' ');
					writer.newLine();
				}
				writer.close();
			}
		} catch (java.io.IOException e) {
			e.printStackTrace();
		}
	}

	public static void convertSrcToBEMS(String outputFile, String... inputFiles) {
		//
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
			BufferedReader reader;
			String tmp;
			for (String inputFile: inputFiles) {
				reader = new BufferedReader(new FileReader(inputFile));
				while ((tmp = reader.readLine()) !=null) {
					String[] tmps = tmp.split("[，。]");
					int offset = 0;
					for (String sentence: tmps) {
						offset += sentence.length() + 1;
						//考虑没有逗号和句号的行
						for (int i = 0; i < sentence.length(); i++){
							writer.append(sentence.charAt(i));
							writer.newLine();
						}
						if (offset - 1 < tmp.length()) {
							writer.append(tmp.charAt(offset - 1));
							writer.newLine();
						}
						writer.newLine();
					}
				}
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void convertBEMSToSeg(String inputFile, String segFile, String newWordFile) {
		//write segFile and new word File
		HashSet<String> newWordList = new HashSet<>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(inputFile));
			BufferedWriter writerSeg = new BufferedWriter(new FileWriter(segFile));
			BufferedWriter writerNewWord = new BufferedWriter(new FileWriter(newWordFile));
			String tmp;
			while ( (tmp = reader.readLine()) !=null) {
				if (tmp.length() == 0) {
					writerSeg.newLine();
					continue;
				}
				StringBuilder wordBuffer = new StringBuilder();
				wordBuffer.append(tmp.split("\t", 2)[0]);
				if (tmp.charAt(tmp.length() - 1) == 'B') {
					do {
						tmp = reader.readLine();
						wordBuffer.append(tmp.split("\t", 2)[0]);
					}while (tmp.charAt(tmp.length() - 1) != 'E');
				}

				String word = wordBuffer.toString();
				writerSeg.append(word + ' ');
				if (isNewWord(word) && !newWordList.contains(word)) {
					newWordList.add(word);
					writerNewWord.append(word);
					writerNewWord.newLine();
				}
			}
			writerNewWord.close();
			writerSeg.close();
		} catch (java.io.IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String... args) {
		String[] basicWordFiles = {"data/2000-01-粗标.txt", "data/2000-02-粗标.txt", "data/2000-03-粗标.txt"};
		String[] newWordFiles = {"data/1_5000_1.segged.txt", "data/1_5000_2.segged.txt", "data/1_5000_3.segged.txt", "data/1_5000_4.segged.txt", "data/1_5000_5.segged.txt"};
		extractNewWord(newWordFiles);// create test data
		//convertToTrainBEMS("data/train.bems", newWordFiles);
		//convertToTrainBEMS("data/train0.bems", basicWordFiles);
		//convertSrcToBEMS("tmp3.txt", new String[]{"data/1_5000_1.segged.txt.src"});
		//convertBEMSToSeg("data/train.bems", "tmp1.txt", "tmp2.txt");
	}
}
