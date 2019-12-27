package client;
import myutil.ParseDataUtil;


import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class GroupFileView extends JFrame{
    private GroupChatView view;
    //窗口尺寸
    private int width = 400;
    private int height = 600;

    private JButton uploadButton;//上传按钮
    private JLabel flushLabel;//刷新标签
    private JPanel staffPanel;      //在JSpanel上的panel

    private PrintStream serverOut;
    private BufferedReader clientIn;
    private String ip = "47.102.206.167";//服务器地址
    //如果是用自己的电脑作为服务器，就将IP设为127.0.0.1
//		client.conn("127.0.0.1", 8989);

    private File currentUpploadFile;
    private String downloadSavePath;
    private int Y = 0;

    public GroupFileView(GroupChatView view) {
        this.view = view;
        start();
    }


    public void start(){
        //1-初始化
        initVariable();
        //2-连接服务器
        connectServer();
        //3-注册监听
        registerListener();
        //4-初始化窗口设置
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(width,height);
        this.setTitle("群文件");
        this.setLocationRelativeTo(null);//窗口居中显示
        this.setResizable(false);
        this.setVisible(true);
        //设置关闭窗口时的事件，默认为：关闭窗口，结束进程
        //此时为：调用任意已注册 WindowListener 的对象后自动隐藏并释放该窗体
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }


    private void initVariable() {
        //文件列表模块
        JScrollPane jScrollPane = new JScrollPane();//文件列表板块
        this.getContentPane().add(jScrollPane);

        staffPanel = new JPanel();//加在文件列表板块上的面板
        ///staffPanel.setLayout(new BoxLayout(staffPanel,BoxLayout.Y_AXIS));
        staffPanel.setLayout(null);
        staffPanel.setOpaque(false);

        staffPanel.setPreferredSize(new Dimension(width,height));//设置窗口大小
        jScrollPane.setViewportView(staffPanel);
        jScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);//设置水平滚动条隐藏
        jScrollPane.getViewport().setOpaque(false);  //设置透明
        jScrollPane.setOpaque(false);  //设置透明

        renderTop();//渲染顶部面板
    }


    /**
     *      向服务器重新读取群文件列表
     */
    private void loadGroupFile() {
        serverOut.println("@action=loadFileList");
    }


    /**
     *   渲染顶部面板
     */
    private void renderTop(){
        staffPanel.removeAll();//将全部列表信息移除，方便重新渲染
        Y = 0;
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(1,3,3,10));
        //头顶那一栏操作“行”信息
        //窗口标签
        JLabel groupLabel = new JLabel("\t\t\t\t\t群文件列表 ");
        this.uploadButton = new JButton("上传文件 ");
        flushLabel = new JLabel(new ImageIcon("drawable/flush.png"));//设置刷新图标
        //添加
        panel.add(groupLabel);
        panel.add(uploadButton);
        panel.add(flushLabel);

        panel.setBounds(2,Y,width,30);
        this.staffPanel.add(panel);
        Y += 30;
    }
    /**
     渲染文件列表
     */
    public  void addToFileList(String filename){
        JLabel fileicon = new JLabel(new ImageIcon("drawable/file.png"));//设置文件图标
        JButton downloadBtn = new JButton("下载");
        JLabel fileNameLab = new JLabel(filename);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(1,3,0,0));
        panel.add(fileicon);
        panel.add(fileNameLab);
        panel.add(downloadBtn);
        //panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        panel.setBounds(2,Y,width,30);
        this.staffPanel.add(panel);
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

        //文件下载
        downloadBtn.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                //1-选择下载保存的位置
                JFileChooser f = new JFileChooser(); // 查找文件
                f.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                f.showOpenDialog(null);
                File file = f.getSelectedFile();
                if(file != null){
                    downloadSavePath  = file.getPath();
                    //向服务器请求下载
                    serverOut.println("@action=Download["+filename+":null:null]");
                }
            }
        });
    }
    /**
     *   注册监听
     */
    private void registerListener() {
        //上传文件    消息格式: @action=Upload["fileName":"fileSize":result]
        this.uploadButton.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                JFileChooser f = new JFileChooser(); // 查找文件
                f.showOpenDialog(null);//默认打开文件的位置
                currentUpploadFile = f.getSelectedFile();//获得当前选择的文件
                if(currentUpploadFile != null){
                    serverOut.println("@action=Upload["+currentUpploadFile.getName()
                            +":"+currentUpploadFile.length()+":null]");
                    view.sendFileView(currentUpploadFile);
                }
            }
        });

        //刷新文件列表按钮
        flushLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                loadGroupFile();
            }
            //鼠标移入时
            public void mouseEntered(MouseEvent e) { // 鼠标移动到这里的事件
                flushLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // 让鼠标移动到
            }
        });
    }

    /**
     *  连接服务器
     */
    private void connectServer(){
        //连接服务器
        try {
            //初始化
            //socket
            int port = 5203;
            Socket clientSocket = new Socket(ip, port);
            serverOut = new PrintStream(clientSocket.getOutputStream(),true);
            clientIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            //读取文件列表
            serverOut.println("@action=loadFileList");

            //开启线程监听服务器消息
            new ClientThread().start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *    监听服务器消息
     */
    class ClientThread extends Thread{
        public void run() {
            try {
                String fromServerData;
                int flag = 0;

                while((fromServerData=clientIn.readLine()) != null){
                    //读取群文件列表
                    if(flag++ == 0){
                        if (fromServerData.startsWith("@action=GroupFileList")){
                            String[] fileList = ParseDataUtil.getFileList(fromServerData);
                            for (String filename : fileList) {
                                addToFileList(filename);
                            }
                        }
                        continue;
                    }
                    if(fromServerData.startsWith("@action=GroupFileList")){
                        //重新渲染顶部面板
                        renderTop();
                        //注册监听
                        registerListener();
                        //渲染文件面板
                        String[] fileList = ParseDataUtil.getFileList(fromServerData);
                        for (String filename : fileList) {
                            addToFileList(filename);
                        }
                    }
                    //文件上传
                    if (fromServerData.startsWith("@action=Upload")){
                        String res = ParseDataUtil.getUploadResult(fromServerData);
                        switch (res) {
                            case "NO":
                                JOptionPane.showMessageDialog(null, "文件已存在!");
                                break;
                            case "YES":
                                //开始上传
                                if (currentUpploadFile != null) {
                                    //开启新线程传输文件
                                    new HandelFileThread(1).start();
                                }
                                break;
                            case "上传完成":
                                JOptionPane.showMessageDialog(null, res);
                                loadGroupFile();
                                break;
                        }
                    }
                    //文件下载
                    if(fromServerData.startsWith("@action=Download")){
                        String res = ParseDataUtil.getDownResult(fromServerData);
                        if(res.equals("文件不存在")){
                            JOptionPane.showMessageDialog(null,"该文件不存在404");
                        }else {
                            String downFileName = ParseDataUtil.getDownFileName(fromServerData);
                            String downFileSize = ParseDataUtil.getDownFileSize(fromServerData);
                            //开启新线程传输文件
                            new HandelFileThread(0,downFileName,downFileSize).start();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**----------------------------------------------------------------------------------
         *     文件传输线程
         */
        class HandelFileThread extends Thread{
            private int mode;  //文件传输模式  1-上传  2-下载
            private String filename;
            private Long fileSize;

            public HandelFileThread(int mode) {
                this.mode = mode;
            }
            public HandelFileThread(int mode,String filename,String fileSize){
                this.mode = mode;
                this.filename = filename;
                this.fileSize = Long.parseLong(fileSize);
            }

            public void run() {
                try {
                    //上传文件模式
                    if(this.mode == 1){
                        Socket socket = new Socket(ip,8888);
                        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(currentUpploadFile));
                        BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());

                        int len;
                        int i = 0;
                        double sum = 0;
                        byte[] arr = new byte[8192];
                        String schedule;

                        System.out.println("开始上传--文件大小为："+currentUpploadFile.length());

                        while((len = bis.read(arr)) != -1){
                            bos.write(arr,0,len);
                            bos.flush();
                            sum += len;
                            if (i++ %100 == 0){
                                schedule = "上传进度:"+100*sum/currentUpploadFile.length()+"%";
                                System.out.println(schedule);
                            }
                        }
                        //上传完成
                        socket.shutdownOutput();
                        System.out.println("上传进度:100%\n上传完成");
                    }

                    //下载文件模式
                    if(this.mode == 0){
                        Socket socket = new Socket(ip,8888);
                        BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
                        BufferedOutputStream bos =
                                new BufferedOutputStream(new FileOutputStream(downloadSavePath+"/"+filename));

                        int len;
                        byte[] arr =new byte[8192];
                        double sumDown = 0;
                        int i = 0;

                        System.out.println("客户端开始下载 ");
                        while ((len = bis.read(arr)) != -1){
                            sumDown += len;
                            if(i++%100 == 0)
                                System.out.println("下载进度为："+100*sumDown/fileSize+"%");

                            bos.write(arr,0,len);
                            bos.flush();
                        }
                        //下载完成
                        System.out.println("下载进度:100%\n下载完成");
                        bos.close();
                        bis.close();
                        socket.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

