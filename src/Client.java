import java.io.*;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
/**
 * Created by lylw on 2017/7/17.
 */
public class Client {

    private static Long timeInterval;
    private static Long startPoint;
    private static boolean _switch;

    public static void setTimeInterval(Long timeInterval) {
        Client.timeInterval = timeInterval;
    }

    public static void setStartPoint(Long startPoint) {
        Client.startPoint = startPoint;
    }

    public static void set_switch(boolean _switch) {
        Client._switch = _switch;
    }

    public static void main(String[] args) throws IOException {
        SettingThread settingThread=new SettingThread();
        settingThread.start();
        TimingThread timingThread=new TimingThread(Long.valueOf(0),Long.valueOf(0),false);
        timingThread.start();
        TransferThread transferThread=new TransferThread();
        transferThread.run();
    }
    private static class TimingThread extends Thread{
        public TimingThread(Long startPoint,Long timeInterval,boolean _switch){
            Client.startPoint=startPoint;
            Client.timeInterval=timeInterval;
            Client._switch=_switch;
        }
        public void run(){
            Timer timer=new Timer();
            timer.schedule(new TransferThread(),1000L,60000L);
        }
    }
    private static class SettingThread extends Thread{
        public void run(){
            while(true){
                Scanner in = new Scanner(System.in);
                String input=in.nextLine();
            }
        }
    }
    private static class TransferThread extends java.util.TimerTask{
        public TransferThread(){

        }
        public void run() {
            try {
                writeFilesToServer("G:\\testFile");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private static class MyThread extends Thread {
        private Socket socket;
        private String absolutePath;
        private String MD5;
        private Long bytePosition;
        public MyThread(Socket socket, String absolutePath,Long bytePosition) {
            this.socket = socket;
            this.absolutePath = absolutePath.substring(0, absolutePath.length());
            this.bytePosition=bytePosition;
        }

        public void run() {
            try {
                sendFileNameAndLength(absolutePath);
                InputStream ins = socket.getInputStream();
                byte[] input = new byte[1024];
                ins.read(input);
                boolean t = writeFileToServer(absolutePath);
                System.out.println("finish send file\n");
                ins.read(input);
                System.out.println(MD5+" "+new String(input)+"\n");
                if (t) {
                    //deleteFile(file2.getAbsolutePath());
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }

        private boolean sendFileNameAndLength(String absolutePath) throws IOException {
            boolean t = true;
            File file = new File(absolutePath);
            OutputStream os = socket.getOutputStream();
            PrintWriter pw = new PrintWriter(os);
            pw.write(absolutePath + " " + file.length() + " "+bytePosition+" ");
            pw.flush();
            os.flush();
            return t;
        }

        private boolean writeFileToServer(String absolutePath) throws IOException, NoSuchAlgorithmException {
            boolean t = true;
            MD5=Md5CaculateUtil.getFileMD5(absolutePath);
            OutputStream os = socket.getOutputStream();
            File file = new File(absolutePath);
            FileInputStream in = new FileInputStream(file);
            byte[] buffer = new byte[16];
            int size = 0;
            bytePosition= Long.valueOf(0);
            while ((size = in.read(buffer)) != -1) {
                System.out.println("客户端发送数据包，大小为" + size);
                os.write(buffer, 0, size);
                os.flush();
                InputStream ins = socket.getInputStream();
                byte[] readAccept=new byte[16];
                ins.read(readAccept);
                String temp=new String(readAccept);
                System.out.println("temp "+temp.split("\n")[0]);
                bytePosition+=Long.parseLong(temp.split("\n")[0]);
                System.out.println("bytePosition "+bytePosition);
            }
            //os.close();
            //socket.close();
            //in.close();
            return t;
        }
        private static boolean deleteFile(String absolutePath) {
            boolean t = false;
            File file = new File(absolutePath);
            if (file.exists() && file.isFile()) {
                if (file.delete()) {
                    System.out.println("删除单个文件" + absolutePath + "成功！");
                    t = true;
                } else {
                    System.out.println("删除单个文件" + absolutePath + "失败！");
                    t = false;
                }
            } else {
                System.out.println("删除单个文件失败：" + absolutePath + "不存在！");
                t = false;
            }
            return t;
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
                    System.out.println("文件夹:" + file2.getAbsolutePath());
                    list.add(file2);
                    folderNum++;
                } else {
                    System.out.println("文件:" + file2.getAbsolutePath());
                    Socket socket = new Socket("localhost", 10086);//1024-65535的某个端口
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