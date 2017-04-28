package crfModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by wan on 4/24/2017.
 */
abstract public class crfppWrapper {
	private static Logger logger = LoggerFactory.getLogger(crfppWrapper.class);
	static String crf_test = new File("lib/crfpp/crf_test").getAbsolutePath();
	static String crf_learn = new File("lib/crfpp/crf_learn").getAbsolutePath();
	static String shell = "";
	String model, template, trainData;

	static {
		if (System.getProperty("os.name").contains("Win")) {
			shell = "cmd /c";
			crf_test += ".exe";
			crf_learn += ".exe";
		}
		//windows 和 unix这里有区别
		if (!new File(crf_test).exists())
			logger.error("{} not exits!", crf_test);
	}

	private static void runCommand(String cmd) {
		try {
			logger.debug("Run command: [{}]", cmd);
			Process pro = Runtime.getRuntime().exec(cmd);
			InputStream in = pro.getInputStream();
			BufferedReader read = new BufferedReader(new InputStreamReader(in));
			String line = null;
			while ((line = read.readLine()) != null) {
				System.err.println(line);
			}
			in.close();
			pro.waitFor();
			in = pro.getErrorStream();
			read = new BufferedReader(new InputStreamReader(in));
			while ((line = read.readLine()) != null) {
				System.err.println(line);
			}
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void decode(String modelFile, String bemsInputFile, String bemsOutputFile) {
		String cmd = String.join(" ", shell, crf_test, "-m", modelFile, bemsInputFile, "-o", bemsOutputFile);
		runCommand(cmd);
	}

	public void train() {
		String cmd = String.join(" ", shell, crf_learn, template, trainData, model, "-t");
		runCommand(cmd);
	}

	public void detect(String inputFile, String outputFile) {
		String crfppInput = inputFile + ".crfin";
		String crfppOutput = inputFile + ".crfout";
		convertSrc2TestInput(new String[]{inputFile}, crfppInput);
		decode(model, crfppInput, crfppOutput);
		convertTestOuput2Res(crfppOutput, outputFile);
	}

	abstract public void convert2TrainInput(String[] inputFiles);
	abstract public void convertSrc2TestInput(String[] inputFiles, String crfppInput);
	abstract public void convertTestOuput2Res(String crfppOutput, String resFile);

}
