package dataProcess;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CounterMap implements Serializable {
	static private final Logger logger = LoggerFactory.getLogger(CounterMap.class);
	private static final long serialVersionUID = -3903452740943758085L;

	private Map<String, MutableInteger> count = new ConcurrentHashMap<>();

	public void output(String outputFile){
		logger.debug("outputing CouterMap to {}", outputFile);
		try {
			Map<String, MutableInteger> map = SortByValue.sortByValue(count);
			BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
			for (Map.Entry<String, MutableInteger> entry: map.entrySet()){
				writer.append(entry.getKey() + "\t" +entry.getValue().get());
				writer.newLine();
			}
			writer.close();
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	public CounterMap() {
	}

	public CounterMap(int capacitySize) {
		count = new ConcurrentHashMap<>(capacitySize);
	}

	public void incr(String key) {
		MutableInteger initValue = new MutableInteger(1);
		// 利用 HashMap 的put方法弹出旧值的特性
		MutableInteger oldValue = count.put(key, initValue);
		if (oldValue != null) {
			initValue.set(oldValue.get() + 1);
		}
	}

	public void incrby(String key, int delta) {

		MutableInteger initValue = new MutableInteger(delta);
		// 利用 HashMap 的put方法弹出旧值的特性
		MutableInteger oldValue = count.put(key, initValue);
		if (oldValue != null) {
			initValue.set(oldValue.get() + delta);
		}
	}

	public int get(String key) {
		MutableInteger value = count.get(key);
		if (null == value)
			return 0;
		return value.get();
	}

	public Map<String, MutableInteger> countAll() {
		return count;
	}

	private static final class MutableInteger implements Comparable{
		private int val;

		public MutableInteger(int val) {
			this.val = val;
		}

		public int get() {
			return this.val;
		}

		public void set(int val) {
			this.val = val;
		}
		// 为了方便打印
		public String toString() {
			return Integer.toString(val);
		}

		@Override
		public int compareTo(Object o) {
			return ((MutableInteger)o).get() - this.get();
		}
	}
}
