package test;

import baseline1.NagaoAlgorithm;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashSet;

/**
 * Created by wan on 4/7/2017.
 */
public class Test {
	public static void test(String golden, String ans){
		int hit = 0, select  = 0, sum =0;
		HashSet<String> goldenAnswer = new HashSet<>();
		BufferedReader reader;
		String word;
		try {
			reader = new BufferedReader(new FileReader(golden));
			while ((word = reader.readLine()) != null) {
				goldenAnswer.add(word);
			}
			sum =goldenAnswer.size();
			reader = new BufferedReader(new FileReader(ans));
			while ((word = reader.readLine()) != null) {
				select++;
				if (goldenAnswer.contains(word))
					hit++;
			}
		} catch (java.io.IOException e) {
			e.printStackTrace();
		}
		System.out.println("test on " + golden);
		System.out.println("select " +select +" hit " + hit + " total " + sum);
		System.out.println("precision: " + (float)hit / select + "\trecall: " + (float)hit/sum);

	}
	public static void main(String... args) {
		int counter = 5;
		for (int i = 1; i<= counter; i++) {
			String[] tmp = {"data/src"+i+".txt"};
			NagaoAlgorithm.applyNagao(tmp, "tmp.txt", "stoplist.txt");
			Test.test("data/ans" + i + ".txt", "tmp.txt");
		}
	}
}
