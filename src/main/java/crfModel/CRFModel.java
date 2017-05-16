package crfModel;

import evaluate.NewWordDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Created by wan on 4/24/2017.
 */
abstract public class CRFModel implements NewWordDetector {
	static protected final char label_begin = 'B', label_meddle = 'M', label_end = 'E', label_single = 'S',
			label_true = 'T', label_false = 'F', label_inside = 'I', label_other = 'O';
	private static Logger logger = LoggerFactory.getLogger(CRFModel.class);
	private CrfToolInterface crfToolWrapper = new CRFsuiteWrapper(this);

	String model, template, trainData;

	{
		model = "data/model/" + this.getClass().getSimpleName() + ".model";
		template = "data/crf-template/" + this.getClass().getSimpleName() + ".template";
		trainData = "CRFPPWrapper/crf/" + this.getClass().getSimpleName() + ".crf";
	}

	public static String getWord(String in) {
		return in.split("\t", 2)[0];
	}

	public static char getLabel(String in) {
		return in.charAt(in.length() - 1);
	}

	public void train(String[] inputFiles, String pattern) {
		model = "data/model/" + this.getClass().getSimpleName() + "." + pattern + ".model";
		template = "data/crf-template/" + this.getClass().getSimpleName() + "." + pattern + ".template";
		trainData = "CRFPPWrapper/crf/" + this.getClass().getSimpleName() + "." + pattern + ".crf";
		convert2TrainInput(inputFiles, pattern);
		crfToolWrapper.train(template, model, trainData);
	}

	public Set<String> detectNewWord(String inputFile, String outputFile, String pattern) {
		String crfppInput = String.join("", "CRFPPWrapper/crf/", inputFile.replaceAll(".*/", ""),
				".", this.getClass().getSimpleName(), ".", pattern, ".crfin");
		String crfppOutput = String.join("", "CRFPPWrapper/crf/", inputFile.replaceAll(".*/", ""),
				".", this.getClass().getSimpleName(), ".", pattern, ".crfout");
		convertSrc2TestInput(new String[]{inputFile}, crfppInput, pattern);
		crfToolWrapper.decode("data/model/" + this.getClass().getSimpleName() + "." + pattern + ".model", crfppInput,
				crfppOutput);
		return convertTestOuput2Res(crfppOutput, outputFile, pattern);
	}

	abstract void convert2TrainInput(String[] inputFiles, String pattern);

	abstract void convertSrc2TestInput(String[] inputFiles, String crfppInput, String pattern);

	abstract Set<String> convertTestOuput2Res(String crfppOutput, String resFile, String pattern);

}
