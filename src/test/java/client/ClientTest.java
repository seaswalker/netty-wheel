package client;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import handler.Handler;
import handler.HandlerInitializer;
import handler.ResponseHandler;
import handler.SimpleInBoundHandler;
import handler.decoder.DelimiterBasedDecoder;
import handler.decoder.LengthFieldBasedDecoder;
import handler.decoder.StringDecoder;
import org.junit.Assert;
import org.junit.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.Server;
import util.DataUtils;

/**
 * 测试-客户端角色.
 */
public class ClientTest {

    private static int PORT = 8081;

    private static final Logger log = LoggerFactory.getLogger(ClientTest.class);

    @Test
    public void testLengthFieldBasedDecoder() throws IOException, InterruptedException {
        Server server = new Server();
        int port = (PORT++);
        server.bind(port).setHandlers(new HandlerInitializer() {
            @Override
            public Handler[] init() {
                return new Handler[] {
                        new LengthFieldBasedDecoder(0, 4),
                        new StringDecoder(),
                        new SimpleInBoundHandler()
                };
            }
        }).start();

        TimeUnit.SECONDS.sleep(2);
        Pair pair = connectServer(port);

        BufferedOutputStream bos = pair.bos;
        byte[] result = new byte[35];
        System.arraycopy(DataUtils.int2Bytes(31), 0, result, 0, 4);
        System.arraycopy("org.apache.commons.lang.builder".getBytes(), 0, result, 4, 31);
        for (int i = 0; i < 2; i++) {
            bos.write(result);
        }
        bos.flush();

        BufferedReader br = new BufferedReader(new InputStreamReader(pair.socket.getInputStream()));
        String line = br.readLine();

        // readLine会吃掉换行符
        Assert.assertEquals("org.apache.commons.lang.builder", line);

        line = br.readLine();
        Assert.assertEquals("org.apache.commons.lang.builder", line);

        TimeUnit.SECONDS.sleep(6);

        bos.close();
        br.close();
        server.close();
    }

    @Test
    public void testDelimiterBasedDecoder() throws InterruptedException, IOException {
        Server server = new Server();
        int port = (PORT++);
        server.bind(port).setHandlers(new HandlerInitializer() {
            @Override
            public Handler[] init() {
                return new Handler[] {new DelimiterBasedDecoder('a'), new StringDecoder(), new ResponseHandler()};
            }
        }).start();

        TimeUnit.SECONDS.sleep(2);
        Pair pair = connectServer(port);

        BufferedOutputStream bos = pair.bos;
        byte[] data = "This isadoga".getBytes();
        bos.write(data);
        bos.flush();

        BufferedReader br = new BufferedReader(new InputStreamReader(pair.socket.getInputStream()));
        String line  = br.readLine();
        Assert.assertEquals("This is", line);

        line = br.readLine();
        Assert.assertEquals("dog", line);

        TimeUnit.SECONDS.sleep(2);

        bos.close();
        server.close();
    }

    private Pair connectServer(int port) throws IOException {
        Socket socket = new Socket();
        log.info("尝试连接: {}端口.", port);
        socket.connect(new InetSocketAddress("localhost", port), 1000);
        BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
        return new Pair(bos, socket);
    }

    private static class Pair {

        BufferedOutputStream bos;
        Socket socket;

        Pair(BufferedOutputStream bos, Socket socket) {
            this.bos = bos;
            this.socket = socket;
        }
    }

}
