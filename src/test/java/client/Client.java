package client;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

public class Client {

	public static void main(String[] args) throws IOException, InterruptedException {
		Socket socket = new Socket();
		socket.connect(new InetSocketAddress(8080));
		BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
		bos.write("Hello, Server".getBytes());
		bos.flush();
		TimeUnit.SECONDS.sleep(5);
		bos.close();
		socket.close();
	}
	
}
