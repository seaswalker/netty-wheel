package handler.decoder;

import handler.HandlerInitializer;

/**
 * 基于换行符的decoder，应该配合{@link HandlerInitializer}使用.
 * 以byte数组的形式传向下一个Handler，分隔符不含在内。
 * 
 * @author skywalker
 *
 */
public class LineBasedDecoder extends DelimiterBasedDecoder {

	public LineBasedDecoder(char delimiter) {
		super(delimiter);
	}
	
}
