import java.io.*;
import java.net.Socket;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by lylw on 2017/7/17.
 */
public class Client {
    public static void main(String[] args) throws IOException {
        writeFilesToServer("G:\\testFile");
    }

    public static void writeFilesToServer(String path) throws IOException {
        int fileNum = 0, folderNum = 0;
        File file = new File(path);
        ExecutorService fixedThreadPool= Executors.newFixedThreadPool(3);
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
                    fixedThreadPool.execute(new Runnable() {
                        public void run(){
                            try{
                                boolean t=writeFileToServer(file2.getAbsolutePath());
                                if(t){
                                    //deleteFile(file2.getAbsolutePath());
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    fileNum++;
                }
            }
        }
        fixedThreadPool.shutdown();
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

    public static boolean writeFileToServer(String path) throws IOException {
        boolean t=true;
        //调用accept()方法开始监听，等待客户端的连接
        Socket socket = new Socket("localhost",10086);//1024-65535的某个端口

        OutputStream os = socket.getOutputStream();
        PrintWriter pw=new PrintWriter(os);
        pw.write(path+"\n");
        pw.flush();
        os.flush();
        File file=new File(path);
        FileInputStream in=new FileInputStream(file);
        byte[] buffer = new byte[4096 * 5];
        int size=0;
        while((size = in.read(buffer)) != -1){
            System.out.println("客户端发送数据包，大小为" + size);
            /**向输出流中写入刚刚读到的数据包*/
            os.write(buffer, 0, size);
            /**刷新一下*/
            os.flush();
        }

        //关闭资源
        pw.close();
        os.close();
        socket.close();
        in.close();
        return t;
    }
    public void readFromServer() throws IOException {
        //调用accept()方法开始监听，等待服务端的连接
        Socket socket = new Socket("localhost",10086);
        //获取输入流，并读取服务端端信息
        InputStream is = socket.getInputStream();
        InputStreamReader isr =new InputStreamReader(is);
        BufferedReader br =new BufferedReader(isr);
        String info =null;

        while((info=br.readLine())!=null){
            System.out.println("Hello,我是客户端，服务器说："+info);
        }
        socket.shutdownInput();//关闭输入流
        //获取输出流，响应服务器的请求
        socket.close();
        br.close();
        isr.close();
        is.close();

    }
}
