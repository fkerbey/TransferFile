package cn.edu.fudan.server;

import cn.edu.fudan.Configure.ServerConfigure;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by lylw on 2017/7/17.
 */
public class Server {

    public static void main(String[] args) throws IOException, InterruptedException {
        // 读服务器端配置文件
        ServerConfigure.loadProperties();
        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(5);
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(ServerConfigure.port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        while(true){
            Socket socket = serverSocket.accept();
            fixedThreadPool.submit(new MyThread(socket));
        }
    }
}