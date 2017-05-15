package ansj;

import evaluate.config;
import org.ansj.splitWord.analysis.ToAnalysis;

/**
 * Created by don on 08/05/2017.
 */
public class AnsjToAnalysis extends Ansj {
	public AnsjToAnalysis() {
		config.openAnsj();
		parser = new ToAnalysis();
		parser.setIsNameRecognition(true);
		parser.setIsNumRecognition(true);
	}
}
