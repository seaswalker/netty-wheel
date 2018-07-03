package bootstrap;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import handler.Handler;
import handler.HandlerInitializer;
import handler.ResponseHandler;
import handler.SimpleInBoundHandler;
import handler.decoder.DelimiterBasedDecoder;
import handler.decoder.LengthFieldBasedDecoder;
import handler.decoder.StringDecoder;
import handler.encoder.StringEncoder;
import server.Server;

/**
 * 启动.
 * 
 * @author skywalker
 *
 */
public class ServerTest {

    private static int PORT = 8081;

    @Test
    public void lengthFieldBasedDecoder() throws InterruptedException {
        Server server = new Server();
        server.bind(PORT++).setHandlers(new HandlerInitializer() {
            @Override
            public Handler[] init() {
                return new Handler[] {new LengthFieldBasedDecoder(0, 4), new StringDecoder(), new SimpleInBoundHandler()};
            }
        }).start();

        TimeUnit.SECONDS.sleep(2);

        server.close();
    }

    @Test
    public void delimiterBasedDecoder() throws InterruptedException {
        Server server = new Server();
        server.bind(PORT++).setHandlers(new HandlerInitializer() {
            @Override
            public Handler[] init() {
                return new Handler[] {new DelimiterBasedDecoder('a'), new StringDecoder(), new SimpleInBoundHandler()};
            }
        }).start();

        TimeUnit.SECONDS.sleep(2);

        server.close();
    }

    @Test
    public void response() throws InterruptedException {
        Server server = new Server();
        server.bind(PORT++).setHandlers(new StringDecoder(), new ResponseHandler(), new StringEncoder()).start();

        TimeUnit.SECONDS.sleep(2);

        server.close();
    }

}
