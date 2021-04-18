package cn.edu.fudan.client;

import cn.edu.fudan.Configure.ClientConfigure;

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
        try {
            //如果文件夹中已经存在文件，则不向tsfiledb请求文件列表，不继续执行
            writeFilesToServer(ClientConfigure.snapshootDirectory);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeFilesToServer(String path) throws IOException {
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
                    System.out.println(new Date().toString() + " ------ transfer a file " + file2.getName());
                    Socket socket = new Socket(ClientConfigure.server_address, ClientConfigure.port);//1024-65535的某个端口
                    fixedThreadPool.submit(new MyThread(socket, file2.getAbsolutePath(), (long) 0));
                    fileNum++;
                }
            }
            fixedThreadPool.shutdown();
        }
    }

}