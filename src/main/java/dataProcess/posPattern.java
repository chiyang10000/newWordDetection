package dataProcess;

import ansj.Ansj;
import evaluate.config;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashSet;

/**
 * Created by wan on 5/19/2017.
 */
public class posPattern {
	private static Logger logger = LoggerFactory.getLogger(posPattern.class);
	public static posPattern renminribao = new posPattern(config.renmingribao);
	CounterMap counterMap = new CounterMap();

	private posPattern(String inputFile) {
		readPosPattern(inputFile);
	}

	static void countPosPattern(String inputFile, posPattern exist) {
		HashSet<String> kk = new HashSet<>();
		try (BufferedReader input = new BufferedReader(new FileReader(inputFile))) {
			String line;
			int tt = 0;
			while ((line = input.readLine()) != null) {
				line = line.replace('[', ' ').replaceAll("][^ ]+", " ");
				String[] jj = line.split(" +");
				line = line.replaceAll("[^ ]+/", "");
				String[] segs = line.split(" +");
				for (int i = 0; i < segs.length - 2; i++) {
					String tmp = String.join("/", segs[i], segs[i + 1], segs[i + 2]);
					tmp = String.join("/", segs[i].substring(0, 1), segs[i + 1].substring(0, 1), segs[i + 2].substring(0, 1));
					if (!segs[i].matches("[\\p{IsLatin}]+"))
						continue;
					if (kk.contains(tmp))
						continue;
					if (!tmp.contains("w"))
						if (!tmp.contains("u"))
							if (!tmp.contains("y"))
								if (!exist.isDefined(tmp)) {
									kk.add(tmp);
									System.err.println(jj[i] + jj[i + 1] + jj[i + 2]);
									tt++;
								}
				}
				//System.out.println(line);
			}
			System.err.println(tt);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String... args) {
		Ansj.segFile(new ToAnalysis(), config.getInputFile(config.totalData), "tmp/ansj.txt");
		posPattern p1 = new posPattern(config.renmingribao);
		posPattern p2 = new posPattern(config.totalData);
		countPosPattern("tmp/ansj.txt", p1);
		//countPosPattern(config.totalData, p1);
	}

	private void readPosPattern(String inputFile) {
		String tmp = "data/corpus/" + inputFile.replaceAll("^.*/", "") + ".posPattern";
		if (new File(tmp).exists()) {
			logger.debug("read pos pattern from {}", tmp);
			counterMap.input(tmp);

		} else countPosPattern(inputFile);
	}

	private void countPosPattern(String inputFile) {
		logger.debug("counting pos pattern from {}", inputFile);
		try (BufferedReader input = new BufferedReader(new FileReader(inputFile))) {
			String line;
			while ((line = input.readLine()) != null) {
				line = line.replace('[', ' ').replaceAll("][^ ]+", " ");
				//line = line.replaceAll("/nx", "/en");
				line = line.replaceAll("[^ ]+/", "").trim().toLowerCase();
				String[] segs = line.split(" +");
				//System.out.println(line);
				for (int i = 0; i < segs.length - 2; i++) {
					String tmp = String.join("/", segs[i], segs[i + 1], segs[i + 2]);
					tmp = String.join("/", segs[i].substring(0, 1), segs[i + 1].substring(0, 1), segs[i + 2].substring(0, 1));
					counterMap.incr(tmp);
					tmp = String.join("/", segs[i], segs[i + 1]);
					counterMap.incr(tmp);
					counterMap.incr(segs[i]);
				}
			}
			String tmp = "data/corpus/" + inputFile.replaceAll("^.*/", "") + ".posPattern";
			counterMap.output(tmp);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean isDefined(String tmp) {
		return counterMap.get(tmp) > 2;
	}

	public int count(String tmp) {
		return counterMap.get(tmp);
	}
}
