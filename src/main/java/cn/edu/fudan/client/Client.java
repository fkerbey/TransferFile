package cn.edu.fudan.client;

import java.io.*;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Scanner;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by lylw on 2017/7/17.
 */
public class Client {

    private static Long timeInterval = 10000L;
    private static Long delay_time = 0L;
    private static boolean _switch;
    private static Long startTime ; //10:00
    private static int port;
    private static String server_address;
    private static String snapshootDirectory;

    public static Long getTimeInterval() {
        return timeInterval;
    }

    public static void setTimeInterval(Long timeInterval) {
        Client.timeInterval = timeInterval;
    }

    public static Long getDelay_time() {
        return delay_time;
    }

    public static void setDelay_time(Long delay_time) {
        Client.delay_time = delay_time;
    }

    public static boolean is_switch() {
        return _switch;
    }

    public static void set_switch(boolean _switch) {
        Client._switch = _switch;
    }

    public static Long getStartTime() {
        return startTime;
    }

    public static void setStartTime(Long startTime) {
        Client.startTime = startTime;
    }

    public static int getPort() {
        return port;
    }

    public static void setPort(int port) {
        Client.port = port;
    }

    public static String getServer_address() {
        return server_address;
    }

    public static void setServer_address(String server_address) {
        Client.server_address = server_address;
    }

    public static String getSnapshootDirectory() {
        return snapshootDirectory;
    }

    public static void setSnapshootDirectory(String snapshootDirectory) {
        Client.snapshootDirectory = snapshootDirectory;
    }

    public static void main(String[] args) throws IOException {
        //读取配置文件，设置配置项
        InputStream inputStream = new FileInputStream("settings.properties");
        Properties p = new Properties();
        try {
            p.load(inputStream);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        port=Integer.parseInt(p.getProperty("CLIENT_PORT"));
        server_address=p.getProperty("SERVER_ADDRESS");
        snapshootDirectory=p.getProperty("SNAPSHOOT_DIRECTORY");
        //传送文件
        Scanner in = new Scanner(System.in);
        Timer timer=new Timer();
        startTime=System.currentTimeMillis()+delay_time;
        //System.out.println("log--------------------------------------------------------------");
        timer.schedule(new TransferThread(),delay_time,timeInterval);
        //System.out.println("-----------------------------------------------------------------");
        while (true) {
            try {
                System.out.print("input a command:\n");
                String cmd = in.nextLine();
                if (cmd.equals("set")) {
                    System.out.print("input delay time: ");
                    delay_time = in.nextLong();
                    startTime=System.currentTimeMillis()+delay_time;
                    System.out.print("input time interval: ");
                    timeInterval = in.nextLong();
                    timer.cancel();
                    timer.purge();
                    timer = new Timer();
                    //System.out.println("log--------------------------------------------------------------");
                    timer.schedule(new TransferThread(), delay_time, timeInterval);
                    //System.out.println("-----------------------------------------------------------------");
                } else if (cmd.equals("transfer now")) {
                    //System.out.println("log--------------------------------------------------------------");
                    Thread thread = new Thread(new TransferThread());
                    thread.start();
                    //System.out.println("-----------------------------------------------------------------");
                }
                else if(cmd.equals("switch")){
                    System.out.print("set on(1) or off(0):");
                    int getbool=in.nextInt();
                    _switch=(getbool==0)?false:true;
                    if(_switch){
                        //触发timer的schedule
                        timer.cancel();
                        timer.purge();
                        Long nowtime=System.currentTimeMillis();
                        while(startTime<nowtime){
                            startTime+=timeInterval;
                        }
                        //startTime=System.currentTimeMillis()+delay_time;
                        //System.out.println("log--------------------------------------------------------------");
                        delay_time=startTime-System.currentTimeMillis();
                        timer=new Timer();
                        timer.schedule(new TransferThread(),delay_time,timeInterval);
                        //System.out.println("-----------------------------------------------------------------");
                    }
                    else if(!_switch){
                        timer.cancel();
                        timer.purge();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private static class TransferThread extends java.util.TimerTask{
        public TransferThread(){

        }
        public void run() {
            System.out.println(System.currentTimeMillis() + "  transfer files");
            try {
                //如果文件夹中已经存在文件，则不向tsfiledb请求文件列表，不继续执行
                writeFilesToServer(snapshootDirectory);
            } catch (IOException e) {
                e.printStackTrace();
            }
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
                    Socket socket = new Socket(server_address, port);//1024-65535的某个端口
                    fixedThreadPool.submit(new MyThread(socket, file2.getAbsolutePath(), (long) 0));
                    fileNum++;
                }
            }
            fixedThreadPool.shutdown();
        }
    }

    public void readFromServer() throws IOException {
        Socket socket = new Socket("localhost", 10086);
        InputStream is = socket.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String info = null;

        while ((info = br.readLine()) != null) {
            System.out.println("Hello,我是客户端，服务器说：" + info);
        }
        socket.shutdownInput();
        socket.close();
        br.close();
        isr.close();
        is.close();
    }

}