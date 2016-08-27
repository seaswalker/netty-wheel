package bootstrap;

import handler.Handler;
import handler.HandlerInitializer;
import handler.SimpleInBoundHandler;
import handler.decoder.StringDecoder;
import server.Server;

/**
 * 启动
 * 
 * @author skywalker
 *
 */
public class Bootstrap {

	public static void main(String[] args) {
		Server server = new Server();
		server.bind(8080).setHandlers(new HandlerInitializer() {
			@Override
			public Handler[] init() {
				return new Handler[] {new StringDecoder(), new SimpleInBoundHandler()};
			}
		}).start();
	}
	
}
