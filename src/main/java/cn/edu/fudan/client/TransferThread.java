package cn.edu.fudan.client;

import cn.edu.fudan.Configure.ClientConfigure;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by dell on 2017/7/25.
 */
public class TransferThread extends java.util.TimerTask {
    private static Map<String,Long> filemap=new HashMap<>();

    public void run() {
        File file = new File(ClientConfigure.snapshootDirectory);
//        if(file.exists()){
//            System.out.println("Still transferring");
//            return;
//        }
        try {
            //请求文件列表，存储到 snapshootDirectory
            System.out.println(new Date().toString() + " ------ transfer files");
            writeFilesToServer(ClientConfigure.snapshootDirectory);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setMap(String file,Long bytePosition){
        filemap.put(file,bytePosition);
    }

    public static void writeFilesToServer(String path) throws IOException {
        File file = new File(path);
        File[] files = file.listFiles();
        for (File file2 : files) {
            setMap(file2.getAbsolutePath(),Long.valueOf(0));
        }

        while (file.exists() && files.length>0) {
            ExecutorService fixedThreadPool = Executors.newFixedThreadPool(ClientConfigure.clientNTread);
            files = file.listFiles();
            for (File file2 : files) {
                System.out.println(new Date().toString() + " ------ transfer a file " + file2.getName());
                Socket socket = new Socket(ClientConfigure.server_address, ClientConfigure.port);//1024-65535的某个端口
                fixedThreadPool.submit(new MyThread(socket, file2.getAbsolutePath(), filemap.get(file2.getAbsolutePath())));
            }
            fixedThreadPool.shutdown();
            int threadcount= ((ThreadPoolExecutor)fixedThreadPool).getActiveCount();
            while(threadcount!=0){
                threadcount= ((ThreadPoolExecutor)fixedThreadPool).getActiveCount();
            }

            file = new File(path);
            files = file.listFiles();
        }
    }
}