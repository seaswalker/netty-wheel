package selector;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import worker.WorkerManager;
import context.HandlerContext;
import event.ChannelInActiveEvent;
import event.ChannelReadEvent;
import manager.LifeCycle;

/**
 * 拥有自己的工作队列的Selector
 * 
 * @author skywalker
 *
 */
public final class QueuedSelector implements Runnable, LifeCycle {

	private Selector selector;
	private final ArrayDeque<Runnable> jobs;
	private final static int defaultQueueSize = 100;
	//默认ByteBuffer分配大小
	private static final int defaultAllocateSize = 1024;
	private final ExecutorService executor;
	private boolean closed = false;
	private static final Logger logger = LoggerFactory.getLogger(QueuedSelector.class);
	private final Runnable eventProcessor = new EventProcessor();
	private final Lock lock = new ReentrantLock();
	private SelectorManager selectorManager;
	private WorkerManager workerManager;
	
	public QueuedSelector(ExecutorService executor) {
		this(0, executor);
	}

	public QueuedSelector(int capacity, ExecutorService executor) {
		if (capacity < 1) 
			capacity = defaultQueueSize;
		jobs = new ArrayDeque<>(defaultQueueSize);
		this.executor = executor;
	}
	
	public void setSelectorManager(SelectorManager selectorManager) {
		this.selectorManager = selectorManager;
	}
	
	public void setWorkerManager(WorkerManager workerManager) {
		this.workerManager = workerManager;
	}

	/**
	 * 启动Selector
	 */
	@Override
	public void start() {
		try {
			selector = Selector.open();
			executor.execute(this);
		} catch (IOException e) {
			logger.error("Selector open failed: " + e.getMessage());
		}
	}
	
	/**
	 * 向Selector注册事件
	 * @return 提交成功返回true
	 */
	public boolean register(SocketChannel channel, int ops) {
		Register register = new Register(channel, ops);
		boolean result;
		lock.lock();
		try {
			result = jobs.offer(register);
		} finally {
			lock.unlock();
		}
		//唤醒Selector
		selector.wakeup();
		return result;
	}


	@Override
	public void run() {
		while (!closed) {
			Runnable task;
			lock.lock();
			try {
				task = jobs.poll();
			} finally {
				lock.unlock();
			}
			if (task == null)
				eventProcessor.run();
			else
				task.run();
		} 
	}
	
	/**
	 * 向Selector注册感兴趣的事件
	 * 
	 * @author skywalker
	 *
	 */
	private class Register implements Runnable {
		
		private final SocketChannel channel;
		private final int ops;
		
		public Register(SocketChannel channel, int ops) {
			this.channel = channel;
			this.ops = ops;
		}

		@Override
		public void run() {
			try {
				channel.register(selector, ops);
			} catch (ClosedChannelException e) {
				logger.error("Channel register failed: " + e.getMessage());
			}
		}
		
	}
	
	/**
	 * 处理Selector事件
	 * 
	 * @author skywalker
	 *
	 */
	private class EventProcessor implements Runnable {
		
		@Override
		public void run() {
			try {
				int i = selector.select();
				if (i > 0) {
					Set<SelectionKey> keys = selector.selectedKeys();
					for (SelectionKey key : keys) {
						if (key.isReadable()) {
							SocketChannel channel = (SocketChannel) key.channel();
							ByteBuffer buffer = ByteBuffer.allocate(defaultAllocateSize);
							int n = channel.read(buffer);
							if (n == -1) {
								processInActive(key);
							} else {
								processRead(buffer);
							}
						} 
					}
					keys.clear();
				}
			} catch (IOException e) {
				logger.debug("Selector was closed.");
				closed = true;
			}
		}
		
		/**
		 * 客户端断开连接
		 */
		private void processInActive(SelectionKey key) {
			HandlerContext context = new HandlerContext(selectorManager.getHandlerChain(), true);
			workerManager.chooseOne().submit(new ChannelInActiveEvent(context));
			key.cancel();
		}

		/**
		 * 处理读事件
		 * @param channel 客户端连接
		 * @throws IOException 
		 */
		private void processRead(ByteBuffer buffer) throws IOException {
			//触发Handler链
			HandlerContext context = new HandlerContext(selectorManager.getHandlerChain(), true);
			workerManager.chooseOne().submit(new ChannelReadEvent(context, buffer));
		}
		
	}
	
}
