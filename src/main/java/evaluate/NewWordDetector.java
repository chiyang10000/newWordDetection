package evaluate;

import java.util.Set;

/**
 * Created by don on 27/04/2017.
 */
public interface NewWordDetector {
	Set<String> detectNewWord(String inputFile, String outputFile, String pattern);
}
