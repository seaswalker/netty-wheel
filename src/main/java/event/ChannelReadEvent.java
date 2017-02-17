package event;

import context.HandlerContext;

/**
 * {@link Event}实现，数据读取事件.
 *
 * @author skywalker
 */
public class ChannelReadEvent extends Event {

    public ChannelReadEvent(HandlerContext context, Object message) {
        super(context, message);
    }

    @Override
    protected void doRun(HandlerContext context, Object message) {
        context.fireChannelRead(message);
    }

}
