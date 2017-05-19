package evaluate;

/**
 * Created by don on 19/05/2017.
 */
public class Ner {
	public static Ner[] supported;
	final public static Ner nw = new Ner("", "nw", "nw");
	final public static Ner nr = new Ner("P", "nr", "ner");
	final public static Ner ns = new Ner("L", "ns", "ner");
	final public static Ner nt = new Ner("O", "nt", "ner");
	final public static Ner nz = new Ner("Z", "nz","ner");
	public String label;
	public String pattern;
	public String model;

	static {
		supported = new Ner[]{nw, nr, ns, nt};
	}

	Ner(String l, String p, String m) {
		label = l;
		pattern = p;
		model = m;
	}
}
