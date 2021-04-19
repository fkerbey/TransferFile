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
        byte[] buffer = new byte[1024];
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
        ous.close();
    }
}