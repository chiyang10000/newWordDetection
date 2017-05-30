package ansj;

import org.ansj.splitWord.analysis.ToAnalysis;

/**
 * Created by wan on 5/8/2017.
 */
public class AnsjTo extends Ansj {
	public AnsjTo() {
		parser = new ToAnalysis();
		parser.setIsNameRecognition(true);
		parser.setIsNumRecognition(true);
		parser.setIsQuantifierRecognition(false);
	}
}
