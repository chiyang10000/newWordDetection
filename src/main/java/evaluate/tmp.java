package evaluate;

import ansj.Ansj;
import dataProcess.Corpus;
import dataProcess.WordInfoInCorpus;
import org.ansj.splitWord.analysis.ToAnalysis;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 用来做测试的
 * Created by wan on 5/8/2017.
 */
public class tmp {
	static public void main(String... args) throws IOException {
		Ansj.segFile(new ToAnalysis(), config.getInputFile(config.totalData), "tmp/tmp");
		if (true)
			return;
		//ConvertHalfWidthToFullWidth.convertFileToFulllKeepPos(config.renmingribao, "tmp/tmp");
		//Corpus.convertToSrc(new String[]{"tmp/tmp"}, config.corpusFile);
		Corpus tmp = new Corpus(config.totalData);
		WordInfoInCorpus wordInfoInCorpus = new WordInfoInCorpus(config.getInputFile(config.totalData));
		PrintWriter writer = new PrintWriter(new FileWriter("data/info/word.info"));
		for (String word : tmp.wordList)
			if (!word.matches(config.newWordExcludeRegex)) {
				String tag = Corpus.isNewWord(word, null) ? "yes" : "no";
				writer.println(String.join("\t", wordInfoInCorpus.addWordInfo(word + "\t" + word.length()), tag));
			}
		writer.close();
	}
}
