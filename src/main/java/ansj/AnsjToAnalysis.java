package ansj;

import evaluate.config;
import org.ansj.splitWord.analysis.ToAnalysis;

/**
 * Created by don on 08/05/2017.
 */
public class AnsjToAnalysis extends Ansj {
	public AnsjToAnalysis() {
		parser = new ToAnalysis();
		parser.setIsNameRecognition(true);
		parser.setIsNumRecognition(true);
		parser.setIsQuantifierRecognition(false);
	}
}
