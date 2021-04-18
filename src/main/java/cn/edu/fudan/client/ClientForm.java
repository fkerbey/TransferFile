package cn.edu.fudan.client;

import cn.edu.fudan.Configure.ClientConfigure;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Timer;

/**
 * Created by dell on 2017/7/24.
 */
public class ClientForm extends javax.swing.JFrame {

    private JPanel panel1;
    private JTextArea textArea1;
    private JButton startButton;
    private JButton offButton;
    private JButton transferButton;
    private JTextField startTimeTextField;
    private JTextField intervalTextField;
    private JButton submitButton;

    private static Long timeInterval = 60000L;
    private static Long startPoint = 0L;
    java.util.Timer timer = new java.util.Timer();

    public ClientForm() {
        offButton.setEnabled(false);
        System.setOut(new GUIPrintStream(System.out, textArea1));
        // manual transfer file
        transferButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Thread thread = new Thread(new TransferThread());
                thread.start();
            }
        });
        // start timing transfer
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                timer.cancel();
                timer.purge();
                timer = new java.util.Timer();
                timer.schedule(new TransferThread(),startPoint,timeInterval);
                System.out.println(new Date().toString() + " ------ start a new timing transfer, " +
                        "delay is " + (startPoint/1000) + " s, "+
                        "period is " + (timeInterval/1000) + " s");
                startButton.setEnabled(false);
                offButton.setEnabled(true);
            }
        });

        // shutdown timing transfer
        offButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                timer.cancel();
                timer.purge();
                System.out.println(new Date().toString() + " ------ end a timing transfer\n");
                offButton.setEnabled(false);
                startButton.setEnabled(true);
            }
        });
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String startTimeStr = startTimeTextField.getText();
                String intervalStr = intervalTextField.getText();
                try {
                    SimpleDateFormat sdf =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date time = sdf.parse(startTimeStr);
                    Long cachedTimeInterval = Long.valueOf(intervalStr);
                    if (time.before(new Date())) {
                        System.out.println(new Date().toString() + " ------ Err: Start time should be later than current time!");
                    } else {
                        startPoint = time.getTime() - new Date().getTime();
                        timeInterval = cachedTimeInterval;
                        System.out.println(new Date().toString() + " ------ Success: update the parameter of timing task!");
                        if (!startButton.isEnabled()) {
                            // if timing task is running
                            timer.cancel();
                            timer.purge();
                            System.out.println(new Date().toString() + " ------ start a new timing transfer, " +
                                    "delay is " + (startPoint/1000) + " s, "+
                                    "period is " + (timeInterval/1000) + " s");
                            timer = new Timer();
                            timer.schedule(new TransferThread(), startPoint, timeInterval);
                        }
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                    System.out.println(new Date().toString() + " ------ " + exception.getMessage());
                }
            }
        });
    }


    public static void main(String[] args) throws FileNotFoundException {
        //读取配置文件，设置配置项
        ClientConfigure.loadProperties();
        JFrame frame = new JFrame("ClientForm");
        frame.setContentPane(new ClientForm().panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
