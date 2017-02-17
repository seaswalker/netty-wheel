package handler;

import context.HandlerContext;

/**
 * 数据输出事件处理器.
 *
 * @author skywalker
 */
public interface OutBoundHandler extends Handler {

    /**
     * 向客户端返回数据.
     *
     * @param message 数据(消息)
     * @param context {@link HandlerContext} 处理器上下文
     */
    void channelWrite(Object message, HandlerContext context);

}
