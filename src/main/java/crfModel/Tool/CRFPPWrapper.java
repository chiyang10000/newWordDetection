package crfModel.Tool;

import crfModel.CRFModel;
import evaluate.RunSystemCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Created by wan on 4/24/2017.
 */
public class CRFPPWrapper extends CrfToolInterface {
	static String crf_test = new File("lib/crfpp/crf_test").getAbsolutePath();
	static String crf_learn = new File("lib/crfpp/crf_learn").getAbsolutePath();
	private static Logger logger = LoggerFactory.getLogger(CRFPPWrapper.class);

	public CRFPPWrapper(CRFModel tmp) {
		super(tmp);
	}
	static {
		if (System.getProperty("os.name").contains("Win")) {
			crf_test += ".exe";
			crf_learn += ".exe";
		}
		//windows 和 unix这里有区别
		if (!new File(crf_test).exists())
			logger.error("{} not exits!", crf_test);
	}


	public void decode(String modelFile, String bemsInputFile, String bemsOutputFile) {
		String cmd = String.join(" ", crf_test, "-m", modelFile, bemsInputFile, "-o", bemsOutputFile);
		RunSystemCommand.run(cmd);
	}

	@Override
	public void train(String template, String modelFile, String trainData) {
		String cmd = String.join(" ", crf_learn, template, trainData, modelFile);
		RunSystemCommand.run(cmd);
	}

}
