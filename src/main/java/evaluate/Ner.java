package evaluate;

/**
 * Created by don on 19/05/2017.
 */
public class Ner {
	public static Ner[] supported;
	final public static Ner nw = new Ner("", "nw", "nw", "nw");
	final public static Ner ner = new Ner("ner", "(nr)|(ns)|(nt)", "ner", "ner");
	final public static Ner nr = new Ner("nr", "nr", "ner", "ner");
	final public static Ner ns = new Ner("ns", "ns", "ner", "ner");
	final public static Ner nt = new Ner("nt", "nt", "ner", "ner");
	final public static Ner nz = new Ner("nz", "nz","nz", "ner");
	final public String label;
	final public String pattern;
	final public String model;
	final public String template;

	static {
		supported = new Ner[]{nw, nr, ns, nt};
	}

	private Ner(String l, String p, String m, String t) {
		label = l;
		pattern = p;
		model = m;
		template = t;
	}
}
