package NagaoAlgorithm;

public class Main {

	public static void main(String[] args) {

		//if 3 arguments, first argument is input files splitting with ','
		//second argument is output file
		//output 7 columns split with ',' , like below:
		//word, term frequency, left neighbor number, right neighbor number, left neighbor entropy, right neighbor entropy, mutual information
		//third argument is stop words list
		if(args.length == 3)
			NagaoAlgorithm.applyNagao(args[0].split(","), args[1], args[2]);

			//if 4 arguments, forth argument is the NGram parameter N
			//5th argument is threshold of output words, default is "20,3,3,5"
			//output TF > 20 && (left | right) neighbor number > 3 && MI > 5
		else if(args.length == 5)
			NagaoAlgorithm.applyNagao(args[0].split(","), args[1], args[2], Integer.parseInt(args[3]), args[4]);

	}

}