package com.debei.flightdisplay.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.debei.logic.Flight;
import com.debei.utils.Constants;
import com.debei.utils.EventMsg;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class SocketService extends Service {
    private Socket socket;
    /*连接线程*/
    private Thread connectThread;

    public static final int RECEIVE_THREAD = 0;
    public static final int SEND_THREAD = 1;
    public static final int BEAT_THREAD = 2;
    public static final int RESEND_THREAD = 3;
    public static final int COUNTDOWN_THREAD = 4;


    private Timer[] timers;
    private TimerTask[] timerTasks;
    private OutputStream outputStream;
    private InputStream inputStream;
    private SocketBinder sockerBinder = new SocketBinder();
    private String ip;
    private String port;
    private String text;
    private static int failureTimes = 0;
    private static boolean hasSendFlag = false;
    /*默认重连*/
    private boolean isReConnect = true;
    private Handler handler = new Handler(Looper.getMainLooper());


    @Override
    public IBinder onBind(Intent intent) {
        return sockerBinder;
    }

    public void startResend(final String pack) {
        if (socket != null && socket.isConnected()) {
            if (timers[SocketService.RESEND_THREAD] == null) {
                timers[SocketService.RESEND_THREAD] = new Timer();
            }
            if (timerTasks[SocketService.RESEND_THREAD] == null) {
                timerTasks[SocketService.RESEND_THREAD] = new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            outputStream = socket.getOutputStream();
                            /*这里的编码方式根据你的需求去改*/
                            outputStream.write((pack).getBytes("gbk"));
                            outputStream.flush();
                            if(failureTimes < 3){
                                failureTimes ++;
                                Log.e("TCP状态","重发了" + failureTimes);
                            }
                            else if(!hasSendFlag){
                                sendMessage(Constants.ERROR, "");
                                hasSendFlag = true;
                            }
                        } catch (Exception e) {
                            /*发送失败说明socket断开了或者出现了其他错误*/
                            toastMsg("连接断开，正在重连");
                            sendMessage(Constants.SEND_FAILURE, pack);
                            e.printStackTrace();
                        }
                    }
                };
                timers[SocketService.RESEND_THREAD].schedule(timerTasks[SocketService.RESEND_THREAD], 1000,1000);
            }
        }

    }

    public void scheduleCountdownTimer(final String pack){
        if (socket != null && socket.isConnected()) {
            if (timers[SocketService.COUNTDOWN_THREAD] == null) {
                timers[SocketService.COUNTDOWN_THREAD] = new Timer();
            }
            if (timerTasks[SocketService.COUNTDOWN_THREAD] == null) {
                timerTasks[SocketService.COUNTDOWN_THREAD] = new TimerTask() {
                    @Override
                    public void run() {
                        startResend(pack);
                    }
                };
                timers[SocketService.COUNTDOWN_THREAD].schedule(timerTasks[COUNTDOWN_THREAD], 10000);
            }
        }
    }

    public void cancelTimer() {
        for(int i = 1; i < 5; i++){
            if(timers[i] != null){
                timers[i].purge();
                timers[i].cancel();
                timers[i] = null;
            }
            if(timerTasks[i] != null){
                timerTasks[i].cancel();
                timerTasks[i] = null;
            }
        }
        resetResendTimes();
        Log.e("TCP状态", "所有计时器已重置");
    }


    public void cancelBeatTimer() {
        if (timers[SocketService.BEAT_THREAD] != null) {
            timers[SocketService.BEAT_THREAD].purge();
            timers[SocketService.BEAT_THREAD].cancel();
            timers[SocketService.BEAT_THREAD] = null;
        }
        if (timerTasks[SocketService.BEAT_THREAD] != null) {
            timerTasks[SocketService.BEAT_THREAD].cancel();
            timerTasks[SocketService.BEAT_THREAD] = null;
        }
    }


    public class SocketBinder extends Binder {

        /*返回SocketService 在需要的地方可以通过ServiceConnection获取到SocketService  */
        public SocketService getService() {
            return SocketService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        /*拿到传递过来的ip和端口号*/
        ip = intent.getStringExtra(Constants.INTENT_IP);
        port = intent.getStringExtra(Constants.INTENT_PORT);

        /*初始化socket*/
        initSocket();
        resetResendTimes();
        return super.onStartCommand(intent, flags, startId);
    }

    public void resetResendTimes() {
        failureTimes = 0;
    }


    /*初始化socket*/
    private void initSocket() {
        if (socket == null && connectThread == null) {
            timers = new Timer[5];
            timerTasks = new TimerTask[5];
            connectThread = new Thread(new Runnable() {
                @Override
                public void run() {

                    socket = new Socket();
                    try {
                        /*超时时间为2秒*/
                        socket.connect(new InetSocketAddress(ip, Integer.parseInt(port)), 2000);
                        /*连接成功的话  发送心跳包*/
                        if (socket.isConnected()) {
                            /*因为Toast是要运行在主线程的  这里是子线程  所以需要到主线程哪里去显示toast*/
                            toastMsg("socket已连接");
                            /*发送连接成功的消息*/
                            sendMessage(Constants.CONNET_SUCCESS, "");
                            /*发送心跳数据*/
//                            sendBeatData(Constants.PACKAGE_HEAD + "First-4,149" + Constants.PACKAGE_TAIL, 0);
                            recieveData();
                        }


                    } catch (IOException e) {
                        e.printStackTrace();
                        if (e instanceof SocketTimeoutException) {
                            toastMsg("连接超时，正在重连");
                            sendMessage(Constants.CONNET_FAIL, "");
                            //releaseSocket();

                        } else if (e instanceof NoRouteToHostException) {
                            toastMsg("该地址不存在，请检查");
                            sendMessage(Constants.CONNET_FAIL, "");
                            //releaseSocket();
                        } else if (e instanceof ConnectException) {
                            toastMsg("连接异常或被拒绝，请检查网络设置");
                            sendMessage(Constants.CONNET_FAIL, "");
                            //releaseSocket();
                        }


                    }

                }
            });

            /*启动连接线程*/
            connectThread.start();

        }


    }



    /*因为Toast是要运行在主线程的   所以需要到主线程哪里去显示toast*/
    private void toastMsg(final String msg) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }


    /*发送数据*/
    public void sendOrder(final String order) {
        if (socket != null && socket.isConnected()) {
            if (timers[SocketService.SEND_THREAD] == null) {
                timers[SocketService.SEND_THREAD] = new Timer();
            }

            if (timerTasks[SocketService.SEND_THREAD] == null) {
                timerTasks[SocketService.SEND_THREAD] = new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            outputStream = socket.getOutputStream();
                            /*这里的编码方式根据你的需求去改*/
                            outputStream.write((order).getBytes(StandardCharsets.UTF_8));
                            outputStream.flush();
                            sendMessage(Constants.NORMAL_SEND_SUCCESS, order);
                        } catch (Exception e) {
                            /*发送失败说明socket断开了或者出现了其他错误*/
                            toastMsg("连接断开，正在重连");
                            /*重连*/
                            sendMessage(Constants.SEND_FAILURE, order);
                            e.printStackTrace();


                        }
                    }
                };
                timers[SocketService.SEND_THREAD].schedule(timerTasks[SocketService.SEND_THREAD], 2000);
            }
        }
    }

    /*定时发送数据*/
    public void sendBeatData(String pack, int delay) {
        if (socket != null && socket.isConnected()) {
            if (timers[SocketService.BEAT_THREAD] == null) {
                timers[SocketService.BEAT_THREAD] = new Timer();
            }

            if (timerTasks[SocketService.BEAT_THREAD] == null) {
                timerTasks[SocketService.BEAT_THREAD] = new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            outputStream = socket.getOutputStream();
                            /*这里的编码方式根据你的需求去改*/
                            outputStream.write((pack).getBytes(StandardCharsets.UTF_8));
                            outputStream.flush();
                            sendMessage(Constants.NORMAL_SEND_SUCCESS, pack);
                            Log.e("TCP状态" , "成功发送心跳包");
                        } catch (Exception e) {
                            /*发送失败说明socket断开了或者出现了其他错误*/
                            toastMsg("连接断开，正在重连");
                            /*重连*/
                            sendMessage(Constants.SEND_FAILURE, pack);
                            e.printStackTrace();


                        }
                    }
                };
                timers[SocketService.BEAT_THREAD].schedule(timerTasks[SocketService.BEAT_THREAD], 30000, 30000);
            }
        }


    }


    private void sendMessage(String tag, String order) {
        EventMsg msg = new EventMsg();
        msg.setTag(tag);
        msg.setMessage(order);
        EventBus.getDefault().post(msg);
    }




    private void recieveData() {

        if (timers[SocketService.RECEIVE_THREAD] == null) {
            timers[SocketService.RECEIVE_THREAD] = new Timer();
        }

        if (timerTasks[SocketService.RECEIVE_THREAD] == null) {
            timerTasks[SocketService.RECEIVE_THREAD] = new TimerTask() {
                @Override
                public void run() {
                    try {
                        if (!socket.isClosed()) {
                            inputStream = socket.getInputStream();
                            InputStreamReader reader = new InputStreamReader(inputStream);
                            BufferedReader bufReader = new BufferedReader(reader);
                            byte[] buffer = new byte[1024];
                            int count = inputStream.read(buffer);
                            if (count != -1) {
                                text = new String(buffer, 0, count);
                                if (isVerify(text)) {
                                    //toastMsg(text + " is verified");
                                    sendMessage(Constants.RECEIVE, text);
                                }
                                    //toastMsg(text + " is not valid");

                            }

                        }
                    } catch (Exception e) {
                        /*发送失败说明socket断开了或者出现了其他错误*/
                        toastMsg("socket已断开");
                        /*重连*/
                        e.printStackTrace();
                    }
                }
            };
        }
        timers[SocketService.RECEIVE_THREAD].schedule(timerTasks[SocketService.RECEIVE_THREAD], 0, 1000);
    }





    /*释放资源*/
    public void releaseSocket() {
        for(Timer timer :timers) {
            if (timer != null) {
                timer.purge();
                timer.cancel();
                timer = null;
            }
        }
        for(TimerTask task: timerTasks) {
            if (task != null) {
                task.cancel();
                task = null;
            }
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (outputStream != null) {
            try {
                outputStream.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
            outputStream = null;
        }

        if (inputStream != null) {
            try {
                inputStream.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
            inputStream = null;
        }

        if (socket != null) {
            try {
                socket.close();

            } catch (IOException e) {
            }
            socket = null;
        }

        if (connectThread != null) {
            connectThread = null;
        }

        /*重新初始化socket*/
        if (isReConnect) {
            initSocket();
        }

    }


    public boolean isVerify(String text) {
        int index = 0;
        if(!text.startsWith(Constants.PACKET_HEAD) || !text.endsWith(Constants.PACKET_TAIL)){
            return false;
        }
        String code = text.trim().substring(text.length() - 4, text.length() - 1);
        for(char ch: code.toCharArray()){
            if(!Character.isDigit(ch)){
                return false;
            }
        }
        String info = text.trim().substring(text.indexOf("{") + 1, text.length() - 4);
        for (char ch : info.toCharArray()) {
            index += ch;
        }
        index = index % 256;
        return index == Integer.parseInt(code);
    }


    @Override
    public void onDestroy() {
        Log.e("SocketService", "onDestroy");
        isReConnect = false;
        failureTimes = 0;
        hasSendFlag = false;
        releaseSocket();
    }

}