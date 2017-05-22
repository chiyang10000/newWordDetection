package dataProcess;

import ansj.Ansj;
import evaluate.config;
import org.ansj.splitWord.analysis.ToAnalysis;

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
				line = line.replaceAll("[^ ]+/", "").trim();
				String[] segs = line.split(" +");
				//System.out.println(line);
				for (int i = 0; i < segs.length-2; i++){
					String tmp = String.join("/", segs[i], segs[i+1], segs[i+2]);
					tmp = String.join("/", segs[i].substring(0,1), segs[i+1].substring(0,1), segs[i+2].substring(0, 1));
					counterMap.incr(tmp);
				}
			}
			counterMap.output("tmp/"+inputFile.replaceAll("^.*/", "") + ".posPattern");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	void countPosPattern(String inputFile, posPattern exist) {
		try (BufferedReader input = new BufferedReader(new FileReader(inputFile))){
			String line;
			int tt =0 ;
			while ((line = input.readLine())!= null) {
				line = line.replace('[', ' ').replaceAll("][^ ]+", " ");
				line = line.replaceAll("/en", "/nx");
				String[] jj = line.split(" +");
				line = line.replaceAll("[^ ]+/", "");
				String[] segs = line.split(" +");
				for (int i = 0; i < segs.length-2; i++){
					String tmp = String.join("/", segs[i], segs[i+1], segs[i+2]);
					tmp = String.join("/", segs[i].substring(0,1), segs[i+1].substring(0,1), segs[i+2].substring(0,1));
					counterMap.incr(tmp);
					if (!segs[i].matches("[\\p{IsLatin}]+"))
						continue;
					if (!tmp.contains("w"))
						if(!tmp.contains("u"))
							if(!tmp.contains("y"))
					if (!exist.counterMap.countAll().keySet().contains(tmp)) {
						System.err.println(jj[i] + jj[i + 1] + jj[i + 2]);
						tt++;
					}
				}
				//System.out.println(line);
			}
			counterMap.output("tmp/tmp");
			System.err.println(tt);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void main(String... args) {
		Ansj.segFile(new ToAnalysis(), config.getInputFile(config.totalData), "tmp/ansj.txt");
		posPattern p1 = new posPattern();
		p1.countPosPattern(config.renmingribao);
		posPattern p2 = new posPattern();
		p2.countPosPattern(config.totalData);
		posPattern pp = new posPattern();
		//pp.countPosPattern("tmp/ansj.txt", p2);
		pp.countPosPattern(config.totalData, p1);
	}
}
