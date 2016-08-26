package event;

import context.HandlerContext;

/**
 * 数据发送事件
 * @author skywalker
 *
 */
public class ChannelWriteEvent extends Event {

	public ChannelWriteEvent(HandlerContext context, Object message) {
		super(context, message);
	}

	@Override
	protected void doRun(HandlerContext context, Object message) {
		context.fireChannelWrite(message);
	}
	
}
