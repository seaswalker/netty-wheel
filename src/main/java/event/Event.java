package event;

import context.HandlerContext;

/**
 * 事件
 * 
 * @author skywalker
 *
 */
public abstract class Event implements Runnable {

	private HandlerContext ctx;
	private Object message;
	
	public Event(HandlerContext context, Object message) {
		this.ctx = context;
		this.message = message;
	}
	
	public Event(HandlerContext ctx) {
		this.ctx = ctx;
	}

	@Override
	public void run() {
		ctx.reset();
		doRun(ctx, message);
	}
	
	/**
	 * 子类真正的运行方法
	 */
	protected abstract void doRun(HandlerContext context, Object message);
	
}
