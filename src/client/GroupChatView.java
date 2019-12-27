package client;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

/**
 * 聊天视图
 * 
 * @author Administrator
 *
 */
public class GroupChatView {
	// 聊天记录文本域
	public static JTextArea area;

	// 客户端实体对象
	private ChatClient client=new ChatClient(GroupChatView.this);

	//传输的文件
	private File file;

	private JPanel userCountPanel;
	private int Y=0;
	private int userListWidth = 150;
	private JLabel userCountLabel;

	//保存用户名：
	private List<String> namesList = new ArrayList<>();

	//背景
	private String backgroundIcon = "drawable/0.jpg";

	/**
	 * 创建一个视图
	 * 47.102.206.167
	 * 127.0.0.1
	 */
	public void create() {
		// 连接服务器
		client.conn("47.102.206.167", 8989);
		//如果是用自己的电脑作为服务器，就将IP设为127.0.0.1
//		client.conn("127.0.0.1", 8989);


		//设置背景
		ImageIcon icon = new ImageIcon(backgroundIcon);
//		ImageIcon icon = new ImageIcon("")
		final JLabel imgLabel = new JLabel(icon);
		imgLabel.setBounds(-500,-200,1920,1080);

		//窗口
		JFrame frame = new JFrame("聊天程序");
		frame.getLayeredPane().add(imgLabel,new Integer(Integer.MIN_VALUE));
		JPanel j = (JPanel)frame.getContentPane();
		j.setOpaque(false);

		//用户在线情况
		JScrollPane jScrollPane = new JScrollPane();
		frame.add(jScrollPane, BorderLayout.WEST);
		userCountPanel = new JPanel();
//		userCountPanel.setBackground(Color.BLUE);
		userCountPanel.setLayout(null);
		userCountPanel.setOpaque(false);
		userCountPanel.setPreferredSize(new Dimension(userListWidth,450));//设置窗口大小,太小的话，不利于刷新
		jScrollPane.setViewportView(userCountPanel);
		jScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);//设置水平滚动条隐藏
//		jScrollPane.setOpaque(false);
		jScrollPane.getViewport().setOpaque(false);  //设置透明
		jScrollPane.setOpaque(false);  //设置透明
		renderTop();

		// 登录面板
		JPanel loadPanel = new JPanel();
		loadPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		loadPanel.setOpaque(false);
		frame.add(loadPanel, BorderLayout.NORTH);
		
		// 标签以及输入框
		final JLabel userLabel = new JLabel("   用户未登录");
		final JTextField userTextField = new JTextField(20);
		userTextField.setOpaque(false);
		JButton loadButton = new JButton("登录");
		loadButton.setContentAreaFilled(false);
		//用于显示当前用户在线数量
		userCountLabel = new JLabel();

		//背景选择功能
		JButton selectBGBtn = new JButton("背景选择");
		loadPanel.add(selectBGBtn,BorderLayout.WEST);
		selectBGBtn.addActionListener(e -> {//修改聊天背景
			Object[] obj2 ={ "<空>","1.jpg", "2.jpg", "3.jpg", "4.jpg", "5.jpg", "6.jpg",};
			backgroundIcon = (String) JOptionPane.showInputDialog(null,
					"请选择你的背景:\n", "背景选择", JOptionPane.PLAIN_MESSAGE,
					new ImageIcon("1.jpg"), obj2, "足球");
			if (!(backgroundIcon == null)){
				imgLabel.setIcon(new ImageIcon("drawable/"+backgroundIcon));
			}
		});

		//添加
		loadPanel.add(userLabel);
		loadPanel.add(userTextField);
		loadPanel.add(loadButton);
		loadPanel.add(userCountLabel);
		userCountLabel.setVisible(false);
		loadButton.setFocusPainted(false);

		//登录按钮的监听事件
		loadButton.addActionListener(e -> {
			String user = userTextField.getText();
			if (user != null && !user.equals("")) {
				client.load(user);
				userLabel.setText("   user:" + user);
				userTextField.setText("");
				userTextField.setVisible(false);
				loadButton.setVisible(false);
				userCountLabel.setVisible(true);
			}else{
				JOptionPane.showMessageDialog(null,"请输入用户名");
			}
		});
		
		//设置回车登录事件
		userTextField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == 10) {
					String user = userTextField.getText();
					if (user != null && !user.equals("")) {
						client.load(user);
						userLabel.setText("   user:" + user);
						userTextField.setText("");
						userTextField.setVisible(false);
						loadButton.setVisible(false);
						userCountLabel.setVisible(true);
					}
				}
			}
		});

		// 聊天记录面板
		JPanel topPanel = new JPanel();
		topPanel.setOpaque(false);
		loadPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		// 聊天记录文本域
		area = new JTextArea(14, 51);
		area.setBorder(BorderFactory.createLoweredBevelBorder());
		area.setEditable(false);
//		area.setOpaque(false);
//		area.select(0,0);
		area.setBackground(null);
		area.setOpaque(false);
		area.setLineWrap(true);
//		area.setLineWrap(true);
		// 滚动条
		JScrollPane jsp = new JScrollPane(area, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		//添加
		frame.add(topPanel);
		topPanel.add(jsp,BorderLayout.EAST);
		jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		jsp.getViewport().setOpaque(false);
		jsp.setOpaque(false);
		renderTop();


		//文件选择板块
		// 按钮面板
		JPanel fileButtonPanel = new JPanel();
		fileButtonPanel.setOpaque(false);
		fileButtonPanel.setBorder(BorderFactory.createEmptyBorder(0, 13, 20, 0));
		// 窗口属性值
		int WIDTH = 750;//窗口宽度
		fileButtonPanel.setPreferredSize(new Dimension(600, 40));
		fileButtonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

		// 文件选择按钮
		ImageIcon icon1 = new ImageIcon("drawable/wenjian .png");
		JButton fileSendButton = new JButton("上传/下载文件");
		fileSendButton.setIcon(icon1);
		fileSendButton.setContentAreaFilled(false);
		fileButtonPanel.add(fileSendButton);
		fileSendButton.setFocusPainted(false);
		topPanel.add(fileButtonPanel);

		// 底部输入面板
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
//		frame.add(bottomPanel,BorderLayout.SOUTH);
		bottomPanel.setPreferredSize(new Dimension(580, 155));//设置尺寸
		bottomPanel.setOpaque(false);
		topPanel.add(bottomPanel);//这句话很重要，不然聊天输入框的长度为窗口长度
		// 文本域
		final JTextArea ta = new JTextArea();
		ta.setBorder(BorderFactory.createLoweredBevelBorder());
		ta.setFont(new Font("宋体", Font.PLAIN, 15));
		ta.setPreferredSize(new Dimension(565, 100));
		ta.setOpaque(false);
		ta.setBorder(BorderFactory.createLoweredBevelBorder());
		ta.select(0, 0);
		ta.setLineWrap(true);
		//设置回车发送消息
		ta.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyChar() == KeyEvent.VK_ENTER) {
					if (!ta.getText().equals("") && userLabel.getText().contains("user:")) {
						client.sendMsg(ta.getText());
						ta.setText("");
					} else if("".equals(ta.getText())){
						JOptionPane.showMessageDialog(null,"请输入聊天内容");
					}else if (!userLabel.getText().contains("user:")){
						JOptionPane.showMessageDialog(null,"用户未登录");
					}
					e.consume();
				}
			}
		});

		//添加文件选择按钮按钮点击事件,λ表达式
		fileSendButton.addActionListener(e -> {
			if (userLabel.getText().contains("user:")){
				new GroupFileView(GroupChatView.this);//开启传输文件的窗口，并传输文件
			}else{
				JOptionPane.showMessageDialog(null,"用户未登录");
			}
		});

		//输入聊天输入框随鼠标的动态效果（作用不大，已取消）
		ta.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				if (ta.getText().equals("") || ta.getText().equals("//输入聊天内容"))
					ta.setText("");
			}

			@Override
			public void mouseExited(MouseEvent e) {
//				if (ta.getText().equals(""))
//					ta.setText("//输入聊天内容");
			}
		});

		// 按钮面板
		JPanel buttonPanel = new JPanel();
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));
		buttonPanel.setPreferredSize(new Dimension(WIDTH, 50));
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

		// 按钮
		JButton sendButton = new JButton("发送");
		sendButton.setContentAreaFilled(false);
		sendButton.setFocusPainted(false);
		//添加按钮点击发送事件，,λ表达式
		sendButton.addActionListener(e -> {
			if (ta.getText() != null && ta.getText().length() != 0 && userLabel.getText().contains("user:")) {
				if (file != null){
					client.sendFile(ta.getText());
				}else{
					client.sendMsg(ta.getText());
				}
				ta.setText("");
			}else if("//输入聊天内容".equals(ta.getText())){
				JOptionPane.showMessageDialog(null,"请输入聊天内容");
			} else {
				JOptionPane.showMessageDialog(null,"用户未登录或内容为空");
				System.out.println("用户未登录或内容为空");
			}
		});
		
		// 底部面板添加控件
		bottomPanel.add(ta);
		bottomPanel.add(sendButton);
		//添加窗口关闭自动退出系统事件
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				client.logout();
			}
		});

		// 窗口设置
		int HEIGHT = 510;//窗口高度
		frame.setSize(WIDTH, HEIGHT);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//设置关闭窗口时，结束进程
	}


	/**
	 *   渲染顶部面板
	 */
	private void renderTop(){
		userCountPanel.removeAll();//将全部列表信息移除，方便重新渲染
		Y = 0;
	}

	/**
	 渲染用户列表
	 */
	public  void addToUserList(String userName,int length){
		if (namesList.contains(userName)){
			return;
		}
		namesList.add(userName);
		userCountLabel.setText("                   当前在线用户数量："+length);
		JLabel userIcon = new JLabel(new ImageIcon("drawable/userIcon.png"));//设置用户图标
		JLabel userNameLab = new JLabel(userName);
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(1,3,0,0));
		panel.add(userIcon);
		panel.add(userNameLab);

		panel.setBounds(2,Y,userListWidth,30);
		this.userCountPanel.add(panel);
		Y+=30;

		panel.addMouseListener(new MouseAdapter() {
			//鼠标移入时
			public void mouseEntered(MouseEvent e) { // 鼠标移动到这里的事件
				panel.setBackground(Color.orange);
				panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // 让鼠标移动到
			}
			public void mouseExited(MouseEvent e) { // 鼠标离开的事件
				panel.setBackground(Color.white);
			}
		});
	}

	/**
	 渲染用户列表
	 */
	public  void reduceToUserList(String userName,int length){
		if (Y/30-1==length){
			userCountPanel.removeAll();
			Y = 0;
		}
		userCountLabel.setText("                   当前在线用户数量："+length);
		JLabel userIcon = new JLabel(new ImageIcon("drawable/userIcon.png"));//设置用户图标
		JLabel userNameLab = new JLabel(userName);
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(1,3,0,0));
		panel.add(userIcon);
		panel.add(userNameLab);

		panel.setBounds(2,Y,userListWidth,30);
		this.userCountPanel.add(panel);
		Y+=30;

		panel.addMouseListener(new MouseAdapter() {
			//鼠标移入时
			public void mouseEntered(MouseEvent e) { // 鼠标移动到这里的事件
				panel.setBackground(Color.orange);
				panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // 让鼠标移动到
			}
			public void mouseExited(MouseEvent e) { // 鼠标离开的事件
				panel.setBackground(Color.white);
			}
		});
	}

	public void sendFileView(File file){
		if (file != null){
			String[] filePath = file.getPath().split("\\\\");//获取文件名
			client.sendFile(filePath[filePath.length-1]);//发送文件名
		}
	}
}