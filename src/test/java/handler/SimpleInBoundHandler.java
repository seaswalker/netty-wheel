package handler;

import context.HandlerContext;

/**
 * 简单的{@link InBoundHandlerAdapter}实现，简单地打印出事件触发以及收到的消息.
 *
 * @author skywalker
 */
public class SimpleInBoundHandler extends InBoundHandlerAdapter {

    @Override
    public void channelActive(HandlerContext context) {
        System.out.println("channel active");
    }

    @Override
    public void channelInActive(HandlerContext context) {
        System.out.println("channel inActive");
    }

    @Override
    public void channelRead(Object message, HandlerContext context) {
        // 去掉前面的4字节
        context.writeFlush(((String) message).substring(4) + "\n");
    }

}
