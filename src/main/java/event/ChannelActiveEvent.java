package event;

import context.HandlerContext;

public class ChannelActiveEvent extends Event {

	public ChannelActiveEvent(HandlerContext ctx) {
		super(ctx);
	}

	@Override
	protected void doRun(HandlerContext context, Object message) {
		context.fireChannelActive();
	}

}
