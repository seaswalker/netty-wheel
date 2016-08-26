package context;

import java.nio.channels.SocketChannel;
import java.util.List;

import event.ChannelWriteEvent;
import handler.HandlerChain;
import handler.InBoundHandler;
import handler.OutBoundHandler;
import worker.WorkerManager;

/**
 * Handler执行上下文，负责handler之间的转发
 * 
 * @author skywalker
 *
 */
public class HandlerContext {

	private final List<InBoundHandler> inBoundHandlers;
	private final List<OutBoundHandler> outBoundHandlers;
	private int index;
	private final int inBoundSize, outBoundSize;
	private WorkerManager workerManager;
	private final HandlerChain handlerChain; 
	private SocketChannel channel;
	
	public HandlerContext(HandlerChain handlerChain, boolean isInBound) {
		this.handlerChain = handlerChain;
		this.inBoundHandlers = handlerChain.getInBoundHandlers();
		this.outBoundHandlers = handlerChain.getOutBoundHandlers();
		this.inBoundSize = inBoundHandlers.size();
		this.outBoundSize = outBoundHandlers.size();
		this.index = isInBound ? 0 : (outBoundSize - 1);
	}
	
	public void setWorkerManager(WorkerManager workerManager) {
		this.workerManager = workerManager;
	}
	
	/**
	 * 直接使用Channel原生方法返回方法数据不会被OutBoundHandler处理
	 */
	public SocketChannel getChannel() {
		return channel;
	}

	public void setChannel(SocketChannel channel) {
		this.channel = channel;
	}

	/**
	 * 触发连接建立事件
	 */
	public void fireChannelActive() {
		if (index >= inBoundSize) return;
		InBoundHandler handler = inBoundHandlers.get(index);
		++index;
		handler.channelActive(this);
	}
	
	/**
	 * 触发连接断开事件
	 */
	public void fireChannelInActive() {
		if (index >= inBoundSize) return;
		InBoundHandler handler = inBoundHandlers.get(index);
		++index;
		handler.channelInActive(this);
	}
	
	/**
	 * 触发消息读取事件
	 * @param message 消息
	 */
	public void fireChannelRead(Object message) {
		if (index >= inBoundSize) return;
		InBoundHandler handler = inBoundHandlers.get(index);
		++index;
		handler.channelRead(message, this);
	}
	
	/**
	 * 触发消息写出事件
	 * @param message 消息
	 */
	public void fireChannelWrite(Object message) {
		if (index < 0) return;
		OutBoundHandler handler = outBoundHandlers.get(index);
		--index;
		handler.channelWrite(message, this);
	}
	
	/**
	 * 向客户端返回数据，触发OutBound事件
	 * @param message 数据
	 */
	public void writeFlush(Object message) {
		HandlerContext context = new HandlerContext(handlerChain, false);
		workerManager.chooseOne().submit(new ChannelWriteEvent(context, message));
	}
	
}
