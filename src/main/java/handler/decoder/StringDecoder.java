package handler.decoder;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import context.HandlerContext;
import handler.InBoundHandlerAdapter;

/**
 * 将ByteBuffer或byte[]转为字符串, 仅支持Heap ByteBuffer.
 * 
 * @author skywalker
 *
 */
public class StringDecoder extends InBoundHandlerAdapter {

    private static final Charset defaultCharSet = StandardCharsets.UTF_8;
    private static final Logger logger = LoggerFactory.getLogger(StringDecoder.class);
    private final Charset charset;

    public StringDecoder() {
        this(null);
    }

    public StringDecoder(Charset charset) {
        if (charset != null) {
            this.charset = charset;
        } else {
            this.charset = defaultCharSet;
        }
    }

    @Override
    public void channelRead(Object message, HandlerContext context) {
        if (message == null) return;
        byte[] array = null;
        if (message instanceof ByteBuffer) {
            ByteBuffer buffer = (ByteBuffer) message;
            if (buffer.hasArray()) {
                array = buffer.array();
            } else {
                logger.debug("We support heap ByteBuffer only.");
            }
        } else if (message instanceof byte[]) {
            array = (byte[]) message;
        }
        if (array != null) {
            message = new String(array, charset);
        }
        context.fireChannelRead(message);
    }

}
