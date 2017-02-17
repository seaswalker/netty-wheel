package manager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import worker.Worker;

/**
 * 基于远程端口的选取策略，可以保证同一个连接的事件被分发到同一个线程。
 * 
 * @author skywalker
 *
 */
public class PortBasedChooseStrategy extends AbstractChooseStrategy<Worker> {

    private static final Logger logger = LoggerFactory.getLogger(PortBasedChooseStrategy.class);

    @Override
    public Worker doChoose(Object param) {
        if (!(param instanceof SocketChannel)) {
            throw new IllegalArgumentException(
                    "The param must be SocketChannel.");
        }
        Worker worker;
        try {
            SocketChannel channel = (SocketChannel) param;
            InetSocketAddress address = (InetSocketAddress) channel.getRemoteAddress();
            int port = address.getPort();
            worker = candidates.get(port % length);
        } catch (IOException e) {
            logger.debug("Remote connection has closed.");
            // 采用简单的递增策略
            worker = candidates.get(index);
        }
        return worker;
    }

}
