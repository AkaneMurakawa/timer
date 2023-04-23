package com.github.timer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;

/**
 * 计时器
 *
 * @author Akane Murakawa
 * @date 2017-8-22
 */
public class Timer extends JFrame implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 界面时间初始显示标签
     */
    private static final String DEFAULT_LABEL = "00:00:00 000";
    /**
     * 记录开始时间
     */
    private long startTime = System.currentTimeMillis();
    /**
     * 停止时间，程序开始是停止的
     */
    private long pauseTime = startTime;
    /**
     * 暂停时消耗时间，程序停止的总时间
     */
    private long pauseCount = 0;
    /**
     * 获取想要多少分钟倒计时，单位
     */
    private int countDownSeconds = 0;
    /**
     * 计数线程类，线程：thread
     */
    private CountingThread countingThread = new CountingThread();
    /**
     * 定义需要的组件
     */
    private JLabel timeJl;
    private JButton countUpJb;
    private JButton resetJb;
    private JButton countDownJb;
    private JPanel timeJp;
    private JPanel buttonJp;
    private JTextField countDownJtf;

    /**
     * 计数线程类
     */
    class CountingThread extends Thread {
        /**
         * 判断时间是否停止，一開始的時候是停止的
         */
        private boolean stopped = true;
        /**
         * 计时模式：0表示正计时，1表示倒计时
         */
        private int timeMode;
        /**
         * 0表示正计时
         */
        private static final int COUNT_UP = 0;
        /**
         * 1表示倒计时
         */
        private static final int COUNT_DOWN = 1;
        /**
         * 用于多少秒倒计时，初始化為0
         */
        private long timeLongSeconds = 0;

        CountingThread() {
            // 守护线程 daemon:n. 妖魔鬼怪; 朋友; 某协议的伺服机 (计算机用语)
            setDaemon(true);
        }

        @Override
        public void run() {
            while (true) {
                // 获取输入的字符串，注意需加"0"会不然会出错，因为传入的字符不能为0
                countDownSeconds = Integer.parseInt("0" + countDownJtf.getText());

                // 正计时
                if (COUNT_UP == timeMode && !stopped) {
                    // 获取消耗的时间=当前时间-开始时间-暂停消耗的时间
                    long pass = System.currentTimeMillis() - startTime - pauseCount;
                    timeJl.setText(timeFormat(pass));
                }
                // 倒计时
                if (COUNT_DOWN == timeMode && !stopped) {
                    // 获取消耗的时间=设置的倒计时-正计时时间
                    long pass = timeLongSeconds - (System.currentTimeMillis() - startTime - pauseCount);
                    timeJl.setText(timeFormat(pass));
                }

                try {
                    // 1毫秒更新一次显示
                    sleep(1);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            }
        }

        /**
         * 时间格式化：时：分：秒 毫秒
         * 例如：00：00：00 000
         */
        private String timeFormat(long pass) {
            int hour;
            int minute;
            int second;
            int milli;
            milli = (int) (pass % 1000);           // pass为消耗的时间，毫秒单位。转换成多少秒先
            pass = pass / 1000;                    // 此时pass就是一共多少秒
            second = (int) (pass % 60);            // 秒转换成分，余数就是多少秒
            pass = pass / 60;                      // 此时pass就是多少分
            minute = (int) (pass % 60);            // 分转换成小时，余数就是多少分
            pass = pass / 60;
            hour = (int) (pass % 60);
            return String.format("%02d:%02d:%02d %-4d", hour, minute, second, milli);
        }
    }

    /**
     * 事件1，正计时
     */
    private ActionListener countUpButtonListener = (ActionEvent e) -> {
        if (!countDownJb.getText().equals("倒计时")) {
            reset();
        }

        countingThread.timeMode = CountingThread.COUNT_UP;
        // 点击开始/点击继续 ——> 显示暂停
        if (countingThread.stopped) {
            // 当点击继续时，计算由暂停——>继续的时间
            pauseCount += (System.currentTimeMillis() - pauseTime);
            countingThread.stopped = false;
            countUpJb.setText("暂停");
        }
        // 点击暂停 ——> 显示继续
        else {
            // 暂停，那停止时间就是当前时间
            pauseTime = System.currentTimeMillis();
            countingThread.stopped = true;
            countUpJb.setText("继续");
        }
    };

    /**
     * 事件2，倒计时
     */
    private ActionListener countDownButtonListener = (ActionEvent e) -> {
        if (!countUpJb.getText().equals("开始")) {
            reset();
        }

        countingThread.timeMode = CountingThread.COUNT_DOWN;
        countingThread.timeLongSeconds = countDownSeconds * 1000L;
        // 点击开始/点击继续 ——> 显示暂停
        if (countingThread.stopped) {
            // 当点击继续时，计算由暂停——>继续的时间
            pauseCount += (System.currentTimeMillis() - pauseTime);
            countingThread.stopped = false;
            countDownJb.setText("暂停");
        }
        // 点击暂停 ——> 显示继续
        else {
            // 暂停，那停止时间就是当前时间，一直在计时
            pauseTime = System.currentTimeMillis();
            countingThread.stopped = true;
            countDownJb.setText("继续");
        }
    };

    /**
     * 事件3，清除
     */
    private ActionListener resetButtonListener = (ActionEvent e) -> reset();

    private void reset() {
        pauseTime = startTime;
        pauseCount = 0;
        countingThread.stopped = true;
        timeJl.setText(DEFAULT_LABEL);
        countUpJb.setText("开始");
        resetJb.setText("清除");
        countDownJb.setText("倒计时");
    }

    /**
     * 初始化
     */
    Timer(String title) throws HeadlessException {
        // 创建组件
        countUpJb = new JButton("开始");
        countUpJb.setBackground(Color.WHITE);
        resetJb = new JButton("清除");
        countDownJb = new JButton("倒计时");

        timeJl = new JLabel(DEFAULT_LABEL);
        timeJl.setFont(new Font("微软雅黑", timeJl.getFont().getStyle(), 40));
        timeJp = new JPanel();
        timeJp.add(timeJl);
        timeJp.setBackground(Color.white);
        buttonJp = new JPanel();
        buttonJp.setBackground(Color.white);

        countDownJtf = new JTextField("000");
        countDownJtf.setBackground(Color.decode("#e1e1e1"));
        buttonJp.add(countDownJtf);
        buttonJp.add(countUpJb);
        buttonJp.add(resetJb);
        buttonJp.add(countDownJb);
        this.add(buttonJp, BorderLayout.SOUTH);
        this.add(timeJp, BorderLayout.CENTER);

        //this.pack();// 自适应大小
        this.setSize(300, 140);
        this.setTitle(title);
        this.setLocationRelativeTo(null);
        this.setIconImage(new ImageIcon("images/icon.jpg").getImage());
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);

        // 注册监听事件
        countUpJb.addActionListener(countUpButtonListener);
        resetJb.addActionListener(resetButtonListener);
        countDownJb.addActionListener(countDownButtonListener);

        // 启动计数线程
        countingThread.start();
    }

    public static void main(String[] args) {
        try {
            // 设置窗体风格
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        new Timer("Timer");
    }
}
