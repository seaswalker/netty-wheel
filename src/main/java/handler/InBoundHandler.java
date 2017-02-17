package handler;

import context.HandlerContext;

/**
 * 数据读取事件处理器.
 *
 * @author skywalker
 *
 */
public interface InBoundHandler extends Handler {

    /**
     * 连接建立.
     *
     * @param context {@link HandlerContext} 处理器上下文
     */
    void channelActive(HandlerContext context);

    /**
     * 连接断开.
     *
     * @param context {@link HandlerContext} 处理器上下文
     */
    void channelInActive(HandlerContext context);

    /**
     * 数据读取.
     *
     * @param context {@link HandlerContext} 处理器上下文
     */
    void channelRead(Object message, HandlerContext context);

}
