package cn.edu.fudan.client;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.Date;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by dell on 2017/7/25.
 */
public class TransferThread extends java.util.TimerTask {

    public TransferThread(){

    }
    public void run() {
        System.out.println(new Date().toString() + " ------ transfer a file");
        try {
            writeFilesToServer("C:\\Users\\dell\\Desktop\\BDMS\\TransferFile\\tmp");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeFilesToServer(String path) throws IOException {
        int port = 10086;
        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(5);
        int fileNum = 0, folderNum = 0;
        File file = new File(path);
        if (file.exists()) {
            LinkedList<File> list = new LinkedList<File>();
            File[] files = file.listFiles();
            for (File file2 : files) {
                if (file2.isDirectory()) {
                    //System.out.println("文件夹:" + file2.getAbsolutePath());
                    list.add(file2);
                    folderNum++;
                } else {
                    //System.out.println("文件:" + file2.getAbsolutePath());
                    Socket socket = new Socket("localhost", 10086);//1024-65535的某个端口
                    fixedThreadPool.submit(new MyThread(socket, file2.getAbsolutePath(), (long) 0));
                    fileNum++;
                }
            }
            fixedThreadPool.shutdown();
        }
    }


}