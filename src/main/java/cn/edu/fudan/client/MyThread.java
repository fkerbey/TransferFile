package cn.edu.fudan.client;

import cn.edu.fudan.common.Md5CaculateUtil;

import java.io.*;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;

/**
 * Created by dell on 2017/7/24.
 */
public class MyThread extends Thread {
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
        MD5= Md5CaculateUtil.getFileMD5(absolutePath);
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