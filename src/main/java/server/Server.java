package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import handler.Handler;
import handler.HandlerChain;
import lifecycle.LifeCycle;
import manager.PortBasedChooseStrategy;
import selector.SelectorManager;
import worker.WorkerManager;

/**
 * Server
 * 
 * @author skywalker
 *
 */
public final class Server implements LifeCycle {

	private final ExecutorService executor;
	// 默认以阻塞的方式监听
	private boolean block = true;
	private ServerSocketChannel serverSocketChannel;
	private final SelectorManager selectorManager;
	private final WorkerManager workerManager;
	private final int acceptors;
	private final HandlerChain handlerChain;
	private final static Logger logger = LoggerFactory.getLogger(Server.class);

	public Server() {
		this(0, 0);
	}

	public Server(int acceptors, int workers) {
		int cores = Runtime.getRuntime().availableProcessors();
		if (acceptors <= 1) {
			acceptors = Math.max(2, Math.min(4, cores / 8));
		}
		if (workers <= 0) {
			workers = Math.max(2, Math.min(4, cores / 8));
		}
		if (acceptors > cores) {
			throw new IllegalArgumentException(
					"The acceptors count should be less than cores.");
		}
		if (workers > cores) {
			throw new IllegalArgumentException(
					"The workers count should be less than cores.");
		}
		executor = Executors.newFixedThreadPool(acceptors + workers);
		int selectors = (acceptors /= 2);
		selectorManager = new SelectorManager(selectors, executor);
		workerManager = new WorkerManager(workers, executor, new PortBasedChooseStrategy());
		this.acceptors = acceptors;
		this.handlerChain = new HandlerChain();
		selectorManager.setHandlerChain(handlerChain);
		selectorManager.setWorkerManager(workerManager);
	}

	/**
	 * 设置监听方式
	 */
	public Server configureBlocking(boolean block) {
		this.block = block;
		return this;
	}

	/**
	 * 监听到指定的端口
	 */
	public Server bind(int port) {
		if (port < 1)
			throw new IllegalArgumentException(
					"The param port can't be negative.");
		try {
			serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.socket().setReuseAddress(true);
			serverSocketChannel.bind(new InetSocketAddress(port));
			serverSocketChannel.configureBlocking(block);
		} catch (IOException e) {
			logger.error("The port " + port + "bind failed.");
			System.exit(1);
		}
		return this;
	}

	public Server setHandlers(Handler... handlers) {
		for (int i = 0, l = handlers.length; i < l; i++) {
			handlerChain.addHandler(handlers[i]);
		}
		return this;
	}

	/**
	 * 启动服务器
	 */
	@Override
	public void start() {
		workerManager.start();
		selectorManager.start();
		for (int i = 0; i < acceptors; i++)
			executor.execute(new Acceptor());
		logger.info("Server starts successfully.");
	}

	/**
	 * 接收客户端连接
	 * 
	 * @author skywalker
	 *
	 */
	private class Acceptor implements Runnable {

		@Override
		public void run() {
			while (true) {
				try {
					SocketChannel channel = serverSocketChannel.accept();
					channel.configureBlocking(false);
					boolean result = selectorManager.chooseOne(null).register(
							channel, SelectionKey.OP_READ);
					if (!result) {
						// register operation failed
						logger.debug("Register channel to selector failed.");
					}
					;
				} catch (IOException e) {
					logger.debug("Client accepted failded: " + e.getMessage());
				}
			}
		}

	}

}
