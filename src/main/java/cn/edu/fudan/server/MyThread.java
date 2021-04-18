package cn.edu.fudan.server;

import cn.edu.fudan.Configure.ServerConfigure;
import cn.edu.fudan.common.Md5CaculateUtil;

import java.io.*;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;

/**
 * Created by dell on 2017/7/24.
 */
public class MyThread extends Thread {
    private Socket socket;
    private String receive_filePath;
    private int fileSize;

    public MyThread(Socket socket){
        this.socket = socket;
    }
    /**
     * write data for communication with main.java.cn.edu.fudan.client
     */
    public void run(){
        try {
            read(socket);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
    private void read(Socket socket) throws IOException, NoSuchAlgorithmException {
        InputStream is = socket.getInputStream();
        InputStreamReader isr =new InputStreamReader(is);
        BufferedReader br =new BufferedReader(isr);
        byte[] buffer = new byte[128];
        is.read(buffer);

        String info =new String(buffer);
        String[] args=info.split(" ");

        String path = null;
        path = args[0];
        fileSize=Integer.parseInt(args[1].substring(0,args[1].length()));
        String[] args1 = path.split("\\\\");
        String fileName = args1[args1.length - 1];

        String temp= ServerConfigure.storage_directory.concat(fileName);
        receive_filePath=temp.substring(0,temp.length());
        Long startPosition= Long.parseLong(args[2]);
        //System.out.println("startPosition");
        File receive_file=new File(receive_filePath);
        if(!receive_file.exists()){
            receive_file.createNewFile();
        }
        FileInputStream fis=new FileInputStream(receive_file);
        File temp_file=new File(ServerConfigure.storage_directory+"temp_"+fileName);
        FileOutputStream fos= new FileOutputStream(temp_file);

        byte[] copyfile=new byte[16];

        int read=0;
        int total_read=0;
        while(total_read<startPosition){
            read=fis.read(copyfile);
            fos.write(copyfile);
            total_read+=read;
        }
        fos.close();
        fis.close();
        int tempsize=0;
        fis=new FileInputStream(temp_file);
        fos=new FileOutputStream(receive_file);
        while(tempsize<startPosition){
            tempsize+=fis.read(copyfile);
            fos.write(copyfile);
        }
        fis.close();
        if(!temp_file.delete()){
            System.out.println("delete file "+temp_file.getAbsoluteFile()+" fail");
        }
        OutputStream ous=socket.getOutputStream();
        PrintWriter pw=new PrintWriter(ous);
        pw.write("ok\n");
        pw.flush();
        ous.flush();

        int receive_size=0;
        int readSize=0;
        while((receive_size<fileSize) && ((readSize=is.read(buffer))!=-1)){
            receive_size+=readSize;
            System.out.println("receive_size "+receive_size);
            fos.write(buffer,0,readSize);
            pw.write(readSize+"\n");
            pw.flush();
            ous.flush();
        }
        String md5= Md5CaculateUtil.getFileMD5(receive_filePath);
        pw.write(md5+"\n");
        pw.flush();
        ous.flush();
        fos.close();
        br.close();
        isr.close();
        is.close();
    }

    private boolean readFileNameAndSize(Socket socket) throws IOException {
        boolean t=true;
        InputStream is = socket.getInputStream();
        InputStreamReader isr =new InputStreamReader(is);
        BufferedReader br =new BufferedReader(isr);
        String info =br.readLine();
        String[] args=info.split(" ");
        String path = null;
        String base = "G:/receiveFile/";
        path = args[0];
        fileSize=95;
        String[] args1 = path.split("\\\\");
        String fileName = args1[args1.length - 1];
        //System.out.println("fileName "+fileName);
        String temp= base.concat(fileName);
        receive_filePath=temp.substring(0,temp.length());
        //System.out.println("Hello,我是服务器，客户端说：" + info.split(" ")[0]+"\n");
        System.out.println("filePath " + receive_filePath + "\n");
        br.close();
        isr.close();
        //is.close();
        return t;
    }

    public void readFromClient(Socket socket) throws IOException {
        InputStream is = socket.getInputStream();
        System.out.println("receice_filePath "+receive_filePath+" InputStream Size "+is.available());
        InputStreamReader isr =new InputStreamReader(is);
        BufferedReader br =new BufferedReader(isr);
        FileOutputStream out=new FileOutputStream(receive_filePath);
        //byte[] buffer = new byte[4096 * 5];
        byte[] buffer = new byte[128];
        int receive_size=0;
        int readSize=0;
        DataInputStream dis = new DataInputStream(new BufferedInputStream(is));
        while((readSize=dis.read(buffer))!=-1){
            receive_size+=readSize;
            out.write(buffer,0,readSize);
        }
        out.close();
        socket.shutdownInput();
        br.close();
        isr.close();
        //is.close();
        //socket.close();
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