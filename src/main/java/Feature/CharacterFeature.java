package Feature;

import evaluate.config;
import org.ansj.domain.Term;
import org.ansj.splitWord.Analysis;
import org.ansj.splitWord.analysis.ToAnalysis;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by don on 11/05/2017.
 */
public class CharacterFeature {
	static Analysis ansj;

	static {
		ansj = new ToAnalysis();
		ansj.setIsNumRecognition(true);
		ansj.setIsNameRecognition(false);
		ansj.setIsQuantifierRecognition(false);
	}

	char c;
	String pos;
	String loc;
	String word;
	String preword, postword, preWordPos, postWordPost;

	CharacterFeature(String input) {

	}


	@Override
	public String toString() {
		return super.toString();
	}

	public static List<String> getRes(String sentence) {
		//System.err.println(sentence);
		List<Term> src = ansj.parseStr(sentence).getTerms();
		List<String> res = new ArrayList<>();
		//String preWordPos = "^", preWord = "^";
		for (int i = 0; i < src.size(); i++) {
			Term term = src.get(i);
			String word = term.getRealName();
			String pos = term.getNatureStr();
			//String postWord = i != src.size() - 1 ? src.get(i + 1).getRealName() : "$";
			//String postWordPos = i != src.size() - 1 ? src.get(i + 1).getNatureStr() : "$";

			if (word.length() == 1) {
				res.add(String.join("\t", Character.toString(word.charAt(0)), word, pos, "S"));
			} else if (word.length() == 2) {
				//res.add(String.join("\t", Character.toString(word.charAt(0)), word, pos, "B", preWord, preWordPos, postWord, postWordPos));
				res.add(String.join("\t", Character.toString(word.charAt(0)), word, pos, "B"));
				res.add(String.join("\t", Character.toString(word.charAt(1)), word, pos, "E"));
			} else {
				res.add(String.join("\t", Character.toString(word.charAt(0)), word, pos, "B"));
				for (int k = 1; k < word.length() - 1; k++)
					res.add(String.join("\t", Character.toString(word.charAt(k)), word, pos, "M"));
				res.add(String.join("\t", Character.toString(word.charAt(word.length() - 1)), word, pos, "E"));
			}
			//preWord = word;
			//preWordPos = pos;
		}
		return res;
	}
}
