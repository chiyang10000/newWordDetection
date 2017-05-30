package Feature;

import com.github.stuxuhai.jpinyin.PinyinException;
import com.github.stuxuhai.jpinyin.PinyinFormat;
import com.github.stuxuhai.jpinyin.PinyinHelper;
import dataProcess.posPattern;
import org.ansj.domain.Term;
import org.ansj.splitWord.Analysis;
import org.ansj.splitWord.analysis.ToAnalysis;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wan on 5/11/2017.
 */
public class CharacterFeature {
	static Analysis ansj;

	static {
		ansj = new ToAnalysis();
		ansj.setIsNumRecognition(true);
		ansj.setIsNameRecognition(false);
		ansj.setIsQuantifierRecognition(false);
	}

	public static List<String> getRes(String sentence) {
		//System.err.println(sentence);
		List<Term> src = ansj.parseStr(sentence).getTerms();
		List<String> res = new ArrayList<>();
		for (int i = 0; i < src.size(); i++) {
			Term term = src.get(i);
			String word = term.getRealName();
			String pos = term.getNatureStr();
			String prePos = "", preprePos = "";
			if (i > 0)
				prePos = src.get(i - 1).getNatureStr().substring(0, 1);
			if (i > 1)
				preprePos = src.get(i - 2).getNatureStr().substring(0, 1);
			String pinying = "";
			String posWindow = String.join("/", preprePos, prePos, pos.substring(0, 1));
			String isRegular = posPattern.renminribao.isDefined(posWindow) ? "T" : "F";
			if (i == 0 || i == 1)
				isRegular = "T";
			try {
				if (word.length() == 1) {
					pinying = PinyinHelper.convertToPinyinString(Character.toString(word.charAt(0)), "", PinyinFormat.WITHOUT_TONE);
					res.add(String.join("\t", Character.toString(word.charAt(0)), word, pos, "S", pinying, isRegular));
				} else if (word.length() == 2) {
					pinying = PinyinHelper.convertToPinyinString(Character.toString(word.charAt(0)), "", PinyinFormat.WITHOUT_TONE);
					res.add(String.join("\t", Character.toString(word.charAt(0)), word, pos, "B", pinying, isRegular));
					pinying = PinyinHelper.convertToPinyinString(Character.toString(word.charAt(1)), "", PinyinFormat.WITHOUT_TONE);
					res.add(String.join("\t", Character.toString(word.charAt(1)), word, pos, "E", pinying, isRegular));
				} else {
					pinying = PinyinHelper.convertToPinyinString(Character.toString(word.charAt(0)), "", PinyinFormat.WITHOUT_TONE);
					res.add(String.join("\t", Character.toString(word.charAt(0)), word, pos, "B", pinying, isRegular));
					for (int k = 1; k < word.length() - 1; k++) {
						pinying = PinyinHelper.convertToPinyinString(Character.toString(word.charAt(k)), "", PinyinFormat.WITHOUT_TONE);
						res.add(String.join("\t", Character.toString(word.charAt(k)), word, pos, "M", pinying, isRegular));
					}
					pinying = PinyinHelper.convertToPinyinString(Character.toString(word.charAt(word.length() - 1)), "",
							PinyinFormat
									.WITHOUT_TONE);
					res.add(String.join("\t", Character.toString(word.charAt(word.length() - 1)), word, pos, "E",
							pinying, isRegular));
				}
			} catch (PinyinException e) {
				e.printStackTrace();
			}
		}
		return res;
	}

	@Override
	public String toString() {
		return super.toString();
	}
}
