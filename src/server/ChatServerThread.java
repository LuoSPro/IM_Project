package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.*;

import myutil.ChatProtocol;
import myutil.ChatResult;

/**
 * 服务器端线程
 * 负责与客户端通信
 * @author Administrator
 *
 */
public class ChatServerThread implements Runnable{
	//套接字
	public Socket socket;
	
	//输入、输出流
	public DataInputStream dis=null;
	public DataOutputStream dos=null;
	
	//用户昵称
	public String userName=null;

	//标志线程是否生存
	public boolean isLive=true;
	
	/**
	 * 构造服务器端线程实体
	 * 初始化输入、输出流
	 * @param socket 客户端套接字
	 */
	public ChatServerThread(Socket socket){
		this.socket=socket;
		try {
			dis=new DataInputStream(socket.getInputStream());
			dos=new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 线程体
	 */
	public void run() {
		while(isLive){
			try {
				//解析消息
				ChatResult result;
				result = ChatProtocol.getResult(dis);
				if(result!=null){
					//按类型处理
					handleType(result.getType(),result.getData());
				}
			}catch (Exception exception){//异常抛出时，不再管理该用户，让其自动下线
				System.out.println("【"+userName+"】出错了，被迫下线");
				ChatServer.clients.keySet().stream().forEach(e-> {//通知出错的用户，他被强迫下线了
					DataOutputStream dos2 = ChatServer.clients.get(e).getDataOutputStream();
					if (e.equals(userName)) {
						//提示用户已下线
						ChatProtocol.send(ChatProtocol.TYPE_OUTLINE, ("").getBytes(), dos2);
					}
				});
				//移掉该用户
				ChatServer.clients.remove(userName);
				//将剩下的用户名传给客户端
				Iterator<Map.Entry<String,UserInfo>> iter = ChatServer.clients.entrySet().iterator();
				StringBuffer buffer = new StringBuffer();
				while (iter.hasNext()){
					buffer.append(iter.next().getKey()).append(";");
				}
				ChatServer.clients.keySet().stream().forEach(e->{
					DataOutputStream  dos2= ChatServer.clients.get(e).getDataOutputStream();
					//提示所有人，xxx已下线
					ChatProtocol.send(ChatProtocol.TYPE_LOADSUCCESS, ("   系统："+userName+"退出聊天室").getBytes(), dos2);
					//返回用户在线信息
					ChatProtocol.send(ChatProtocol.TYPE_USEREXIT,(String.valueOf(buffer)).getBytes(),dos2);
				});
				isLive = false;
			}
		}
	}

	private String getPrivateChatReceiver(String message){
		String receiver=null;
		if(message!=null){
			if(message.startsWith("@")){
				int spaceIndex=message.indexOf(" ");
				receiver=message.substring(1,spaceIndex);
			}
		}
		return receiver;
	}

	private void groupChat(String message){
		//遍历集合，获取输出流
		//向所有用户转发消息
		Set<String> namesSet = ChatServer.clients.keySet();
		Iterator<String> iter = namesSet.iterator();
		for(int i = 0; iter.hasNext(); i++){
			String name = iter.next();
			if (i==0){
				System.out.println("user:【"+userName+"】send message:  "+message);
			}
			DataOutputStream  dos2= ChatServer.clients.get(name).getDataOutputStream();
			ChatProtocol.send(ChatProtocol.TYPE_TEXT,(userName+"说："+message).getBytes(),dos2);
		}
	}

	private void groupFile(String message){
		//遍历集合，获取输出流
		//向所有用户转发文件共享消息
		Set<String> namesSet = ChatServer.clients.keySet();
		Iterator<String> iter = namesSet.iterator();
		for(int i = 0; i< ChatServer.clients.size(); i++){
			String name = iter.next();
			if (i==0){
				System.out.println("user:【"+userName+"】send file:  "+message+" 到群中");
			}
			DataOutputStream  dos2= ChatServer.clients.get(name).getDataOutputStream();
			ChatProtocol.send(ChatProtocol.TYPE_FILE,(userName+"分享【"+message+"】文件到群中，欢迎大家下载").getBytes(),dos2);
		}
	}

	private void privateChat(String message,String receiver){
		//遍历集合，获取输出流
		//向receiver转发消息
		DataOutputStream  dos2= ChatServer.clients.get(receiver).getDataOutputStream();
		ChatProtocol.send(ChatProtocol.TYPE_TEXT,(userName+"对你说："+message).getBytes(),dos2);

		//把消息也给自己转发一下，显示在发送者自己的聊天内容中
		ChatProtocol.send(ChatProtocol.TYPE_TEXT,("你对【"+receiver+"】说："+message).getBytes(),dos);
	}

	/**
	 * 根据消息类型执行相应操作
	 * @param type 类型
	 * @param data 消息内容
	 */
	public void handleType(int type, byte[] data) {
		switch (type) {
			case 1:
				String message=new String(data);
				String receiver=null;
				if(message.startsWith("@")){
					int spaceIndex=message.indexOf(" ");
					receiver=message.substring(1,spaceIndex);
					message=message.substring(spaceIndex+1);
					System.out.println("【私聊消息】  user:【"+userName+"】send message:"+message+", receiver:"+receiver);
				}
				//私聊
				if(receiver!=null){
					privateChat(message,receiver);
				} else{//群聊
					groupChat(message);
				}
				break;
			case 6:
				String fileMessage=new String(data);
				groupFile(fileMessage);//群文件
				break;
			case 2://用户登录成功时，也要刷新用户列表,type=2,7
				//设置配置信息并添加至服务器端的集合中
				userName=new String(data);
				UserInfo userInfo = new UserInfo();
				userInfo.setUsername(userName);
				userInfo.setDataOutputStream(dos);
				ChatServer.clients.put(userName,userInfo);
			case 7:
				Iterator<Map.Entry<String,UserInfo>> iter = ChatServer.clients.entrySet().iterator();
				StringBuffer buffer = new StringBuffer();
				while (iter.hasNext()){
					buffer.append(iter.next().getKey()+";");//客户端通过";"来区分不同的用户名
				}
				ChatServer.clients.keySet().stream().forEach(e->{
					DataOutputStream  dos2= ChatServer.clients.get(e).getDataOutputStream();
					ChatProtocol.send(ChatProtocol.TYPE_LOADSUCCESS, ("   系统："+userName+"进入聊天室").getBytes(), dos2);
					ChatProtocol.send(ChatProtocol.TYPE_USERLOAD,(String.valueOf(buffer)).getBytes(),dos2);
				});
				break;
			case 3://用户退出时，也要刷新用户列表,type=3,8
				//删除集合中保存的该客户端信息
				ChatServer.clients.remove(userName);
				isLive=false;
			case 8:
				iter = ChatServer.clients.entrySet().iterator();
				buffer = new StringBuffer();
				while (iter.hasNext()){
					buffer.append(iter.next().getKey()+";");
				}
				//告知所有用户有人要退出聊天室
				ChatServer.clients.keySet().stream().forEach(e->{
					DataOutputStream  dos2= ChatServer.clients.get(e).getDataOutputStream();
					ChatProtocol.send(ChatProtocol.TYPE_LOADSUCCESS, ("   系统："+userName+"退出聊天室").getBytes(), dos2);
					ChatProtocol.send(ChatProtocol.TYPE_USEREXIT,(String.valueOf(buffer)).getBytes(),dos2);
				});
				break;
			default:
				break;
		}
	}
}