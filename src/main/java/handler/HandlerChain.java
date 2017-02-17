package handler;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link Handler}调用链.
 * 
 * @author skywalker
 *
 */
public class HandlerChain {

    private final List<InBoundHandler> inBoundHandlers;
    private final List<OutBoundHandler> outBoundHandlers;

    public HandlerChain() {
        this.inBoundHandlers = new ArrayList<>();
        this.outBoundHandlers = new ArrayList<>();
        this.outBoundHandlers.add(new DefaultOutBoundHandler());
    }

    /**
     * 添加处理器
     */
    public void addHandler(Handler handler) {
        if (handler instanceof InBoundHandler) {
            inBoundHandlers.add((InBoundHandler) handler);
        } else if (handler instanceof OutBoundHandler) {
            outBoundHandlers.add((OutBoundHandler) handler);
        }
    }

    public List<InBoundHandler> getInBoundHandlers() {
        return new ArrayList<InBoundHandler>(inBoundHandlers);
    }

    public List<OutBoundHandler> getOutBoundHandlers() {
        return new ArrayList<OutBoundHandler>(outBoundHandlers);
    }

}
