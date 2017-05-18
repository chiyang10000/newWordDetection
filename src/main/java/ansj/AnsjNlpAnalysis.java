package ansj;

import evaluate.config;
import org.ansj.splitWord.analysis.NlpAnalysis;

/**
 * Created by don on 08/05/2017.
 */
public class AnsjNlpAnalysis extends Ansj {
	public AnsjNlpAnalysis() {
		parser = new NlpAnalysis();
		parser.setIsNameRecognition(true);
		parser.setIsNumRecognition(true);
		parser.setIsQuantifierRecognition(false);
	}
}
