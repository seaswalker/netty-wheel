package manager;

/**
 * 线程选取策略
 * @author skywalker
 *
 */
public interface ChooseStrategy<T> {

	public T choose();
	
}
