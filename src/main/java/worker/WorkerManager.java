package worker;

import java.util.concurrent.ExecutorService;

import manager.AbstractManager;

/**
 * 管理工作线程
 * 
 * @author skywalker
 *
 */
public class WorkerManager extends AbstractManager<Worker> {

	public WorkerManager(int s, ExecutorService executor) {
		super(s, executor);
	}

	@Override
	protected Worker newCandidate() {
		return new Worker(executor);
	}

}
