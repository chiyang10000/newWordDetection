package evaluate;

import org.nlpcn.commons.lang.util.ObjConver;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import static evaluate.Test.readWordList;
import static evaluate.Test.test;

/**
 * Created by wan on 4/25/2017.
 */
public class config {


	final public static String sepSentenceRegex = "[，。？！：]";// 这样搞了之后就断不开了
	final public static String sepWordRegex = " +";

	final public static double thresholdMI = 10;
	final public static double thresholdTF = 3;
	final public static double thresholdNeighborEntropy = 1.5;
	final public static Integer testSize = 5;
	public static final String timeRegx =
			"([\\p{IsDigit}－～：兆亿万千百十九八七六五四三二一零○]+" +
					"((([年月日]|(世纪)|(年代))[前初中底末]?)|[号时分秒点]|(秒钟)|(点钟)|(月份)|(小时)))";
	public static final String pureLetterStringRegex = "([\\p{IsLatin}\\p{IsCyrillic}]+)";
	public static final String pureNumStringRegex = "(第?[兆亿万千百\\p{IsDigit}，．％∶：／×－＋·～]+)";
	public static final String letterWithNumStringRegex = "([\\p{IsDigit}\\p{IsCyrillic}\\p{IsLatin}．／－·～]+)";
	final public static String punctuationStringRegex = "([　°～｜■＋±\\pP&&[^·－／]]+)";
	final public static String newWordExcludeRegex = String.join("|"
			, pureNumStringRegex
			, pureLetterStringRegex
			, timeRegx
			, punctuationStringRegex);
	public static final String pureChineseStringRegex = "([\\p{IsHan}]+)";
	public static final String chineseJoinedStringRegex = "([\\p{IsHan}·－／]+)";
	final public static String news = "data/raw/news.txt";
	final public static String renmingribao = "data/raw/renminribao.txt";
	public static String comment;//report.log里面的注释
	public static String newWordRemove;
	public static Integer levelNum = 8;
	public static Integer maxStringLength = 8;
	public static Boolean isShuffle = false;
	public static Boolean isNewWordFilter = true;
	public static Boolean isCRFsuite = false;//设置用crfsuite还是crf++
	public static String algorithmInCRFSuite = "";//crfsuite里面用的算法
	public static String newWordFile = "tmp/newword.DBC";

	public static String testData = "data/test/test.txt";
	public static String trainData = "data/test/train.txt";
	public static String totalData = "data/test/total.txt";

	public static Set<String> trainModelList = new HashSet<>();
	public static Set<String> testModelList = new HashSet<>();

	static {
		Properties prop = new Properties();
		try {
			//读取属性文件
			FileInputStream input = new FileInputStream(new File("config.properties"));
			prop.load(new InputStreamReader(input, Charset.forName("UTF-8")));     ///加载属性列表
			Iterator<String> it = prop.stringPropertyNames().iterator();
			while (it.hasNext()) {
				String key = it.next();
				//train和的test哪些模型
				if (key.equals("train")) {
					String[] tmp = prop.getProperty(key).split(",");
					for (String tmp0 : tmp) {
						trainModelList.add(tmp0);
					}
					continue;
				}
				if (key.equals("test")) {
					String[] tmp = prop.getProperty(key).split(",");
					for (String tmp0 : tmp)
						testModelList.add(tmp0);
					continue;
				}
				Field field = config.class.getField(key);
				field.set(null, ObjConver.conversion(prop.getProperty(key), field.getType()));
			}
			input.close();
		} catch (Exception e) {
			System.err.println(e);
		}
		String[] dirList = {
				"info"
				, "tmp"
				, "tmp/crf"
				, "data"
				, "data/corpus"
				, "data/corpus/wordlist"
				, "data/model"
				, "data/test"
				, "data/test/input"
				, "data/test/ans"};//初始化的时候要把这些文件夹建起来
		for (String dir : dirList)
			if (!new File(dir).exists()) {
				new File(dir).mkdir();
			}
	}

	public static String removePos(String in) {
		return in.replaceAll("/[^/]*$", "");
	}

	public static String getPos(String in) {
		return in.replaceAll("^.*/", "");
	}

	static public String newWordFileter(String word) {
		if (isNewWordFilter)
			return word.replaceAll(newWordRemove, "");
		return word;
	}

	public static void main(String... args) {
		for (String tmp : new String[]{"21", "20世纪末", "90年代", "5月", "四月中", "三点钟", "０３：２８"})
			System.err.println(tmp + "\t " + tmp.matches(timeRegx));
		for (Ner type : Ner.supported) {
			test(
					readWordList(config.getAnswerFile(config.trainData, type)),
					readWordList(config.getAnswerFile(config.testData, type)),
					type, "count", "count"
			);
		}
	}

	/**
	 * 确定这个串的类型
	 * @param word
	 * @return
	 */
	static public String category(String word) {
		if (word.matches(pureLetterStringRegex))
			return "letter";
		if (word.matches(pureNumStringRegex))
			return "num";
		if (word.matches(letterWithNumStringRegex))
			return "letter-num";
		if (word.matches(pureChineseStringRegex))
			return "chinese";
		if (word.matches(chineseJoinedStringRegex))
			return "chineseWithHyphen";
		return "mix_string";
	}

	public static String getAnswerFile(String inputFile, Ner ner) {
		return "data/test/ans/" + inputFile.replaceAll(".*/", "") + "." + ner.name;
	}

	public static String getInputFile(String inputFile) {
		return "data/test/input/" + inputFile.replaceAll(".*/", "") + ".src";
	}

	public static String getWordListFile(String inputFile) {
		return "data/corpus/wordlist/" + inputFile.replaceAll(".*/", "") + ".wordlist";
	}
}
