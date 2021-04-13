import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
/**
 * Created by lylw on 2017/7/17.
 */
public class Server {
    public static void main(String[] args) throws IOException {
        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(3);
        int count=0;
        //1、创建一个服务器端Socket，即ServerSocket，指定绑定的端口，并监听此端口
        ServerSocket serverSocket =new ServerSocket(10086);//1024-65535的某个端口
        while(true){
            fixedThreadPool.execute(new Runnable() {
                public void run() {
                    try {
                        readFromClient(serverSocket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
    public static void writeFilesToClient(String path) throws IOException {
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
                    boolean t=writeFileToClient(file2.getAbsolutePath());
                    if(t){
                        deleteFile(file2.getAbsolutePath());
                    }
                    fileNum++;
                }
            }
        }
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

    public static boolean writeFileToClient(String path) throws IOException {
        /**
         * 基于TCP协议的Socket通信，实现用户登录，服务端
         */
        boolean t=true;
        //1、创建一个服务器端Socket，即ServerSocket，指定绑定的端口，并监听此端口
        ServerSocket serverSocket =new ServerSocket(10086);//1024-65535的某个端口
        //2、调用accept()方法开始监听，等待客户端的连接
        Socket socket = serverSocket.accept();

        OutputStream os = socket.getOutputStream();
        File file=new File(path);
        FileInputStream in=new FileInputStream(file);
        byte[] buffer = new byte[4096 * 5];
        int size=0;
        while((size = in.read(buffer)) != -1){
            System.out.println("服务器发送数据包，大小为" + size);
            /**向输出流中写入刚刚读到的数据包*/
            os.write(buffer, 0, size);
            /**刷新一下*/
            os.flush();
        }


//5、关闭资源
        os.close();
        socket.close();
        serverSocket.close();

        return t;
    }
    public static void readFromClient(ServerSocket serverSocket) throws IOException {

        //2、调用accept()方法开始监听，等待客户端的连接
        Socket socket = serverSocket.accept();
        //3、获取输入流，并读取客户端信息
        InputStream is = socket.getInputStream();
        InputStreamReader isr =new InputStreamReader(is);
        BufferedReader br =new BufferedReader(isr);
        String info =null;

        while((info=br.readLine())!=null){
            System.out.println("Hello,我是服务器，客户端说："+info);
        }
        socket.shutdownInput();//关闭输入流
        // 4、获取输出流，响应客户端的请求

        br.close();
        isr.close();
        is.close();
        socket.close();
    }
}
