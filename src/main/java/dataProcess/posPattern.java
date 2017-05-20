package dataProcess;

import evaluate.config;

import java.io.*;

/**
 * Created by wan on 5/19/2017.
 */
public class posPattern {
	CounterMap counterMap = new CounterMap();
	void countPosPattern(String inputFile) {
		try (BufferedReader input = new BufferedReader(new FileReader(inputFile))){
			String line;
			while ((line = input.readLine())!= null) {
				line = line.replace('[', ' ').replaceAll("][^ ]+", " ");
				line = line.replaceAll("[^ ]+/", "");
				String[] segs = line.split(" +");
				for (int i = 0; i < segs.length-2; i++){
					counterMap.incr(String.join("/", segs[i], segs[i+1], segs[i+2]));
				}
				System.out.println(line);
			}
			counterMap.output("tmp/tmp");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void main(String... args) {
		posPattern p = new posPattern();
		p.countPosPattern("tmp/wordCRF.ansj.txt");
	}
}
