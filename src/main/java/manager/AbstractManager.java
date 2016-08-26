package manager;

import java.util.concurrent.ExecutorService;

/**
 * Manager骨架实现
 * 
 * @author skywalker
 *
 */
public abstract class AbstractManager<T> implements Manager<T> {

	protected final T[] candidates;
	protected final ExecutorService executor;
	private final ChooseStrategy<T> defaultStrategy;
	private ChooseStrategy<T> chooseStrategy;
	
	@SuppressWarnings("unchecked")
	public AbstractManager(int s, ExecutorService executor) {
		if (s < 1) {
			throw new IllegalArgumentException("The Selectors count cant't be less than 1.");
		}
		candidates = (T[]) new Object[s];
		this.executor = executor;
		defaultStrategy = new DefaultChooseStrategy<T>(candidates);
	}
	
	@Override
	public void start() {
		for (int i = 0, l = candidates.length; i < l; i++) {
			T candidate = newCandidate();
			candidates[i] = candidate;
			if (candidate instanceof LifeCycle) {
				LifeCycle lifeCycle = (LifeCycle) candidate;
				lifeCycle.start();
			}
		}
	}
	
	/**
	 * 生成一个候选人
	 * @return
	 */
	protected abstract T newCandidate();
	
	@Override
	public void setChooseStrategy(ChooseStrategy<T> chooseStrategy) {
		if (chooseStrategy != null)
			this.chooseStrategy = chooseStrategy;
	}
	
	private ChooseStrategy<T> getChooseStrategy() {
		if (chooseStrategy == null)
			chooseStrategy = defaultStrategy;
		return chooseStrategy;
	}
	
	@Override
	public T chooseOne() {
		return getChooseStrategy().choose();
	}
	
}
