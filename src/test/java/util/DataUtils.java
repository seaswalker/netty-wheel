package util;

public class DataUtils {

	public static byte[] int2Bytes(int i) {
		byte[] arr = new byte[4];
		arr[0] = (byte) (i & 0xff);
		arr[1] = (byte) ((i >> 8) & 0xff);
		arr[2] = (byte) ((i >> 16) & 0xff);
		arr[3] = (byte) ((i >> 24) & 0xff);
		return arr;
	}
	
}
