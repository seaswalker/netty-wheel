package handler.encoder;

import java.nio.ByteBuffer;

import context.HandlerContext;
import handler.OutBoundHandlerAdapter;

/**
 * 将String转为{@link ByteBuffer}.
 * 
 * @author skywalker
 *
 */
public class StringEncoder extends OutBoundHandlerAdapter {

    @Override
    public void channelWrite(Object message, HandlerContext context) {
        if (message instanceof String) {
            context.fireChannelWrite(ByteBuffer.wrap(((String) message).getBytes()));
        } else {
            context.fireChannelWrite(message);
        }
    }

}
