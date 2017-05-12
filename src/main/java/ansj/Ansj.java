package ansj;

import dataProcess.Corpus;
import evaluate.NewWordDetector;
import evaluate.Test;
import evaluate.config;
import org.ansj.domain.Term;
import org.ansj.splitWord.Analysis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by wan on 4/28/2017.
 */
public class Ansj implements NewWordDetector {
	private static Logger logger = LoggerFactory.getLogger(Ansj.class);
	Analysis parser;

	public static void main(String... args) throws IOException {
		//segFileForWord2Vec(config.totalDataInput, "tmp/char.txt", "tmp/word.txt");
		config.closeAnsj();
		Ansj ansj = new AnsjToAnalysis();
		ansj.segFile(config.totalDataInput, "ansj.txt");
		String type = config.nr;
		Test.test(Test.readWordList(Test.getAnswerFile(config.testDataInput, type)), ansj.detectNewWord(config.testDataInput,
				"tmp/tmp." + type, type), ansj.getClass().getSimpleName() + " " + type);
	}

	void segFile(String input, String output) throws IOException {
		try (BufferedReader reader = new BufferedReader(new FileReader(input));
			 BufferedWriter writer = new BufferedWriter(new FileWriter(output))
		) {
			String line, tmp;
			while ((line = reader.readLine()) != null) {
				tmp = parser.parseStr(line).toString(" ");
				writer.append(tmp);
				writer.newLine();
			}
		}
	}

	void segFileForWord2Vec(String input, String output1, String output2) throws IOException {
		try (BufferedReader reader = new BufferedReader(new FileReader(input));
			 BufferedWriter writer1 = new BufferedWriter(new FileWriter(output1));
			 BufferedWriter writer2 = new BufferedWriter(new FileWriter(output2))
		) {
			String line, tmp;
			while ((line = reader.readLine()) != null) {
				if (line.length() == 0) continue;
				tmp = parser.parseStr(line).toStringWithOutNature(" ");
				for (int i = 0; i < line.length(); i++)
					writer1.append(line.charAt(i) + " ");
				writer1.newLine();
				writer2.append(tmp);
				writer2.newLine();
			}
		}

	}

	@Override
	public Set<String> detectNewWord(String inputFile, String outputFile, String pattern) {
		config.openAnsj();
		HashSet<String> newWordList = new HashSet<>();
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
			BufferedReader reader = new BufferedReader(new FileReader(inputFile));
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.length() == 0) {
					continue;
				}
				List<Term> list = parser.parseStr(line).getTerms();
				for (Term term : list) {
					String word = term.getRealName(), pos = term.getNatureStr();
					if (pattern == config.nw) {
						word = config.newWordFileter(word);
						if ((Corpus.isNewWord(word, pos)) && !newWordList.contains(word)
								) {
							newWordList.add(word);
							writer.append(word + "\t" + pos);
							writer.newLine();
						}
					}// nw
					if (pattern == config.nr || pattern == config.ns) {
						if (pos.equals(pattern) && !newWordList.contains(word)) {
							newWordList.add(word);
							writer.append(word);
							writer.newLine();
						}
					} // nr ns
				}
			}
			writer.close();
		} catch (java.io.IOException e) {
			logger.error("count word using ansj error");
			e.printStackTrace();
		}
		return newWordList;
	}

}
