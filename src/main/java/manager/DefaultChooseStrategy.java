package manager;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 默认线程选取策略--简单的递增
 * @author skywalker
 *
 * @param <T> 
 */
public class DefaultChooseStrategy<T> implements ChooseStrategy<T> {
	
	private final T[] candidates;
	private int index = 0;
	private final int length;
	private final Lock lock = new ReentrantLock();

	public DefaultChooseStrategy(T[] candidates) {
		this.candidates = candidates;
		length = candidates.length;
	}

	@Override
	public T choose() {
		lock.lock();
		T result;
		try {
			result = candidates[index];
			++index;
			if (index >= length) index = 0;
		} finally {
			lock.unlock();
		}
		return result;
	}

}
