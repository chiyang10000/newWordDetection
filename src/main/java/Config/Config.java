package Config;

/**
 * Created by wan on 4/25/2017.
 */
public class Config {
	final public static String seperatorRegx = "[°~～\\pP&&[^-.%－·@?]]";

	public static void main(String... args) {
		if ("·".matches(seperatorRegx))
			System.out.println("yes");
	}
}
