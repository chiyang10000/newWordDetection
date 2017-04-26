package Config;

/**
 * Created by wan on 4/25/2017.
 */
public class Config {
	final public static String sepSentenceRegex = "[°~～\\pP&&[^-.%－·@?]]";
	final public static String sepWordRegex = " ";
	final public static String sepPosRegex = "/";
	final public static String newWordExcludeRegex = ".*[\\p{IsPunct}\\p{IsDigit}\\p{Lower}\\p{Upper}-[?]]+.*";
	final public static String[] newWordFiles = {"data/raw/1_5000_1.segged.txt", "data/raw/1_5000_2.segged.txt",
			"data/raw/1_5000_3.segged.txt", "data/raw/1_5000_4.segged.txt", "data/raw/1_5000_5.segged.txt"};
	final public static String[] basicWordFiles = {"data/raw/2000-01-粗标.txt", "data/raw/2000-02-粗标.txt", "data/raw/2000-03-粗标.txt"};

	final public static double thresholdMI = 1;
	final public static double thresholdTF = 1;
	final public static double thresholdLeftEntropy = 1;
	final public static double thresholdRightEntropy = 1;
	final public static double thresholdLeftNumber = 1;
	final public static double thresholdLeftRightNumber = 1;

	public static void main(String... args) {
		if ("·".matches(sepSentenceRegex))
			System.out.println("yes");
	}
}
