package ansj;

import evaluate.config;
import org.ansj.splitWord.analysis.ToAnalysis;

/**
 * Created by don on 08/05/2017.
 */
public class AnsjTo extends Ansj {
	public AnsjTo() {
		parser = new ToAnalysis();
		parser.setIsNameRecognition(true);
		parser.setIsNumRecognition(true);
		parser.setIsQuantifierRecognition(false);
	}
}
