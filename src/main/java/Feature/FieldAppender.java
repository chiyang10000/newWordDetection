package Feature;

/**
 * Created by wan on 5/17/2017.
 * 把每一行的信息按列拼起来
 */
public class FieldAppender {
	StringBuilder[] stringBuilders;

	public FieldAppender(String line) {
		String[] tmp = line.split("\t");
		stringBuilders = new StringBuilder[tmp.length];
		for (int i = 0; i < tmp.length; i++) {
			stringBuilders[i] = new StringBuilder();
			stringBuilders[i].append(tmp[i]);
		}
	}

	public void append(String line) {
		String[] tmp = line.split("\t");
		for (int i = 0; i < tmp.length; i++) {
			stringBuilders[i].append("/");
			stringBuilders[i].append(tmp[i]);
		}
	}

	@Override
	public String toString() {
		StringBuilder tmp = new StringBuilder();
		for (int i = 0; i < stringBuilders.length; i++) {
			tmp.append(stringBuilders[i]);
			tmp.append("\t");
		}
		return tmp.toString();
	}
}
