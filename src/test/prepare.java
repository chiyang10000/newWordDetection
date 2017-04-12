package test;

import java.io.*;
import java.util.HashSet;

/**
 * Created by wan on 4/7/2017.
 */
public class prepare {
	public static void main(String... args) {
		HashSet<String> basicWordList = new HashSet<>(), newWordList;
		String[] basicWordFiles = {"data/2000-01-粗标.txt", "data/2000-02-粗标.txt", "data/2000-03-粗标.txt"};
		String[] newWordFiles = {"data/1_5000_1.segged.txt", "data/1_5000_2.segged.txt", "data/1_5000_3.segged.txt", "data/1_5000_4.segged.txt", "data/1_5000_5.segged.txt"};

		// extract the basic word list
		for (String basicWordFile : basicWordFiles) {
			try {
				BufferedReader reader = new BufferedReader(new FileReader(basicWordFile));
				String tmp;
				while ((tmp = reader.readLine()) != null) {
					String[] segs = tmp.split(" ");
					for (String word : segs)
						basicWordList.add(word.split("/")[0]);
				}
			} catch (java.io.IOException e) {
				e.printStackTrace();
				System.err.println("reading file error");
			}
		}
		System.out.println("basic word list size: " + basicWordList.size());
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter("data/basicWordList.txt"));
			for (String word : basicWordList) {
				writer.append(word);
				writer.newLine();
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("writing file error");
		}

		// transfer source to ans.txt and src.txt
		int counter = 0;
		try {
			for (String newWordFile : newWordFiles) {
				counter++;
				newWordList = new HashSet<>();
				BufferedWriter srcWriter = new BufferedWriter(new FileWriter("data/src" + counter + ".txt")), ansWriter = new BufferedWriter(new FileWriter("data/ans" + counter + ".txt"));
				BufferedReader reader = new BufferedReader(new FileReader(newWordFile));
				String tmp;
				while ((tmp = reader.readLine()) != null) {
					String[] segs = tmp.split(" ");
					for (String seg: segs) {
						String word = (seg.split("/")[0]);
						srcWriter.append(word);
						if (!basicWordList.contains(word))
							newWordList.add(word);
					}
				srcWriter.newLine();
				}
				for (String word: newWordList) {
					ansWriter.append(word);
					ansWriter.newLine();
				}
				System.out.println(newWordFile + " got new words: " + newWordList.size());
				srcWriter.close();
				ansWriter.close();
			}
		} catch (java.io.IOException e) {
			e.printStackTrace();
			System.err.println("create test data error");
		}
	}
}
