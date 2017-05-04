package NagaoAlgorithm;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Created by wan on 4/27/2017.
 */
public class DiscreteTFNeighbor implements Serializable {
	double mi[], tf[], le[], re[];

	public DiscreteTFNeighbor(int levelNum, double[] tmp_mi, double[] tmp_tf, double[] tmp_le, double[] tmp_re) {
		Arrays.sort(tmp_mi);
		Arrays.sort(tmp_tf);
		Arrays.sort(tmp_le);
		Arrays.sort(tmp_re);
		mi = new double[levelNum + 1];
		tf = new double[levelNum + 1];
		le = new double[levelNum + 1];
		re = new double[levelNum + 1];
		for (int i = 0; i < levelNum; i++) {
			mi[i] = tmp_mi[i * tmp_mi.length / levelNum];
			tf[i] = tmp_mi[i * tmp_tf.length / levelNum];
			le[i] = tmp_mi[i * tmp_le.length / levelNum];
			re[i] = tmp_mi[i * tmp_re.length / levelNum];
		}
		mi[levelNum] = Double.MAX_VALUE;
		tf[levelNum] = Double.MAX_VALUE;
		le[levelNum] = Double.MAX_VALUE;
		re[levelNum] = Double.MAX_VALUE;

	}

	public int getMI(double value) {
		int i = 0;
		while (mi[++i] < value) ;
		return i - 1;
	}

	public int getTF(double value) {
		int i = 0;
		while (tf[++i] < value) ;
		return i - 1;
	}

	//左右熵为0的算作一类
	public int getLE(double value) {
		if (Math.abs(value) == 0.0)
			return -1;
		int i = 0;
		while (le[++i] < value) ;
		return i - 1;
	}

	public int getRE(double value) {
		if (Math.abs(value) == 0.0)
			return -1;
		int i = 0;
		while (re[++i] < value) ;
		return i - 1;
	}
}
