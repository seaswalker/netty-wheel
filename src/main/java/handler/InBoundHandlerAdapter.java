package handler;

import context.HandlerContext;

/**
 * 除了向下转发什么也不做，自己实现的InBoundHandlerAdapter可继承此类.
 * 
 * @author skywalker
 *
 */
public class InBoundHandlerAdapter implements InBoundHandler {

    @Override
    public void channelActive(HandlerContext context) {
        context.fireChannelActive();
    }

    @Override
    public void channelInActive(HandlerContext context) {
        context.fireChannelInActive();
    }

    @Override
    public void channelRead(Object message, HandlerContext context) {
        context.fireChannelRead(message);
    }

}
