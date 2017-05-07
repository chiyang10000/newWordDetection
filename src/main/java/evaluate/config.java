package evaluate;

/**
 * Created by wan on 4/25/2017.
 */
public interface config {
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
	final public static int testSize = 5;
	public static int levelNum = 10;
	public static int maxNagaoLength = 11;

	public static boolean isNagaoLoadedFromFile = false; //new File("data/model/nagao.corpus").exists();
	public static boolean isNagaoSavedIntoFile = false;
	public static boolean isLoadCorpus = false;
	public static boolean isShuffle = false;

	public static String renmingribao = "data/raw/renminribao.txt";
	//final public static String[] newWordFiles = {"data/raw/1_5000_1.segged.txt", "data/raw/1_5000_2.segged.txt",
	//		"data/raw/1_5000_3.segged.txt", "data/raw/1_5000_4.segged.txt", "data/raw/1_5000_5.segged.txt"};
	final public static String[] basicWordFiles = {renmingribao};
	public static String news = "data/raw/news.txt";
	public static String newWordFile = "tmp/input.txt";
	final public static String[] newWordFiles = {newWordFile};
	public static String testData = "data/test/test.txt";
	public static String trainData = "data/test/train.txt";
	public static String totalData = "data/test/total.txt";
	public static String testDataInput = "data/test/input/test.txt.src";
	public static String trainDataInput = "data/test/input/train.txt.src";
	public static String totalDataInput = "data/test/input/total.txt.src";
	public static String corpusFile = "data/corpus/train.txt.words";
	public static String nw = "nw", nr = "nr", ns = "ns";
	public static String[] supportedType = new String[]{nw};


	public static String removePos(String in) {
		return in.replaceAll("/[^/]*$", "");
	}

	public static String getPos(String in) {
		return in.replaceAll("^.*/", "");
	}

	public static String getAnswerFile(String inputFile, String pattern) {
		return "data/test/ans/" + inputFile.replaceAll(".*/", "") + "." + pattern;
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
		System.out.println(getAnswerFile(testDataInput, nw));
	}
}
