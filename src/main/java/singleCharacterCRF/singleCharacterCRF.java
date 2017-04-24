package singleCharacterCRF;

import evaluate.Corpus;

import java.io.*;

/**
 * Created by wan on 4/24/2017.
 */
public class singleCharacterCRF extends Corpus {
	public static void detect(String inputFile, String outputFile) {
		String bemsInputFile = "tmp/singleCharacterCRF.bems.in.txt";
		String bemsOutputFile = "tmp/singleCharacterCRF.bems.out.txt";
		//windows 和 linux这里有区别
		String model = new File("data/model").getAbsolutePath() + "\\model0";
		String cmd = "lib\\crf_test -m " + model + " " + bemsInputFile + " > " + bemsOutputFile;
		cmd = "cmd /c " + cmd;
		try {
			convertSrcToBEMS(bemsInputFile, inputFile);
			try {
				Process pro = Runtime.getRuntime().exec(cmd);
				InputStream in = pro.getErrorStream();
				BufferedReader read = new BufferedReader(new InputStreamReader(in));
				pro.waitFor();
				if (pro.exitValue() == 1)//p.exitValue()==0表示正常结束，1：非正常结束
					System.err.println("命令执行失败!");
				else System.out.println("yes");
				String line = null;
				while ((line = read.readLine()) != null) {
					System.out.println(line);
				}
				in.close();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			convertBEMSToSeg(bemsOutputFile, "tmp/tmp", outputFile);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
