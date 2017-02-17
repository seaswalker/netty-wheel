package handler;

import java.util.Objects;

import context.HandlerContext;

/**
 * 用以在客户端Channel建立时想HandlerChain添加Handler.
 * <p>这样可以实现对于每一次客户端调用，HandlerChain中的Handler对象都是不同的, 否则都是同一个对象。</p>
 * 
 * @author skywalker
 *
 */
public abstract class HandlerInitializer extends InBoundHandlerAdapter {

    @Override
    public void channelActive(HandlerContext context) {
        Handler[] handlers = init();
        Objects.requireNonNull(handlers);
        context.removeHandlerInitializer(this);
        for (int i = 0, l = handlers.length; i < l; i++) {
            context.addHandler(handlers[i]);
        }
        context.fireChannelActive();
    }

    /**
     * 返回想要添加的Handler数组.
     *
     * @return {@link Handler} array
     */
    public abstract Handler[] init();

}
