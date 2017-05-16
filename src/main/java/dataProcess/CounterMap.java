package dataProcess;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CounterMap implements Serializable {

	private static final long serialVersionUID = -3903452740943758085L;

	private Map<String, MutableInteger> count = new ConcurrentHashMap<>();

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

	private static final class MutableInteger {
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
	}
}
