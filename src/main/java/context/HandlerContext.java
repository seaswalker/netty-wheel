package context;

import java.nio.channels.SocketChannel;
import java.util.List;

import event.ChannelWriteEvent;
import handler.Handler;
import handler.HandlerInitializer;
import handler.InBoundHandler;
import handler.OutBoundHandler;
import worker.WorkerManager;

/**
 * Handler执行上下文，负责handler之间的转发.
 *
 * @author skywalker
 */
public class HandlerContext {

    private final List<InBoundHandler> inBoundHandlers;
    private final List<OutBoundHandler> outBoundHandlers;
    private int index;
    private int inBoundSize, outBoundSize;
    private WorkerManager workerManager;
    private SocketChannel channel;
    private final boolean isInBound;

    public HandlerContext(List<InBoundHandler> inBoundHandlers,
                          List<OutBoundHandler> outBoundHandlers, boolean isInBound) {
        this.inBoundHandlers = inBoundHandlers;
        this.outBoundHandlers = outBoundHandlers;
        this.inBoundSize = inBoundHandlers.size();
        this.outBoundSize = outBoundHandlers.size();
        this.isInBound = isInBound;
        reset();
    }

    public void setWorkerManager(WorkerManager workerManager) {
        this.workerManager = workerManager;
    }

    /**
     * 直接使用Channel原生方法返回方法数据不会被{@link OutBoundHandler}处理.
     */
    public SocketChannel getChannel() {
        return channel;
    }

    public void setChannel(SocketChannel channel) {
        this.channel = channel;
    }

    /**
     * 添加Handler，用于HandlerInitializer，对HandlerContext的添加并不会影响HandlerChain.
     *
     * @param handler {@link Handler}
     * @return this {@link HandlerContext}
     */
    public HandlerContext addHandler(Handler handler) {
        if (handler instanceof InBoundHandler) {
            inBoundHandlers.add((InBoundHandler) handler);
            ++inBoundSize;
        } else if (handler instanceof OutBoundHandler) {
            outBoundHandlers.add((OutBoundHandler) handler);
            ++outBoundSize;
        }
        return this;
    }

    /**
     * 移除HandlerInitializer.
     *
     * @param handlerInitializer {@link HandlerInitializer}
     */
    public void removeHandlerInitializer(HandlerInitializer handlerInitializer) {
        inBoundHandlers.remove(handlerInitializer);
        --inBoundSize;
        --index;
    }

    /**
     * reset the index to original value.
     */
    public void reset() {
        this.index = isInBound ? 0 : (outBoundSize - 1);
    }

    /**
     * 触发连接建立事件.
     */
    public void fireChannelActive() {
        if (index >= inBoundSize)
            return;
        InBoundHandler handler = inBoundHandlers.get(index);
        ++index;
        handler.channelActive(this);
    }

    /**
     * 触发连接断开事件.
     */
    public void fireChannelInActive() {
        if (index >= inBoundSize)
            return;
        InBoundHandler handler = inBoundHandlers.get(index);
        ++index;
        handler.channelInActive(this);
    }

    /**
     * 触发消息读取事件.
     *
     * @param message {@link Object} 消息
     */
    public void fireChannelRead(Object message) {
        if (index >= inBoundSize)
            return;
        InBoundHandler handler = inBoundHandlers.get(index);
        ++index;
        handler.channelRead(message, this);
    }

    /**
     * 多次触发后续Handler channelRead事件.
     *
     * @param messages {@link List}
     */
    public void fireChannelReads(List<byte[]> messages) {
        if (messages != null) {
            int oldIndex = index;
            for (int i = 0, s = messages.size(); i < s; i++) {
                index = oldIndex;
                fireChannelRead(messages.get(i));
            }
        }
    }

    /**
     * 触发消息写出事件.
     *
     * @param message {@link Object} 消息
     */
    public void fireChannelWrite(Object message) {
        if (index < 0)
            return;
        OutBoundHandler handler = outBoundHandlers.get(index);
        --index;
        handler.channelWrite(message, this);
    }

    /**
     * 向客户端返回数据，触发OutBound事件.
     *
     * @param message {@link Object} 数据
     */
    public void writeFlush(Object message) {
        HandlerContext context = new HandlerContext(inBoundHandlers, outBoundHandlers, false);
        context.setChannel(channel);
        context.setWorkerManager(workerManager);
        workerManager.chooseOne(channel).submit(new ChannelWriteEvent(context, message));
    }

}
