package handler.decoder;

import java.nio.ByteOrder;

import context.HandlerContext;
import handler.InBoundHandlerAdapter;

/**
 * 根据指定的长度读取相应的数据
 * 解决粘包和半包的问题
 * 
 * @author skywalker
 *
 */
public class LengthFieldBasedDecoder extends InBoundHandlerAdapter {
	
	//字节序
	private final ByteOrder byteOrder;
	//长度标志字节起始位置
	private final int offset;
	//长度标志位长度
	private final int length;
	//内容的最大长度
	private final int maxLength;
	//64MB
	private static final int defaultMaxLength = 64 * 1024 * 1024;

	public LengthFieldBasedDecoder(int offset, int length) {
		this(offset, length, defaultMaxLength);
	}

	public LengthFieldBasedDecoder(int offset, int length, int maxLength) {
		this(ByteOrder.LITTLE_ENDIAN, offset, length, maxLength);
	}

	public LengthFieldBasedDecoder(ByteOrder byteOrder, int offset, int length,
			int maxLength) {
		if (byteOrder == null) byteOrder = ByteOrder.LITTLE_ENDIAN;
		this.byteOrder = byteOrder;
		this.offset = offset;
		this.length = length;
		this.maxLength = maxLength;
	}

	@Override
	public void channelRead(Object message, HandlerContext context) {
		
	}
	
}
