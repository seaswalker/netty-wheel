package handler;

import context.HandlerContext;

/**
 * 测试回应情况
 * 
 * @author skywalker
 *
 */
public class ResponseHandler extends InBoundHandlerAdapter {

	@Override
	public void channelRead(Object message, HandlerContext context) {
		context.writeFlush("Hello: " + (String) message + "!\n");
	}
	
}
