package handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import context.HandlerContext;

/**
 * 测试回应情况.
 * 
 * @author skywalker
 *
 */
public class ResponseHandler extends InBoundHandlerAdapter {

    private static final Logger log = LoggerFactory.getLogger(ResponseHandler.class);

    @Override
    public void channelRead(Object message, HandlerContext context) {
        String messageStr = (String) message, response = (messageStr + "\n");
        log.info("收到请求详细: {}, 回复响应: {}.", message, response);
        context.writeFlush(response);
    }

}
