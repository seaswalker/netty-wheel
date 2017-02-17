package worker;

import java.util.concurrent.ExecutorService;

import manager.AbstractManager;
import manager.ChooseStrategy;

/**
 * 管理工作线程。
 * 
 * @author skywalker
 *
 */
public class WorkerManager extends AbstractManager<Worker> {

    public WorkerManager(int s, ExecutorService executor) {
        super(s, executor);
    }

    public WorkerManager(int s, ExecutorService executor,
            ChooseStrategy<Worker> chooseStrategy) {
        super(s, executor, chooseStrategy);
    }

    @Override
    protected Worker newCandidate() {
        return new Worker(executor);
    }

}
