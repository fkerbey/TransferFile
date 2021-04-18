package cn.edu.fudan.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Scanner;
import java.util.Timer;
/**
 * Created by lylw on 2017/7/17.
 */
public class Client {

    private static Long timeInterval = 60000L;
    private static Long startPoint = 5000L;
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
        Scanner in = new Scanner(System.in);
        Timer timer=new Timer();
        timer.schedule(new TransferThread(),startPoint,timeInterval);

        while (true) {
            try {
                System.out.print("input a command: ");
                String cmd = in.nextLine();
                if (cmd.equals("set")) {
                    System.out.print("input start point: ");
                    startPoint = in.nextLong();
                    System.out.print("input time interval: ");
                    timeInterval = in.nextLong();
                    timer.cancel();
                    timer.purge();
                    timer = new Timer();
                    timer.schedule(new TransferThread(), startPoint, timeInterval);
                } else if (cmd.equals("transfer")) {
                    Thread thread = new Thread(new TransferThread());
                    thread.start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
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