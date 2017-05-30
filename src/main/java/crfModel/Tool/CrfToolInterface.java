package crfModel.Tool;

import crfModel.CRFModel;

/**
 * Created by wan on 5/16/2017.
 */
abstract public class CrfToolInterface {
	CRFModel crfModelWrapper;

	CrfToolInterface(CRFModel tmp) {
		crfModelWrapper = tmp;
	}

	abstract public void decode(String modelFile, String bemsInputFile, String bemsOutputFile);

	abstract public void train(String template, String modelFile, String trainData);
}
