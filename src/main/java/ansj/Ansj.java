package ansj;

import evaluate.config;
import evaluate.Corpus;
import evaluate.NewWordDetector;
import evaluate.Test;
import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.NlpAnalysis;
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

	public static void main(String... args) {
		Ansj ansj = new Ansj();
		ansj.calcMostRecallInNW("data/test/test.txt.tagNW");
		Test.test(Test.readWordList(config.testDataNWAns), ansj.detectNewWord(config.testDataSrc,
				"tmp/tmp", "nw"));
	}

	@Override
	public Set<String> detectNewWord(String inputFile, String outputFile, String pattern) {
		HashSet<String> newWordList = new HashSet<>();
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
			BufferedReader reader = new BufferedReader(new FileReader(inputFile));
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.length() == 0) {
					continue;
				}
				List<Term> list = NlpAnalysis.parse(line).getTerms();
				for (Term term : list)
					if ((pattern.equals("nw") && Corpus.isNewWord(term.getRealName()) && !newWordList.contains(term.getRealName()))
							|| (term.getNatureStr().equals(pattern) && !newWordList.contains(term.getRealName()) )
							) {
						newWordList.add(term.getRealName());
						writer.append(term.getRealName());
						writer.newLine();
					}
			}
			writer.close();
		} catch (java.io.IOException e) {
			logger.error("count word using ansj error");
			e.printStackTrace();
		}
		return newWordList;
	}

	void calcMostRecallInNW(String inputFile) {
		BufferedReader reader;
		String srcline;
		double mostRecallInTraindata = 0;
		HashSet<String> newWordList = new HashSet<>();
		HashSet<String> validNewWordList = new HashSet<>();
		try {
			newWordList.addAll(Corpus.extractWord(inputFile, "nw"));
			reader = new BufferedReader(new FileReader(inputFile));
			while ((srcline = reader.readLine()) != null) {
				srcline = srcline.replaceAll("/([^ ]*|$)", "");// 去掉词性
				for (String line : srcline.split(config.sepSentenceRegex)) {
					line = line.trim();
					if (line.length() > 0) {
						String[] golden = line.split(config.sepWordRegex);
						List<Term> ansj = config.parser.parseStr(line.replace(" ", "")).getTerms();
						int k = 0;
						String gs = golden[0], as = "";
						for (int i = 0; i < ansj.size(); i++) {// 总保证循环体开始之前 gs包含as, 且gs仅包含一个词，
							Term term = ansj.get(i);
							as += term.getRealName();
							if (gs.equals(as)) {
								if (newWordList.contains(gs))
									validNewWordList.add(gs);
								as = "";
								if (k + 1 < golden.length) {
									gs = golden[++k];
								}
							} else {
								if (!gs.contains(as)) {
									while (!gs.contains(as)) {
										gs += golden[++k];
									}
								}
								if (gs.equals(as)) {
									if (newWordList.contains(gs))
										validNewWordList.add(gs);
									as = "";
									if (k + 1 < golden.length) {
										gs = golden[++k];
									}
								}
							}
						}
					}
				}
			}
			mostRecallInTraindata = (double) validNewWordList.size() / newWordList.size();
			logger.info("valid{} total{} mostRecall is {}", validNewWordList.size(), newWordList.size(),
					mostRecallInTraindata);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
