package handler;

import context.HandlerContext;

/**
 * 除了向下转发什么也不做，自己实现的OutBoundHandlerAdapter可继承此类。
 * 
 * @author skywalker
 *
 */
public class OutBoundHandlerAdapter implements OutBoundHandler {

    @Override
    public void channelWrite(Object message, HandlerContext context) {
        context.fireChannelWrite(message);
    }

}
