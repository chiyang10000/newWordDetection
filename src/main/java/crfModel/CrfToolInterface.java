package crfModel;

/**
 * Created by wan on 5/16/2017.
 */
abstract public class CrfToolInterface {
	abstract void decode(String modelFile, String bemsInputFile, String bemsOutputFile);
	abstract void train(String template, String modelFile, String trainData);
	CRFModel crfModelWrapper;
	CrfToolInterface(CRFModel tmp) {
		crfModelWrapper = tmp;
	}
}
