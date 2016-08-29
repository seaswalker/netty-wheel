package selector;

import handler.HandlerChain;

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
import event.ChannelActiveEvent;
import event.ChannelInActiveEvent;
import event.ChannelReadEvent;
import lifecycle.LifeCycle;

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
	// 默认ByteBuffer分配大小
	private static final int defaultAllocateSize = 1024;
	private final ExecutorService executor;
	private boolean closed = false;
	private static final Logger logger = LoggerFactory
			.getLogger(QueuedSelector.class);
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
	 * Register the channel to the selector with the interested ops.
	 * 
	 * @return boolean 是否提交成功
	 */
	public boolean register(SocketChannel channel, int ops) {
		Register register = new Register(channel, ops);
		boolean result = false;
		lock.lock();
		try {
			result = jobs.offer(register);
		} finally {
			lock.unlock();
		}
		if (result) {
			// 唤醒Selector
			selector.wakeup();
		}
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
				SelectionKey key = channel.register(selector, ops);
				// fire the ChannelActiveEvent
				HandlerChain handlerChain = selectorManager.getHandlerChain();
				HandlerContext context = new HandlerContext(
						handlerChain.getInBoundHandlers(),
						handlerChain.getOutBoundHandlers(), true);
				context.setChannel(channel);
				context.setWorkerManager(workerManager);
				key.attach(context);
				workerManager.chooseOne(channel).submit(new ChannelActiveEvent(context));
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
							SocketChannel channel = (SocketChannel) key
									.channel();
							ByteBuffer buffer = ByteBuffer
									.allocate(defaultAllocateSize);
							int n = channel.read(buffer);
							if (n == -1) {
								processInActive(key);
							} else {
								processRead(buffer, key);
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
		 * 如果SelectionKey.attachment()返回空，那么重新构造一个HandlerContext， 否则使用原有的。
		 * 
		 * @param attachment
		 *            {@link Object}
		 * @param channel 
		 *            {@link SocketChannel}
		 * @return {@link HandlerContext}
		 */
		private HandlerContext checkAttachment(Object attachment, SocketChannel channel) {
			HandlerContext context;
			if (attachment == null) {
				HandlerChain handlerChain = selectorManager.getHandlerChain();
				context = new HandlerContext(handlerChain.getInBoundHandlers(),
						handlerChain.getOutBoundHandlers(), true);
				context.setChannel(channel);
				context.setWorkerManager(workerManager);
			} else {
				context = (HandlerContext) attachment;
			}
			return context;
		}

		/**
		 * 客户端断开连接
		 */
		private void processInActive(SelectionKey key) {
			HandlerContext context = checkAttachment(key.attachment(), (SocketChannel) key.channel());
			workerManager.chooseOne(context.getChannel()).submit(new ChannelInActiveEvent(context));
			key.cancel();
		}

		/**
		 * 处理读事件
		 * 
		 * @param buffer
		 *            {@link ByteBuffer} 读取的数据
		 * @param key
		 *            {@link SelectionKey}
		 * @throws IOException
		 */
		private void processRead(ByteBuffer buffer, SelectionKey key)
				throws IOException {
			HandlerContext context = checkAttachment(key.attachment(), (SocketChannel) key.channel());
			workerManager.chooseOne(context.getChannel()).submit(new ChannelReadEvent(context, buffer));
		}
	}

}
