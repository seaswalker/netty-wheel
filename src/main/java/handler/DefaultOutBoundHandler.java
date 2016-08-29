package handler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import context.HandlerContext;

/**
 * 负责最后真正向客户端发送数据
 * 
 * @author skywalker
 *
 */
public class DefaultOutBoundHandler implements OutBoundHandler {
	
	private static final Logger logger = LoggerFactory.getLogger(DefaultOutBoundHandler.class);

	@Override
	public void channelWrite(Object message, HandlerContext context) {
		if (message == null) return;
		SocketChannel channel = context.getChannel();
		try {
			ByteBuffer result = null;
			if (message instanceof ByteBuffer) {
				result = (ByteBuffer) message;
			} else if (message instanceof byte[]) {
				result = ByteBuffer.wrap((byte[]) message);
			} else if (message instanceof String) {
				result = ByteBuffer.wrap(message.toString().getBytes());
			}
			if (result == null) {
				logger.debug("Unsupported type: " + message.getClass().getName());
			} else {
				channel.write(result);
			}
		} catch (IOException e) {
			try {
				logger.error("Write to " + channel.getRemoteAddress().toString() + " failed.");
			} catch (IOException e1) {
				logger.error(e1.getMessage());
			}
		}
	}

}
