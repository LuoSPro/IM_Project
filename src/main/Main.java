package main;

import client.GroupChatView;

public class Main {
    public static void main(String[] args) {
        new GroupChatView().create();
        //用于swing构建窗口的工作线程
        javax.swing.SwingUtilities.invokeLater(()->{

        });
    }
}
