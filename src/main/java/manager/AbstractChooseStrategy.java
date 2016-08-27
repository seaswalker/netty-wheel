package manager;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 为ChooseStrategy提供骨架功能
 * 
 * @author skywalker
 *
 */
public abstract class AbstractChooseStrategy<T> implements ChooseStrategy<T> {

	protected List<T> candidates;
	protected int index = 0;
	protected int length;
	private final Lock lock = new ReentrantLock();

	@Override
	public final T choose(Object param) {
		lock.lock();
		T result;
		try {
			result = doChoose(param);
			++index;
			if (index >= length)
				index = 0;
		} finally {
			lock.unlock();
		}
		return result;
	}
	
	@Override
	public void setCandidates(List<T> candidates) {
		this.candidates = candidates;
		this.length = candidates.size();
	}

	/**
	 * 实际选择操作，子类实现
	 * 
	 * @param param
	 *            {@link Object} 参数
	 * @return T
	 */
	public abstract T doChoose(Object param);

}
