package evaluate;

import static evaluate.Test.readWordList;

/**
 * Created by wan on 5/19/2017.
 */
public class Ner {
	final public static Ner nw = new Ner("new", "", "nw", "nw", "nw");
	final public static Ner ner = new Ner("ner", "ner", "(nr)|(ns)|(nt)", "ner", "ner");
	final public static Ner nr = new Ner("per", "nr", "nr", "ner", "ner");
	final public static Ner ns = new Ner("loc", "ns", "ns", "ner", "ner");
	final public static Ner nt = new Ner("org", "nt", "nt", "ner", "ner");
	final public static Ner nz = new Ner("other", "nz", "nz", "nz", "ner");
	public static Ner[] supported;

	static {
		supported = new Ner[]{nw, nr, ns, nt};
	}

	final public String label;
	final public String pattern;
	final public String model;
	final public String template;
	final public String name;
	public double oov;

	private Ner(String n, String l, String p, String m, String t) {
		name = n;
		label = l;
		pattern = p;
		model = m;
		template = t;
	}

	public static void calcOOV() {
		for (Ner ner : Ner.supported)
			ner.oov = Test.test(
					readWordList(config.getAnswerFile(config.trainData, ner)),
					readWordList(config.getAnswerFile(config.testData, ner)),
					ner, "count", "counter"
			);
	}

	public static void main(String... args) {
		calcOOV();
		for (Ner ner : Ner.supported)
			System.err.println(ner.name + "\t" + ner.oov);
	}
}
