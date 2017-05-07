package evaluate;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by don on 04/05/2017.
 */
public class ConvertHalfWidthToFullWidth {
	/**
	 * 转全角的函数(DBC case)
	 * 任意字符串
	 * 全角字符串
	 * 全角空格为12288，半角空格为32
	 * 其他字符半角(33-126)与全角(65281-65374)的对应关系是：均相差65248
	 */
	public static String ToDBC(String input) {
		char[] c = input.toCharArray();
		for (int i = 0; i < c.length; i++) {
			if (c[i] == 32) {
				// c[i] = (char) 12288; //不转空格
				continue;
			}
			if (c[i] == 12288) {
				c[i] = (char) 32;
				continue;
			}
			if (c[i] < 127)
				c[i] = (char) (c[i] + 65248);
		}
		return new String(c);
	}


	/**
	 * 转半角的函数(SBC case)
	 * 任意字符串
	 * 半角字符串
	 * 全角空格为12288，半角空格为32
	 * 其他字符半角(33-126)与全角(65281-65374)的对应关系是：均相差65248
	 */
	public static String ToSBC(String input) {
		char[] c = input.toCharArray();
		for (int i = 0; i < c.length; i++) {
			if (c[i] == 12288) {
				c[i] = (char) 32;
				continue;
			}
			if (c[i] > 65280 && c[i] < 65375) {
				c[i] = (char) (c[i] - 65248);
				//if (!('0' <= c[i] && c[i] <= '9' || 'a' <= c[i] && c[i] <= 'z' || 'A' <= c[i] && c[i] <= 'Z'))
				// 只转数字字母
				//	c[i] = (char) (c[i] + 65248);
			}
		}
		return new String(c);
	}

	public static void convertFileToFulll(String inputFile, String outputFile) throws IOException {
		try (
				BufferedReader reader = new BufferedReader(new FileReader(inputFile));
				BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
			String line;
			Pattern posPattern = Pattern.compile("／[^／]+$");
			Matcher m;
			while ((line = reader.readLine()) != null) {
				line = ToDBC(line).replace('—', '－');//.replace("");
				String[] segs = line.split(" +");
				StringBuffer stringBuffer = new StringBuffer();
				for (String seg : segs) {
					m = posPattern.matcher(seg);
					//System.out.println(seg);
					if (m.find()) {
						String pos = m.group();
						//System.out.print(pos);
						//System.out.print(ToSBC(pos));
						stringBuffer.append(seg.replace(pos, ToSBC(pos)));
					} else
						stringBuffer.append(seg);
					stringBuffer.append(" ");
				}
				writer.append(stringBuffer.toString().trim());
				writer.newLine();
			}
		}
	}

	public static void main(String... args) throws IOException {
		convertFileToFulll(config.news, "tmp/full.txt");
	}
}
