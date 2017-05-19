package evaluate;

import java.util.Map;

/**
 * Created by don on 27/04/2017.
 */
public interface NewWordDetector {
	Map<String, String> detectNewWord(String inputFile, String outputFile, Ner ner);
}
