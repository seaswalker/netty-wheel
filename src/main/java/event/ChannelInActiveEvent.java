package event;

import context.HandlerContext;

/**
 * 连接断开事件
 * @author skywalker
 *
 */
public class ChannelInActiveEvent extends Event {

	public ChannelInActiveEvent(HandlerContext ctx) {
		super(ctx);
	}

	@Override
	protected void doRun(HandlerContext context, Object message) {
		context.fireChannelInActive();
	}
	
}
