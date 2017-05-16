package crfModel;

import evaluate.RunSystemCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Created by wan on 4/24/2017.
 */
public class CRFsuiteWrapper extends CrfToolInterface {
	static String crfsuite = new File("lib/crfsuite/bin/crfsuite").getAbsolutePath();
	static String templateConverter = new File("lib/crfsuite/template.py").getAbsolutePath();
	String algorithm = "ap";
	private static Logger logger = LoggerFactory.getLogger(CRFsuiteWrapper.class);

	CRFsuiteWrapper(CRFModel tmp) {
		super(tmp);
	}
	static {
		if (!System.getProperty("os.name").contains("Linux")) {
			System.err.println("crfsuite only avail in linux");
		}
		//windows 和 unix这里有区别
		if (!new File(crfsuite).exists())
			logger.error("{} not exits!", crfsuite);
	}


	public void decode(String modelFile, String bemsInputFile, String bemsOutputFile) {
		RunSystemCommand.run(String.join(" ", templateConverter, crfModelWrapper.template, bemsInputFile, ">", bemsInputFile + ".crfsuite"));
		String cmd = String.join(" ", crfsuite, "tag", "-m", modelFile, bemsInputFile + ".crfsuite", ">", bemsOutputFile);
		RunSystemCommand.run(cmd);
	}

	@Override
	void train(String template, String modelFile, String trainData) {

		RunSystemCommand.run(String.join(" ", templateConverter, template, trainData, ">", trainData + ".crfsuite"));
		String cmd = String.join(" ", crfsuite, "learn", trainData + ".crfsuite", "-m", modelFile);
		RunSystemCommand.run(cmd);
	}

}
