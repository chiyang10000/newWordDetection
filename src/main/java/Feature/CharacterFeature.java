package Feature;

import com.github.stuxuhai.jpinyin.PinyinException;
import com.github.stuxuhai.jpinyin.PinyinFormat;
import com.github.stuxuhai.jpinyin.PinyinHelper;
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
		for (int i = 0; i < src.size(); i++) {
			Term term = src.get(i);
			String word = term.getRealName();
			String pos = term.getNatureStr();

			String pinying = "";
			try {
			if (word.length() == 1) {
				pinying = PinyinHelper.convertToPinyinString(Character.toString(word.charAt(0)), "", PinyinFormat .WITHOUT_TONE);
				res.add(String.join("\t", Character.toString(word.charAt(0)), word, pos, "S", pinying));
			} else if (word.length() == 2) {
				pinying = PinyinHelper.convertToPinyinString(Character.toString(word.charAt(0)), "", PinyinFormat .WITHOUT_TONE);
				res.add(String.join("\t", Character.toString(word.charAt(0)), word, pos, "B", pinying));
				pinying = PinyinHelper.convertToPinyinString(Character.toString(word.charAt(1)), "", PinyinFormat .WITHOUT_TONE);
				res.add(String.join("\t", Character.toString(word.charAt(1)), word, pos, "E", pinying));
			} else {
				pinying = PinyinHelper.convertToPinyinString(Character.toString(word.charAt(0)), "", PinyinFormat .WITHOUT_TONE);
				res.add(String.join("\t", Character.toString(word.charAt(0)), word, pos, "B", pinying));
				for (int k = 1; k < word.length() - 1; k++) {
					pinying = PinyinHelper.convertToPinyinString(Character.toString(word.charAt(k)), "", PinyinFormat .WITHOUT_TONE);
					res.add(String.join("\t", Character.toString(word.charAt(k)), word, pos, "M", pinying));
				}
				pinying = PinyinHelper.convertToPinyinString(Character.toString(word.charAt(word.length() - 1)), "",
						PinyinFormat
						.WITHOUT_TONE);
				res.add(String.join("\t", Character.toString(word.charAt(word.length() - 1)), word, pos, "E", pinying));
			}
			} catch (PinyinException e) {
				e.printStackTrace();
			}
		}
		return res;
	}
}
