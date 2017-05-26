package crfModel;

import Feature.FieldAppender;
import crfModel.Tool.CRFPPWrapper;
import crfModel.Tool.CRFsuiteWrapper;
import crfModel.Tool.CrfToolInterface;
import dataProcess.Corpus;
import evaluate.Ner;
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
	static protected final String label_begin = "B", label_meddle = "M", label_end = "E", label_single = "S",
			label_true = "T", label_false = "F", label_inside = "I", label_other = "O";
	private static Logger logger = LoggerFactory.getLogger(CRFModel.class);
	private CrfToolInterface crfToolWrapper;

	public String model, template, trainData;

	{
		/*
		if (System.getProperty("os.name").contains("Win")) {
			config.isCRFsuite = true;
			config.algorithm = "ap" + config.algorithm;
		}
		*/
		if (config.isCRFsuite) {
			crfToolWrapper = new CRFsuiteWrapper(this);
		} else
			crfToolWrapper = new CRFPPWrapper(this);
	}

	public static String getWord(String in) {
		return in.split("\t", 2)[0];
	}

	public static String getLabel(String in) {
		String[] tmp = in.split("\t");
		return tmp[tmp.length - 1];
	}

	public static Map<String, String> convertTestOuput2Res(String inputFile, String newWordFile, Ner ner) {
		logger.debug("converting label to ans file {}", newWordFile);
		HashMap<String, String> newWordList = new HashMap<>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(inputFile));
			PrintWriter writer = new PrintWriter(new FileWriter(newWordFile));
			String line = reader.readLine();
			StringBuilder context = new StringBuilder("^");

			while (true) {
				if (line == null) break;
				if (line.length() == 0) {
					line = reader.readLine();
					if (line == null) break;
						context = new StringBuilder("^" + getWord(line));
					continue;
				}
				if (context.length() > 12)
					context.delete(0, context.length() -12);
				StringBuilder wordBuffer = new StringBuilder();
				FieldAppender fieldAppender = null;
				String label_head = getLabel(line);
				String label = label_head;
				if (label.contains(label_begin)) {
					while (!label.contains(label_other) && !label.contains(label_single)) {//todo 瞎搞了
						if (fieldAppender == null)
							fieldAppender = new FieldAppender(line);
						else
							fieldAppender.append(line);
						wordBuffer.append(getWord(line));
						line = reader.readLine();
						context.append(getWord(line));
						if (line.length() <= 0) break;
						label = getLabel(line);
						if (label.contains(label_begin)) break;
					}
				}
				else {
					fieldAppender = new FieldAppender(line);
					wordBuffer.append(getWord(line));
					line = reader.readLine();
					context.append(getWord(line));
				}
				String word = wordBuffer.toString();// 这是一个词
				//if (word.length() ==0) return null;
				if (ner == Ner.nw) {
					if (Corpus.isNewWord(word, null) && !newWordList.keySet().contains(word)) {
						newWordList.put(word, context + "\t" + fieldAppender.toString());
						writer.println(word + "\t" + context + "\t" + fieldAppender);
					}
				} // nw
				if (ner != Ner.nw) {
					if (label_head.equals(ner.label + label_begin) || label_head.equals(ner.label + label_single)) //单字名称 和 多字名称
						if (!newWordList.keySet().contains(word)) {
							newWordList.put(word, context + "\t" + fieldAppender.toString());
							writer.println(word + "\t" + context + "\t" + fieldAppender);
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

	public void train(String[] inputFiles, Ner ner) {
		model = "data/model/"  + crfToolWrapper.getClass().getSimpleName() + "." +  this.getClass().getSimpleName() + "." + ner.model+ ".model";
		template = "data/crf-template/" + this.getClass().getSimpleName() + "." + ner.template+ ".template";
		trainData = "tmp/crf/" + this.getClass().getSimpleName() + "." + ner.model+ ".crf";
		convert2TrainInput(inputFiles, ner);
		crfToolWrapper.train(template, model, trainData);
	}

	public Map<String, String> detectNewWord(String inputFile, String outputFile, Ner ner) {
		model = "data/model/"  + crfToolWrapper.getClass().getSimpleName() + "." +  this.getClass().getSimpleName() + "." + ner.model+ ".model";
		template = "data/crf-template/" + this.getClass().getSimpleName() + "." + ner.template+ ".template"; //crfsuite 要用到
		String prefix = String.join(".",
				"tmp/crf/" + inputFile.replaceAll(".*/", ""),
				this.getClass()
				.getSimpleName(), ner.name
		);
		String crfppInput = String.join(".", prefix, "crfin");
		String crfppOutput = String.join(".", prefix, "crfout");
		convertSrc2TestInput(inputFile, crfppInput, ner);
		crfToolWrapper.decode(model, crfppInput, crfppOutput);
		return convertTestOuput2Res(crfppOutput, outputFile, ner);
	}

	abstract void convert2TrainInput(String[] inputFiles, Ner ner);

	abstract void convertSrc2TestInput(String inputFile, String crfppInput, Ner ner);


}
