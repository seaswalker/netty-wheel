package handler;

import context.HandlerContext;

/**
 * 数据读取事件处理器
 * @author skywalker
 *
 */
public interface InBoundHandler extends Handler {

	public void channelActive(HandlerContext context);
	
	public void channelInActive(HandlerContext context);
	
	public void channelRead(Object message, HandlerContext context);
	
}
