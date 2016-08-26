package event;

import context.HandlerContext;

public class ChannelReadEvent extends Event {

	public ChannelReadEvent(HandlerContext context, Object message) {
		super(context, message);
	}

	@Override
	protected void doRun(HandlerContext context, Object message) {
		context.fireChannelRead(message);
	}
	
}
