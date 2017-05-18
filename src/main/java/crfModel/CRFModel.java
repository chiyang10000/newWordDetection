package crfModel;

import Feature.FieldAppender;
import crfModel.Tool.CRFPPWrapper;
import crfModel.Tool.CRFsuiteWrapper;
import crfModel.Tool.CrfToolInterface;
import dataProcess.Corpus;
import evaluate.NewWordDetector;
import evaluate.config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by wan on 4/24/2017.
 */
abstract public class CRFModel implements NewWordDetector {
	static protected final char label_begin = 'B', label_meddle = 'M', label_end = 'E', label_single = 'S',
			label_true = 'T', label_false = 'F', label_inside = 'I', label_other = 'O';
	private static Logger logger = LoggerFactory.getLogger(CRFModel.class);
	private CrfToolInterface crfToolWrapper;

	public String model, template, trainData;

	{
		if (config.isCRFsuite) {
			crfToolWrapper = new CRFsuiteWrapper(this);
		}
		else
			crfToolWrapper = new CRFPPWrapper(this);
	}

	public static String getWord(String in) {
		return in.split("\t", 2)[0];
	}

	public static char getLabel(String in) {
		return in.charAt(in.length() - 1);
	}

	public static Map<String, String> convertTestOuput2Res(String inputFile, String newWordFile, String pattern) {
		HashMap<String, String> newWordList = new HashMap<>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(inputFile));
			PrintWriter writer = new PrintWriter(new FileWriter(newWordFile));
			String line;

			while ((line = reader.readLine()) != null) {
				if (line.length() == 0)
					continue;
				StringBuilder wordBuffer = new StringBuilder();
				FieldAppender fieldAppender = new FieldAppender(line);
				wordBuffer.append(line.split("\t", 2)[0]);
				char label_head = line.charAt(line.length() - 1);
				if (line.charAt(line.length() - 1) == label_begin) {
					do {
						line = reader.readLine();
						fieldAppender.append(line);
						wordBuffer.append(line.split("\t", 2)[0]);
					} while (line.length() > 0 && line.charAt(line.length() - 1) != label_end);
				}

				String word = wordBuffer.toString();// 这是一个词
				if (pattern == config.nw) {
					if (Corpus.isNewWord(word, null) && !newWordList.keySet().contains(word)) {
						newWordList.put(word, fieldAppender.toString());
						writer.println(word + "\t" + fieldAppender);
					}
				} // nw
				if (pattern == config.nr || pattern == config.ns) {
					if (label_head == label_begin || label_head == label_single) //单字名称 和 多字名称
						if (!newWordList.keySet().contains(word)) {
							newWordList.put(word, fieldAppender.toString());
							writer.println(word + "\t" + fieldAppender);
						}
				} // nr ,ns
			}

			writer.close();
		} catch (IOException e) {
			logger.error("err!");
			e.printStackTrace();
		}
		return newWordList;
	}

	public void train(String[] inputFiles, String pattern) {
		model = "data/model/" + this.getClass().getSimpleName() + "." + pattern + ".model";
		template = "data/crf-template/" + this.getClass().getSimpleName() + "." + pattern + ".template";
		trainData = "tmp/crf/" + this.getClass().getSimpleName() + "." + pattern + ".crf";
		convert2TrainInput(inputFiles, pattern);
		crfToolWrapper.train(template, model, trainData);
	}

	public Map<String, String> detectNewWord(String inputFile, String outputFile, String pattern) {
		model = "data/model/" + this.getClass().getSimpleName() + "." + pattern + ".model";
		template = "data/crf-template/" + this.getClass().getSimpleName() + "." + pattern + ".template";
		trainData = "tmp/crf/" + this.getClass().getSimpleName() + "." + pattern + ".crf";
		String crfppInput = String.join("", "tmp/crf/", inputFile.replaceAll(".*/", ""),
				".", this.getClass().getSimpleName(), ".", pattern, ".crfin");
		String crfppOutput = String.join("", "tmp/crf/", inputFile.replaceAll(".*/", ""),
				".", this.getClass().getSimpleName(), ".", pattern, ".crfout");
		convertSrc2TestInput(new String[]{inputFile}, crfppInput, pattern);
		crfToolWrapper.decode("data/model/" + this.getClass().getSimpleName() + "." + pattern + ".model", crfppInput,
				crfppOutput);
		return convertTestOuput2Res(crfppOutput, outputFile, pattern);
	}

	abstract void convert2TrainInput(String[] inputFiles, String pattern);

	abstract void convertSrc2TestInput(String[] inputFiles, String crfppInput, String pattern);


}
