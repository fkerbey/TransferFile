import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by lylw on 2017/7/17.
 */
public class Server {
    public static void main(String[] args) throws IOException, InterruptedException {
        int port = 10086;
        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(5);
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
            // error log
            // port already exist
            // throw exception
        }
        while(true){
            //try {
                Socket socket = serverSocket.accept();
                fixedThreadPool.submit(new MyThread(socket));

           /* } finally {
                if(!serverSocket.isClosed()){
                    System.out.println("error1");
                    serverSocket.close();
                }
                fixedThreadPool.shutdown();
                // wait to shutdown the thread pool
                while(!fixedThreadPool.isShutdown()){
                    Thread.sleep(2000);
                }
            }*/

        }

    }

    private static class MyThread extends Thread{

        private Socket socket;

        public MyThread(Socket socket){
            this.socket = socket;
        }
        /**
         * write data for communication with client
         */
        public void run(){
            // use socket
            try {
                readFromClient(socket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        public static void readFromClient(Socket socket) throws IOException {
            //3、获取输入流，并读取客户端信息
            InputStream is = socket.getInputStream();
            InputStreamReader isr =new InputStreamReader(is);
            BufferedReader br =new BufferedReader(isr);
            String info =null;
            int count=0;
            FileOutputStream out=null;
            while((info=br.readLine())!=null){
                System.out.println("Hello,我是服务器，客户端说："+info);
                String path=null;
                String base="G:/receiveFile/";
                String filePath=null;

                if(count==0){
                    path=info;
                    String[] args=path.split("\\\\");
                    String fileName=args[args.length-1];
                    filePath=base.concat(fileName);
                    System.out.println("filePath "+filePath+"\n");
                    File file=new File(filePath);
                    out = new FileOutputStream(filePath);
                    count++;
                    continue;
                }
                out.write(info.getBytes());
                count++;
            }
            out.close();
            socket.shutdownInput();//关闭输入流
            // 4、获取输出流，响应客户端的请求

            br.close();
            isr.close();
            is.close();
            socket.close();
        }
        private static boolean deleteFile(String absolutePath) {
            boolean t=false;
            File file=new File(absolutePath);
            if (file.exists() && file.isFile()) {
                if (file.delete()) {
                    System.out.println("删除单个文件" + absolutePath + "成功！");
                    t=true;
                } else {
                    System.out.println("删除单个文件" + absolutePath + "失败！");
                    t=false;
                }
            } else {
                System.out.println("删除单个文件失败：" + absolutePath + "不存在！");
                t=false;
            }
            return t;
        }
    }
}