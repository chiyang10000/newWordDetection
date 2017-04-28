package ansj;

import evaluate.Corpus;
import evaluate.NewWordDetector;
import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.NlpAnalysis;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.List;

/**
 * Created by wan on 4/28/2017.
 */
public class Ansj implements NewWordDetector {
	private Logger logger;

	@Override
	public void detect(String inputFile, String outputFile) {
		HashSet<String> newWordList = new HashSet<>();
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
			BufferedReader reader = new BufferedReader(new FileReader(inputFile));
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.length() == 0) {
					writer.newLine();
					continue;
				}
				List<Term> list = NlpAnalysis.parse(line).getTerms();
				for (Term term : list)
					if (Corpus.isNewWord(term.getRealName()) && !newWordList.contains(term.getRealName())) {
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
	}
}
