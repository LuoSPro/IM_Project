package client;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import myutil.ChatProtocol;

/**
 * 封装客户端与服务器通信的细节
 */
public class ChatClient {
	GroupChatView groupChatView;
	//套接字
	Socket socket;
	//输出流
	DataOutputStream dos = null;

	public ChatClient(GroupChatView groupChatView) {
		this.groupChatView = groupChatView;
	}

	/**
	 * 连接服务器并初始化输出流
	 * 开启客户端线程负责消息的接收
	 * @param address 服务器IP地址
	 * @param port 服务器端口号
	 */
	public void conn(String address, int port) {
		try {
			socket = new Socket(address, port);
			dos = new DataOutputStream(socket.getOutputStream());
			new ChatClientThread(socket,groupChatView).start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 登录
	 * @param user 用户昵称
	 */
	public void load(String user) {
		ChatProtocol.send(ChatProtocol.TYPE_LOAD,user.getBytes(), dos);
	}

	/**
	 * 发送消息
	 * @param msg 消息内容
	 */
	public void sendMsg(String msg) {
		ChatProtocol.send(ChatProtocol.TYPE_TEXT, msg.getBytes(), dos);
	}

	public void sendFile(String fileName){
		ChatProtocol.send(ChatProtocol.TYPE_FILE,fileName.getBytes(),dos);
	}


	/**
	 * 退出
	 */
	public void logout(){
		ChatProtocol.send(ChatProtocol.TYPE_LOGOUT, "logout".getBytes(), dos);
	}
	
	/**
	 * 关闭客户端，释放掉资源
	 */
	public void close() {
		// 向服务器发送退出命令
		ChatProtocol.send(ChatProtocol.TYPE_LOGOUT, new String("logout").getBytes(), dos);
		// 关闭资源
		try {
			if (dos != null)
				dos.close();
			if (socket != null)
				socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}