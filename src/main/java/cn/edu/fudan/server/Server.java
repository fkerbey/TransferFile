package cn.edu.fudan.server;

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
    private static int port;
    public static void main(String[] args) throws IOException, InterruptedException {

        InputStream inputStream = new FileInputStream("settings.properties");
        Properties p = new Properties();
        try {
            p.load(inputStream);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        port= Integer.parseInt(p.getProperty("SERVER_PORT"));
        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(5);
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        while(true){
            Socket socket = serverSocket.accept();
            fixedThreadPool.submit(new MyThread(socket));
        }
    }
}