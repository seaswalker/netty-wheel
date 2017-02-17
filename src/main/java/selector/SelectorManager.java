package selector;

import handler.HandlerChain;

import java.util.concurrent.ExecutorService;

import worker.WorkerManager;
import manager.AbstractManager;
import manager.ChooseStrategy;

/**
 * Selector管理器，负责对Selector的启动、负载均衡处理.
 *
 * @author skywalker
 */
public class SelectorManager extends AbstractManager<QueuedSelector> {

    private HandlerChain handlerChain;
    private WorkerManager workerManager;

    public SelectorManager(int s, ExecutorService executor) {
        super(s, executor);
    }

    public SelectorManager(int s, ExecutorService executor,
                           ChooseStrategy<QueuedSelector> chooseStrategy) {
        super(s, executor, chooseStrategy);
    }

    public void setHandlerChain(HandlerChain handlerChain) {
        this.handlerChain = handlerChain;
    }

    public HandlerChain getHandlerChain() {
        return handlerChain;
    }

    public void setWorkerManager(WorkerManager workerManager) {
        this.workerManager = workerManager;
    }

    @Override
    protected QueuedSelector newCandidate() {
        QueuedSelector selector = new QueuedSelector(executor);
        selector.setSelectorManager(this);
        selector.setWorkerManager(workerManager);
        return selector;
    }

}
