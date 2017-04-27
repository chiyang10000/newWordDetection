package crfModel;

import NagaoAlgorithm.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;

/**
 * Created by don on 27/04/2017.
 */
public class SegementCRF extends crfppWrapper {
	class Tag {
		String word;
		int length;
		String pos;
		int leftEntropy;
		int rightEntropy;
		int tf;
		int mi;

		Tag(String word, String pos) {
			length = word.length();
			this.pos = pos;
			TFNeighbor tfNeighbor = nagao.wordTFNeighbor.get(word);
			leftEntropy = tfNeighbor.getLeftNeighborEntropy();
		}

		@Override
		public String toString() {
			return String.join("\t",
					Integer.toString(length),
					pos,
					Integer.toString(leftEntropy),
					Integer.toString(rightEntropy),
					Integer.toString(tf),
					Integer.toString(mi));
		}
	}

	private static final Logger logger = LoggerFactory.getLogger(SingleCharacterCRF.class);
	static String trainData = "tmp/crfModel.crf";
	static String template = "data/crf-template/SingleCharacterCRF.template";
	NagaoAlgorithm nagao;
	HashSet<String> newWordList;

	public SegementCRF(NagaoAlgorithm nagao) {
		this.nagao = nagao;
		model = "data/model/SingleCharacterCRF.model";
	}

	@Override
	public void convert2TrainInput(String[] inputFiles, String trainFile) {

	}

	@Override
	public void convertSrc2TestInput(String[] inputFiles, String crfppInput) {

	}

	@Override
	public void convertTestOuput2Res(String crfppOutput, String resFile) {

	}

	public static void main(String... args) {
		String[] inputFiles = {"data/raw/train.txt"};
		NagaoAlgorithm nagao= new NagaoAlgorithm(15);
		nagao.scan(inputFiles);
		new SegementCRF(nagao).convert2TrainInput(inputFiles, trainData);
		train(template, trainData, new SingleCharacterCRF().model);
	}
}
