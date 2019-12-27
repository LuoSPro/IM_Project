package client;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import myutil.ChatProtocol;
import myutil.ChatResult;

/**
 * 客户端消息线程
 * 用以接收服务器消息
 * @author Administrator
 *
 */
public class ChatClientThread extends Thread {
	private DataInputStream dis;//输入流
	private GroupChatView groupChatView;//聊天窗口
	
	//初始化套接字与输入流
	public ChatClientThread(Socket socket,GroupChatView groupChatView) {
		this.groupChatView = groupChatView;//实例化，保证为同一对象
		//套接字
		try {
			dis=new DataInputStream(socket.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		//noinspection InfiniteLoopStatement
		while (true) {
			try {
				//解析消息
				ChatResult result = null;
				result = ChatProtocol.getResult(dis);
				if (result != null) {
					//根据消息类型处理
					handleType(result.getType(), result.getData());
				}
			}catch (Exception e){//用户抛出异常后，不再监听服务器发来的消息
				break;
			}
		}
	}
	
	/**
	 * 根据消息的类型对消息处理
	 * @param type 消息类型
	 * @param data 消息内容
	 */
	private void handleType(int type, byte[] data) {
		SimpleDateFormat df=new SimpleDateFormat("yyyy年MM月dd日 hh:mm:ss");
		String time=df.format(new Date());
		switch (type) {
			case 1:
				//文本
				String[] args=new String(data).split("说：");
				GroupChatView.area.append("  "+args[0]+"("+time+")\n  "+args[1]+"\n");
				break;
			case 6:
				//文件
				GroupChatView.area.append("  文件上传提醒："+"("+time+")\n  "+new String(data)+"\n");
				break;
			case 7://其他用户登录后，刷新自己的在线用户列表
				String names = new String(data);
				String[] namesArr = names.split(";");
				for(int i=0;i<namesArr.length;i++){
					groupChatView.addToUserList(namesArr[i],namesArr.length);
				}
				break;
			case 8://用户下线后，刷新用户的在线数目
				String names2 = new String(data);
				String[] namesArr2 = names2.split(";");
				for(int i=0;i<namesArr2.length;i++){
					groupChatView.reduceToUserList(namesArr2[i],namesArr2.length);
				}
				break;
			case 9:
				//用户非法操作，直接下线，并提醒
				System.out.println("因为你非法操作，现在被迫下线，请重新登录");
				GroupChatView.area.append("  因为你非法操作，现在被迫下线，请重新登录\n");
			case 4://有用户进入，通知大家
			case 5://有用户退出，通知大家
				GroupChatView.area.append("  "+new String(data)+"\n");
				break;
			default:
				break;
		}
		GroupChatView.area.select(GroupChatView.area.getText().length(), GroupChatView.area.getText().length());
	}
}