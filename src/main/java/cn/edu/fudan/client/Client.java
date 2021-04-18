package cn.edu.fudan.client;

import cn.edu.fudan.Configure.ClientConfigure;

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
    private static Long timeInterval = 10000L;
    private static Long delay_time = 0L;
    private static boolean _switch;
    private static Long startTime ; //10:00
    private static boolean changeSet=false;
    private static Timer timer;

    public static boolean isChangeSet() {
        return changeSet;
    }

    public static void setChangeSet(boolean changeSet) {
        Client.changeSet = changeSet;
    }

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

    public static void timerStart(Long delay_time, Long timeInterval){
        timer.schedule(new TransferThread(),delay_time,timeInterval);
    }

    public static void timerStop(){
        timer.cancel();
        timer.purge();
    }
    public static void main(String[] args) throws IOException {
        //读取配置文件，设置配置项
        ClientConfigure.loadProperties();
        //传送文件
        Scanner in = new Scanner(System.in);
        Thread thread1 = new Thread(new TransferThread());
        thread1.start();
        timer=new Timer();
        startTime=System.currentTimeMillis()+delay_time;
        //System.out.println("log--------------------------------------------------------------");
        //timerStart(delay_time,timeInterval);
        //timer.schedule(new TransferThread(),delay_time,timeInterval);
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
                    setChangeSet(true);

                    timer.cancel();
                    timer.purge();
                    timer = new Timer();
                    //System.out.println("log--------------------------------------------------------------");
                    //timer.schedule(new TransferThread(), delay_time, timeInterval);
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

    public void readFromServer() throws IOException {
        Socket socket = new Socket("localhost", 10086);
        InputStream is = socket.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String info = null;

        while ((info = br.readLine()) != null) {
            //System.out.println("Hello,我是客户端，服务器说：" + info);
        }
        socket.shutdownInput();
        socket.close();
        br.close();
        isr.close();
        is.close();
    }

}