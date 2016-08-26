package handler.decoder;

import context.HandlerContext;
import handler.InBoundHandlerAdapter;

/**
 * 根据指定的长度读取相应的数据
 * 解决粘包和半包的问题
 * 
 * @author skywalker
 *
 */
public class LengthFieldBasedDecoder extends InBoundHandlerAdapter {

	@Override
	public void channelRead(Object message, HandlerContext context) {
	}
	
}
