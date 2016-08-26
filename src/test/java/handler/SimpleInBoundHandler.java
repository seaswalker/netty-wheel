package handler;

import context.HandlerContext;

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
		System.out.println(message.toString());
	}
	
}
