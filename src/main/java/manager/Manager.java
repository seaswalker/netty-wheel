package manager;

/**
 * 对一组线程进行管理
 * @author skywalker
 *
 */
public interface Manager<T> extends LifeCycle {

	public void setChooseStrategy(ChooseStrategy<T> chooseStrategy);
	
	/**
	 * 从管理的线程中选取一个
	 * @return
	 */
	public T chooseOne();
	
}
