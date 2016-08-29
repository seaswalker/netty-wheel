package handler.decoder;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import context.HandlerContext;
import handler.HandlerInitializer;
import handler.InBoundHandlerAdapter;

/**
 * 根据指定的长度读取相应的数据, 解决粘包和半包的问题.
 * 处理的结果以byte[]的形式传至下一个Handler.
 * 此Handler应该配合 {@link HandlerInitializer}使用.
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
	private final int dataOffset;
	private State state = State.INIT;
	private byte[] todo;
	//还需要的数目
	private int needed = 0;
	private int neededOffset = 0;
	private static final int defaultMaxLength = 2048;

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
		this.dataOffset = offset + length;
		this.maxLength = maxLength - this.dataOffset;
	}

	@Override
	public void channelRead(Object message, HandlerContext context) {
		if (message instanceof ByteBuffer) {
			ByteBuffer buffer = (ByteBuffer) message;
			if (buffer.hasArray()) {
				buffer.flip();
				List<byte[]> out = new ArrayList<>();
				int remaining = buffer.remaining();
				switch (state) {
				case INIT:
					process(buffer, 0, out);
					break;
				case HEAD_NEEDED:
					//补齐head
					if (remaining < needed) {
						//头部依然不足
						System.arraycopy(buffer.array(), 0, todo, neededOffset, remaining);
						neededOffset += remaining;
					} else if (remaining == needed) {
						System.arraycopy(buffer.array(), 0, todo, neededOffset, remaining);
						state = State.CONTENT_NEEDED;
						int contentLength = bytes2Int(todo, offset, length);
						if (contentLength > maxLength) {
							logger.debug("The content length " + contentLength + "exceeds the max length " + maxLength);
							break;
						}
						byte[] arr = new byte[dataOffset + contentLength];
						System.arraycopy(todo, 0, arr, 0, dataOffset);
						todo = arr;
						needed = contentLength;
						neededOffset = dataOffset;
					} else {
						System.arraycopy(buffer.array(), 0, todo, neededOffset, remaining);
						int contentLength = bytes2Int(todo, offset, length);
						if (contentLength > maxLength) {
							logger.debug("The content length " + contentLength + "exceeds the max length " + maxLength);
							break;
						}
						byte[] arr = new byte[dataOffset + contentLength];
						System.arraycopy(todo, 0, arr, 0, dataOffset);
						remaining -= dataOffset;
						if (remaining < contentLength) {
							System.arraycopy(buffer.array(), needed, arr, dataOffset, remaining);
							state = State.CONTENT_NEEDED;
							todo = arr;
							needed = contentLength - remaining;
							neededOffset = remaining;
						} else if (remaining == contentLength) {
							System.arraycopy(buffer.array(), needed, arr, dataOffset, remaining);
							out.add(arr);
							todo = null;
							state = State.INIT; 
						} else {
							System.arraycopy(buffer.array(), needed, arr, dataOffset, contentLength);
							out.add(arr);
							todo = null;
							process(buffer, needed + contentLength, out);
						}
					}
					break;
				case CONTENT_NEEDED:
					if (remaining < needed) {
						//内容不足
						System.arraycopy(buffer.array(), 0, todo, neededOffset, remaining);
						neededOffset += remaining;
						needed -= remaining;
					} else if (remaining == needed) {
						System.arraycopy(buffer.array(), 0, todo, neededOffset, remaining);
						state = State.INIT;
						out.add(todo);
						todo = null;
					} else {
						System.arraycopy(buffer.array(), 0, todo, neededOffset, needed);
						out.add(todo);
						process(buffer, needed, out);
					}
				}
				context.fireChannelReads(out);
			} else {
				logger.debug("We support heap ByteBuffer only.");
			}
		} else {
			context.fireChannelRead(message);
		}
	}
	
	/**
	 * 处理从head开始的一组数据
	 * 
	 * @param buffer 缓冲区
	 * @param begin 开始处理的位置
	 * @param out 结果集
	 */
	private void process(ByteBuffer buffer, int begin, List<byte[]> out) {
		//读取到的数据总量
		int limit = buffer.limit(), remaining = limit - begin;
		byte[] array = buffer.array();
		while (begin < limit) {
			//头部不足
			if (remaining < dataOffset) {
				byte[] head = new byte[dataOffset];
				System.arraycopy(array, begin, head, 0, remaining);
				state = State.HEAD_NEEDED;
				needed = dataOffset - remaining;
				neededOffset = remaining;
				todo = head;
				break;
			} 
			int contentLength = bytes2Int(array, begin + offset, length);
			if (contentLength > maxLength) {
				logger.debug("The content length " + contentLength + "exceeds the max length " + maxLength);
				break;
			}
			remaining -= dataOffset;
			byte[] result = new byte[dataOffset + contentLength];
			if (remaining < contentLength) {
				System.arraycopy(array, begin, result, 0, dataOffset + remaining);
				state = State.CONTENT_NEEDED;
				todo = result;
				needed = contentLength - remaining;
				neededOffset = dataOffset + remaining;
				break;
			} 
			if (remaining == contentLength) {
				System.arraycopy(array, begin, result, 0, dataOffset + remaining);
				state = State.INIT;
				needed = 0;
				todo = null;
				out.add(result);
				break;
			}
			int total = dataOffset + contentLength;
			System.arraycopy(array, begin, result, 0, total);
			out.add(result);
			remaining -= contentLength;
			begin += total;
		}
	}
	
	/**
	 * 将byte数组转为int值
	 * 
	 * @param data byte数组
	 * @return int
	 */
	private int bytes2Int(byte[] data, int offset, int length) {
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
	
	private static enum State {
		//初始状态，需要读取头部
		INIT,
		CONTENT_NEEDED,
		HEAD_NEEDED
	}
	
}
