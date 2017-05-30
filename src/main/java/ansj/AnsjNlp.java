package ansj;

import org.ansj.splitWord.analysis.NlpAnalysis;

/**
 * Created by wan on 5/8/2017.
 */
public class AnsjNlp extends Ansj {
	public AnsjNlp() {
		parser = new NlpAnalysis();
		parser.setIsNameRecognition(true);
		parser.setIsNumRecognition(true);
		parser.setIsQuantifierRecognition(false);
	}
}
