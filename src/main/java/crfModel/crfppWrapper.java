package crfModel;

import evaluate.NewWordDetector;
import evaluate.RunSystemCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Set;

/**
 * Created by wan on 4/24/2017.
 */
abstract public class crfppWrapper implements NewWordDetector {
	static protected final char label_begin = 'B', label_meddle = 'M', label_end = 'E', label_single = 'S',
			label_true = 'T', label_false = 'F', label_inside = 'I', label_other = 'O';
	static String crf_test = new File("lib/crfpp/crf_test").getAbsolutePath();
	static String crf_learn = new File("lib/crfpp/crf_learn").getAbsolutePath();
	private static Logger logger = LoggerFactory.getLogger(crfppWrapper.class);

	static {
		if (System.getProperty("os.name").contains("Win")) {
			crf_test += ".exe";
			crf_learn += ".exe";
		}
		//windows 和 unix这里有区别
		if (!new File(crf_test).exists())
			logger.error("{} not exits!", crf_test);
	}

	String model, template, trainData;

	{
		model = "data/model/" + this.getClass().getSimpleName() + ".model";
		template = "data/crf-template/" + this.getClass().getSimpleName() + ".template";
		trainData = "tmp/crf/" + this.getClass().getSimpleName() + ".crf";
	}

	public static void decode(String modelFile, String bemsInputFile, String bemsOutputFile) {
		String cmd = String.join(" ", crf_test, "-m", modelFile, bemsInputFile, "-o", bemsOutputFile);
		RunSystemCommand.run(cmd);
	}

	public void train(String[] inputFiles, String pattern) {
		model = "data/model/" + this.getClass().getSimpleName() + "." + pattern + ".model";
		template = "data/crf-template/" + this.getClass().getSimpleName() + "." + pattern + ".template";
		trainData = "tmp/crf/" + this.getClass().getSimpleName() + "." + pattern + ".crf";
		convert2TrainInput(inputFiles, pattern);
		String cmd = String.join(" ", crf_learn, template, trainData, model);
		RunSystemCommand.run(cmd);
	}

	public Set<String> detectNewWord(String inputFile, String outputFile, String pattern) {
		String crfppInput = String.join("", "tmp/crf/", inputFile.replaceAll(".*/", ""),
				".", this.getClass().getSimpleName(), ".", pattern, ".crfin");
		String crfppOutput = String.join("", "tmp/crf/", inputFile.replaceAll(".*/", ""),
				".", this.getClass().getSimpleName(), ".", pattern, ".crfout");
		convertSrc2TestInput(new String[]{inputFile}, crfppInput, pattern);
		decode("data/model/" + this.getClass().getSimpleName() + "." + pattern + ".model", crfppInput, crfppOutput);
		return convertTestOuput2Res(crfppOutput, outputFile, pattern);
	}

	abstract void convert2TrainInput(String[] inputFiles, String pattern);

	abstract void convertSrc2TestInput(String[] inputFiles, String crfppInput, String pattern);

	abstract Set<String> convertTestOuput2Res(String crfppOutput, String resFile, String pattern);

}
