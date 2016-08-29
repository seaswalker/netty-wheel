package client;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import util.DataUtils;

public class Client {
	
	private Socket socket;
	private BufferedOutputStream bos;
	
	@Before
	public void init() throws IOException {
		socket = new Socket();
		socket.connect(new InetSocketAddress(8080));
		bos = new BufferedOutputStream(socket.getOutputStream());
	}

	@Test
	public void lengthFieldBasedDecoder() throws IOException, InterruptedException {
		byte[] result = new byte[35];
		System.arraycopy(DataUtils.int2Bytes(31), 0, result, 0, 4);
		System.arraycopy("org.apache.commons.lang.builder".getBytes(), 0, result, 4, 31);
		for (int i = 0; i < 6; i++) {
			bos.write(result);
		}
		TimeUnit.SECONDS.sleep(6);
	}
	
	@Test
	public void delimiterBasedDecoder() throws IOException {
		byte[] data = "This is a beautiful world.\n".getBytes();
		for (int i = 0; i < 12; i++) {
			bos.write(data);
		}
		bos.flush();
	}
	
	@Test
	public void response() throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		bos.write("skywalker".getBytes());
		bos.flush();
		System.out.println(br.readLine());
		br.close();
	}
	
	@After
	public void close() throws IOException {
		bos.close();
		socket.close();
	}
	
}
