package evaluate;

import dataProcess.Corpus;
import dataProcess.WordInfoInCorpus;

/**
 * Created by don on 08/05/2017.
 */
public class Analyse {
	static public void main(String... args) {
		Corpus.extractWord(config.totalData, config.nw);
		//WordInfoInCorpus.addWordInfo();
	}
}
