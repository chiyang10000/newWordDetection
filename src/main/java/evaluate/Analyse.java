package evaluate;

import dataProcess.ConvertHalfWidthToFullWidth;
import dataProcess.Corpus;
import dataProcess.WordInfoInCorpus;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by don on 08/05/2017.
 */
public class Analyse {
	static public void main(String... args) throws IOException {
		//ConvertHalfWidthToFullWidth.convertFileToFulllKeepPos(config.renmingribao, "tmp/tmp");
		//Corpus.convertToSrc(new String[]{"tmp/tmp"}, config.corpusFile);
		Corpus tmp = new Corpus(config.totalData);
		WordInfoInCorpus wordInfoInCorpus = new WordInfoInCorpus(config.totalDataInput);
		PrintWriter writer = new PrintWriter(new FileWriter("data/info/word.info"));
		for (String word: tmp.wordList)
			if (!word.matches(config.newWordExcludeRegex)) {
			String tag = config.renmingribaoWord.isNewWord(word, null) ? "yes" : "no";
				writer.println( String.join("\t", wordInfoInCorpus.addWordInfo(word + "\t"+ word.length()), tag ) );
			}
			writer.close();
	}
}
