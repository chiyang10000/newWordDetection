package ansj;

import evaluate.config;
import org.ansj.splitWord.analysis.NlpAnalysis;

/**
 * Created by don on 08/05/2017.
 */
public class AnsjNlp extends Ansj {
	public AnsjNlp() {
		parser = new NlpAnalysis();
		parser.setIsNameRecognition(true);
		parser.setIsNumRecognition(true);
		parser.setIsQuantifierRecognition(false);
	}
}
