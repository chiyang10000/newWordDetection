package evaluate;

import java.util.Map;

/**
 * Created by wan on 4/27/2017.
 */
public interface NewWordDetector {
	Map<String, String> detectNewWord(String inputFile, String outputFile, Ner ner);
}
