package evaluate;

import com.github.stuxuhai.jpinyin.PinyinException;
import com.github.stuxuhai.jpinyin.PinyinFormat;
import com.github.stuxuhai.jpinyin.PinyinHelper;
import dataProcess.Corpus;
import dataProcess.WordInfoInCorpus;
import org.nlpcn.commons.lang.util.ObjConver;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Properties;

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

	public static final String timeRegx = "([\\p{IsDigit}：兆亿万千百十九八七六五四三二一]+" +
			"(([年月日][前初中底末]?)|[号时分秒点]|(秒钟)|(点钟)|(月份)|(世纪)|(年代)|(小时))?)";
	public static final String pureLetterStringRegex = "([\\p{IsLatin}\\p{IsCyrillic}]+)";
	public static final String pureNumStringRegex = "(第?[兆亿万千百\\p{IsDigit}，．％：∶／×－＋·～]+)";
	public static final String letterWithNumStringRegex = "([\\p{IsDigit}\\p{IsCyrillic}\\p{IsLatin}．／－·～]+型?)";
	final public static String punctExcludeRegx = "([　°～｜■＋±\\pP&&[^·－／]]+)";
	final public static String newWordExcludeRegex = String.join("|", pureNumStringRegex, pureLetterStringRegex,
			letterWithNumStringRegex, timeRegx, punctExcludeRegx);

	public static final String pureChineseStringRegex = "([\\p{IsHan}]+)";
	public static final String chineseJoinedStringRegex = "([\\p{IsHan}·－／]+)";

	public static Integer levelNum = 10;
	public static Integer maxStringLength = 8;

	public static boolean isNagaoLoadedFromFile = false; //new File("data/model/nagao.corpus").exists();
	public static boolean isNagaoSavedIntoFile = false;
	public static boolean isLoadCorpus = false;
	public static Boolean isTrain = true;
	public static Boolean isShuffle = false;
	public static Boolean isNewWordFilter = true;
	public static Boolean isCRFsuite = true;
	public static String algorithm = "ap";


	final public static String news = "data/raw/news.txt";
	final public static String renmingribao = "data/raw/renminribao.txt";
	public static String newWordFile = "tmp/newword.DBC";
	public static String corpusFile = "data/raw/renminribao.noPos.DBC";
	final public static String testData = "data/test/test.txt";
	final public static String trainData = "data/test/train.txt";
	final public static String totalData = "data/test/total.txt";
	public static String testDataInput = "data/test/input/test.txt.src";
	public static String trainDataInput = "data/test/input/train.txt.src";
	public static String totalDataInput = "data/test/input/total.txt.src";

	static {
		Properties prop = new Properties();
		try {
			//读取属性文件a.properties
			InputStream in = new BufferedInputStream(new FileInputStream("config.properties"));
			prop.load(in);     ///加载属性列表
			Iterator<String> it = prop.stringPropertyNames().iterator();
			while (it.hasNext()) {
				String key = it.next();
				Field field = config.class.getField(key);
				field.set(null, ObjConver.conversion(prop.getProperty(key), field.getType()));
				System.out.println(key + "=" + prop.getProperty(key));
			}
			in.close();
		} catch (Exception e) {
			System.err.println(e);
		}
		RunSystemCommand.run("mkdir tmp");
		RunSystemCommand.run("mkdir tmp/crf");
		RunSystemCommand.run("mkdir data");
		RunSystemCommand.run("mkdir data/corpus");
		RunSystemCommand.run("mkdir data/corpus/wordlist");
		RunSystemCommand.run("mkdir data/model");
		RunSystemCommand.run("mkdir data/test");
		RunSystemCommand.run("mkdir data/test/input");
		RunSystemCommand.run("mkdir data/test/ans");
	}

	final public static Corpus renmingribaoWord = new Corpus(renmingribao);
	public static WordInfoInCorpus wordInfoInCorpus_total ;
	//public static WordInfoInCorpus wordInfoInCorpus_corpus = new WordInfoInCorpus(config.corpusFile);


	//static public final String basicWordListFile = "data/corpus/basicWordList.txt";

	public static String removePos(String in) {
		return in.replaceAll("/[^/]*$", "");
	}

	public static String getPos(String in) {
		return in.replaceAll("^.*/", "");
	}

	static public String newWordFileter(String word) {
		if (isNewWordFilter)
			return word.replaceAll("(公司$)", "");
		return word;
	}

	public static void main(String... args) {
		System.err.println("15小时".matches(timeRegx));
		System.out.println(removePos("a/b//l"));
		System.out.println(Double.parseDouble("-Infinity"));
		System.out.println(Double.NEGATIVE_INFINITY);
		System.out.println("７".matches(newWordExcludeRegex));
		System.out.println("Ｐ".matches(newWordExcludeRegex));
		System.out.println("１∶１００".matches(".*∶.*") + "1:100");
		System.out.println("Семёрка".matches(newWordExcludeRegex));
		System.out.println("你".matches("\\p{IsHan}"));
		if ("指令／秒".matches("[\\p{IsHan}·－／]+"))
			try {
				String tmp = PinyinHelper.convertToPinyinString("ak艾克", ",", PinyinFormat.WITH_TONE_NUMBER);
				System.out.println(tmp);
			} catch (PinyinException e) {
				e.printStackTrace();
			}
	}

	static public String category(String word) {
		if (word.matches(pureLetterStringRegex))
			return "纯字母";
		if (word.matches(pureNumStringRegex))
			return "纯数字";
		if (word.matches(letterWithNumStringRegex))
			return "字母和数字连字符组合";
		if (word.matches(pureChineseStringRegex))
			return "纯汉字";
		if (word.matches(chineseJoinedStringRegex))
			return "汉字加连字符斜杠分隔符";
		return "混合";
	}

	public static String getAnswerFile(String inputFile, Ner ner) {
		return "data/test/ans/" + inputFile.replaceAll(".*/", "") + "." + ner.pattern;
	}

	public static String getWordListFile(String inputFile) {
		return "data/corpus/wordlist/" + inputFile.replaceAll(".*/", "") + ".wordlist";
	}
}
