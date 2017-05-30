package crfModel.Tool;

import crfModel.CRFModel;
import evaluate.RunSystemCommand;
import evaluate.config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * Created by wan on 4/24/2017.
 */
public class CRFsuiteWrapper extends CrfToolInterface {
	static String crfsuite = new File("lib/crfsuite/bin/crfsuite").getAbsolutePath();
	static String templateConverter = new File("lib/crfsuite/template.py").getAbsolutePath();
	private static Logger logger = LoggerFactory.getLogger(CRFsuiteWrapper.class);

	static {
		if (!System.getProperty("os.name").contains("Linux")) {
		}
		//windows 和 unix这里有区别
		if (!new File(crfsuite).exists())
			logger.error("{} not exits!", crfsuite);
	}

	String algorithm = "-a " + config.algorithmInCRFSuite;

	public CRFsuiteWrapper(CRFModel tmp) {
		super(tmp);
	}

	public void decode(String modelFile, String bemsInputFile, String bemsOutputFile) {
		RunSystemCommand.run(String.join(" ", "python", templateConverter, crfModelWrapper.template,
				"<", bemsInputFile, ">", bemsInputFile + ".crfsuite"));
		String cmd = String.join(" ", crfsuite, "tag", "-m", modelFile,
				bemsInputFile + ".crfsuite", ">", "tmp/tmp.crfsuite");
		RunSystemCommand.run(cmd);
		try {
			try (BufferedReader input = new BufferedReader(new FileReader(bemsInputFile));
				 BufferedReader label = new BufferedReader(new FileReader("tmp/tmp.crfsuite"));
				 BufferedWriter output = new BufferedWriter(new FileWriter(bemsOutputFile))) {
				String line, tag;
				while ((tag = label.readLine()) != null) {
					line = input.readLine();
					while (tag.length() > 0 && line.length() == 0) {
						line = input.readLine();
					}
					if (tag.length() > 0)
						output.append(line + "\t" + tag);
					output.newLine();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void train(String template, String modelFile, String trainData) {

		RunSystemCommand.run(String.join(" ", "python", templateConverter, template, "<", trainData, ">", trainData +
				"" +
				".crfsuite"));
		String cmd = String.join(" ", crfsuite, "learn", algorithm, "-m", modelFile, trainData + ".crfsuite");
		RunSystemCommand.run(cmd);
	}

}
