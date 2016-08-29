package handler;

import context.HandlerContext;

/**
 * 数据输出事件处理器
 * 
 * @author skywalker
 *
 */
public interface OutBoundHandler extends Handler {

	public void channelWrite(Object message, HandlerContext context);
	
}
