package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

/**
 * 服务器类
 * 负责接受客户端的连接
 * 将客户端的连接交付给服务器端线程处理
 */
public class ChatServer {
	//维护客户端的配置信息
	public static final Map<String,UserInfo> clients = new HashMap<>();

	//主方法
	public static void main(String[] args) {
		try {
			ServerSocket serverSocket = new ServerSocket(8989);
			while (true) {
				Socket socket = serverSocket.accept();
				new Thread(new ChatServerThread(socket)).start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}