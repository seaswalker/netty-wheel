package manager;

import java.util.List;

/**
 * 线程选取策略
 * @author skywalker
 *
 */
public interface ChooseStrategy<T> {

	public T choose(Object param);
	
	public void setCandidates(List<T> candidates);
	
}
