package handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Handler调用链
 * 
 * @author skywalker
 *
 */
public class HandlerChain {

	private final List<InBoundHandler> inBoundHandlers;
	private final List<OutBoundHandler> outBoundHandlers;
	private List<InBoundHandler> unmodifiedInBoundHandlers;
	private List<OutBoundHandler> unmodifiedOutBoundHandlers;
	
	public HandlerChain() {
		this.inBoundHandlers = new ArrayList<>();
		this.outBoundHandlers = new ArrayList<>();
		this.outBoundHandlers.add(new DefaultOutBoundHandler());
	}
	
	/**
	 * 添加处理器
	 */
	public void addHandler(Handler handler) {
		if (handler instanceof InBoundHandler) {
			inBoundHandlers.add((InBoundHandler) handler);
		} else if (handler instanceof OutBoundHandler) {
			outBoundHandlers.add((OutBoundHandler) handler);
		}
	}
	
	public List<InBoundHandler> getInBoundHandlers() {
		if (unmodifiedInBoundHandlers == null) {
			unmodifiedInBoundHandlers = Collections.unmodifiableList(inBoundHandlers);
		}
		return unmodifiedInBoundHandlers;
	}
	
	public List<OutBoundHandler> getOutBoundHandlers() {
		if (unmodifiedOutBoundHandlers == null) {
			unmodifiedOutBoundHandlers = Collections.unmodifiableList(outBoundHandlers);
		}
		return unmodifiedOutBoundHandlers;
	}
	
}
