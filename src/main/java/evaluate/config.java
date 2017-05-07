package evaluate;

import org.ansj.splitWord.Analysis;
import org.ansj.splitWord.analysis.BaseAnalysis;
import org.ansj.splitWord.analysis.NlpAnalysis;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.ansj.util.MyStaticValue;
import org.ansj.library.DicLibrary;

/**
 * Created by wan on 4/25/2017.
 */
public class config {
	//final public static String sepSentenceRegex = "([【】°~～\\pP&&[^-－.%．·@／]]+)";
	final public static String sepSentenceRegex = "，。？！：";
	final public static String sepWordRegex = " +";
	final public static String sepPosRegex = "/";
	//final public static String newWordExcludeRegex = "(.*[\\p{IsDigit}\\p{Lower}\\p{Upper}-[?]]+.*)" + "|" + ".*" +
	// sepSentenceRegex + ".*";
	final public static String newWordExcludeRegex = ".*[｜■±+\\pP&&[^·－／]]+.*" + "|" +
			"[第型．％：／×—－·～\\p{IsDigit}\\p{IsLatin}\\p{IsCyrillic}]+";
	//标点符号和纯数字
	//final public static String newWordExcludeRegex = ".*[^\\u4E00-\\u9FBF·].*";// 只留下汉字词


	final public static String invalidSuffixRegex = "^(的|是|在|等|与|了)$";
	final public static double thresholdMI = 50;
	final public static double thresholdTF = 1;
	final public static double thresholdNeighborEntropy = 1;
	final public static double thresholdLeftEntropy = 1;
	final public static double thresholdRightEntropy = 1;
	final public static double thresholdLeftNumber = 1;
	final public static double thresholdLeftRightNumber = 1;
	final public static Analysis parser;
	final public static int testSize = 5;
	public static int levelNum = 10;
	public static int maxNagaoLength = 11;

	public static boolean isNagaoLoadedFromFile = false; //new File("data/model/nagao.corpus").exists();
	public static boolean isNagaoSavedIntoFile = false;
	public static boolean isLoadCorpus = false;
	public static boolean isShuffle = false;

	public static String  renmingribao = "data/raw/renminribao.txt";
	public static String  news = "data/raw/news.txt";

	public static String newWordFile = "tmp/input.txt";

	public static String testData = "data/test/test.txt";
	public static String trainData = "data/test/train.txt";
	public static String totalData = "data/test/total.txt";

	public static String testDataSrc = "data/test/input/test.txt.src";
	public static String trainDataSrc = "data/test/input/train.txt.src";
	public static String totalDataSrc = "data/test/input/total.txt.src";

	public static String testDataNWAns = "data/test/ans/test.txt.nw.ans";
	public static String testDataNRAns = "data/test/ans/test.txt.nr.ans";
	public static String testDataNSAns = "data/test/ans/test.txt.ns.ans";

	public static String trainDataNWAns = "data/test/ans/train.txt.nw.ans";
	public static String trainDataNRAns = "data/test/ans/train.txt.nr.ans";
	public static String trainDataNSAns = "data/test/ans/train.txt.ns.ans";

	public static String corpusFile = "data/corpus/train.txt.words";
	public static String nw = "nw", nr = "nr", ns = "ns";

	final public static String[] newWordFiles = {newWordFile};
	//final public static String[] newWordFiles = {"data/raw/1_5000_1.segged.txt", "data/raw/1_5000_2.segged.txt",
	//		"data/raw/1_5000_3.segged.txt", "data/raw/1_5000_4.segged.txt", "data/raw/1_5000_5.segged.txt"};
	final public static String[] basicWordFiles = {"data/raw/2000-01-粗标.txt", "data/raw/2000-02-粗标.txt",
			"data/raw/2000-03-粗标.txt"};

	static {
		MyStaticValue.isRealName = true;// ansj不进行大小写转换
		MyStaticValue.isNumRecognition = true;
		MyStaticValue.isQuantifierRecognition = false;
		new BaseAnalysis();
		new NlpAnalysis();
		parser = new ToAnalysis();
	}

	public static String removePos(String in) {
		return in.replaceAll("/[^/]*$", "");
	}

	public static String getPos(String in) {
		return in.replaceAll("^.*/", "");
	}

	public static void main(String... args) {
		System.out.println(removePos("a/b//l"));
		System.out.println(Double.parseDouble("-Infinity"));
		System.out.println(Double.NEGATIVE_INFINITY);
		System.out.println("７".matches(newWordExcludeRegex));
		System.out.println("Ｐ".matches(newWordExcludeRegex));
		System.out.println("Ｐ－７".matches(newWordExcludeRegex));
		System.out.println("Семёрка".matches(newWordExcludeRegex));
		System.out.println("你".matches("\\p{IsHan}"));
	}
}
