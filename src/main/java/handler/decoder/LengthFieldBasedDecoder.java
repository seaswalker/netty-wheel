package handler.decoder;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import context.HandlerContext;
import handler.HandlerInitializer;
import handler.InBoundHandlerAdapter;

/**
 * 根据指定的长度读取相应的数据
 * 解决粘包和半包的问题
 * 此Handler应该配合 {@link HandlerInitializer}使用
 * 
 * @author skywalker
 *
 */
public class LengthFieldBasedDecoder extends InBoundHandlerAdapter {
	
	private static final Logger logger = LoggerFactory.getLogger(LengthFieldBasedDecoder.class);
	
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
	
	public LengthFieldBasedDecoder(ByteOrder byteOrder, int offset, int length) {
		this(byteOrder, offset, length, defaultMaxLength);
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
		if (message instanceof ByteBuffer) {
			ByteBuffer buffer = (ByteBuffer) message;
			if (buffer.hasArray()) {
				buffer.flip();
				int count = buffer.limit();
				if (count < (offset + length)) {
					//头部不足
					return;
				} else {
					byte[] array = buffer.array();
					int contentLength = bytes2Int(array);
					if (contentLength > maxLength) contentLength = maxLength;
				}
			} else {
				logger.debug("We support heap ByteBuffer only.");
			}
		}
	}
	
	/**
	 * 将byte数组转为int值
	 * @param data byte数组
	 * @return int
	 */
	private int bytes2Int(byte[] data) {
		int result = 0;
		if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
			for (int i = 0; i < length; i++) {
				result |= ((data[offset + i] & 0xff) << (i << 3));
			}
		} else {
			for (int i = 0; i < length; i++) {
				result |= ((data[offset + i] & 0xff) << ((length - 1 - i) << 3));
			}
		}
		return result;
	}
	
}
