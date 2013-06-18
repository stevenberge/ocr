package capstone.project.server;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

import javax.imageio.ImageIO;

public class Server {

	public static void main(String[] args) {

		final int port = 8888;
		final String received = "./received.jpg";
		final String sent = "./sent.jpg";
		final String ans = "./ans.txt";
		
		File receivedFile = new File(received);
		File sentFile = new File(sent);

		byte[] inData;
		byte[] outData;
		ServerSocket serverSocket = null;
		Socket socket = null;
		DataInputStream dataInputStream = null;
		DataOutputStream dataOutputStream = null;
        BufferedReader reader = null;
        boolean breakOut = false;

		try {
			serverSocket = new ServerSocket(port);
			System.out.println("Listening On:" + port);
		} catch (IOException e) {
			e.printStackTrace();
		}

		while (true) {
			try {

				socket = serverSocket.accept();
				System.out.println("hello i am server, i get a client");
				dataInputStream = new DataInputStream(socket.getInputStream());
				dataOutputStream = new DataOutputStream(socket
						.getOutputStream());
				System.out.println("Received connection IP: "
						+ socket.getInetAddress());

				// Receive the length for allocation
				int length = dataInputStream.readInt();
				System.out.println("\tReceived allocation size: " + length);
				inData = new byte[length];

				// Receive data
				System.out.println("\tReading Data... ");
				dataInputStream.readFully(inData);
				System.out.println("\tReceived Data...Writing... ");
				
				// Get current sent state
				long initSentTime = sentFile.lastModified();

				// Save it
				InputStream in = new ByteArrayInputStream(inData);
				BufferedImage image = ImageIO.read(in);
				ImageIO.write(image, "jpg", receivedFile);
				System.out.println("\tSaved File ");
				
				//等待二十秒matlab处理
				// Wait 20s for it to be processed
				//long startProc = System.currentTimeMillis();
				//while (initSentTime >= sentFile.lastModified()) {
				//	if (System.currentTimeMillis() - startProc  > 20*1000) {
				//		breakOut = true;
				//		System.out.println("\tBreakout!");
				//		break;
				//	}
				//	sentFile = new File(sent);
				//	Thread.sleep( (long) 1000 );
				//}
				
				// Processed, send the processed file
				if (!breakOut) System.out.println("\tFile Processed");
//				返回结果图片..........
//				image = ImageIO.read(sentFile);
//				ByteArrayOutputStream baos = new ByteArrayOutputStream();
//				ImageIO.write( image, "jpg", baos );
//				baos.flush();
//				outData = baos.toByteArray();
//				baos.close();
//				
//				dataOutputStream.flush();
//				System.out.println("\tSending allocation size: " + outData.length);
//				dataOutputStream.writeInt(outData.length);
//				dataOutputStream.write(outData);
				System.out.println("\tSent File!");
				
				// 发送结果
				dataOutputStream.flush();
		        StringBuffer contents = new StringBuffer();
	            String text = null;
	            dataOutputStream.writeByte(5);
				
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (socket != null) {
					try {
						socket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				if (dataInputStream != null) {
					try {
						dataInputStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				if (dataOutputStream != null) {
					try {
						dataOutputStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (reader != null) {
                    try {
						reader.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
                }
			}
		}
	}
}
