package com.debei.flightdisplay;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.debei.flightdisplay.listview.MyAdapter;
import com.debei.flightdisplay.service.SocketService;
import com.debei.logic.Flight;
import com.debei.utils.Constants;
import com.debei.utils.EventMsg;
import com.debei.utils.SharedPreferencesUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends BaseActivity {
    private SharedPreferencesUtils networkConfiguration;
    private String serverIp, port;
    private ServiceConnection sc;
    private SocketService socketService;
    private Flight[] flights;
    private ListView listView;
    private MyAdapter myAdapter;
    private Map<String, Integer> hashMap;
    private boolean isInitial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("行李状态控制器");
        setSettingButton();
        createHashMap();
        networkConfiguration = new SharedPreferencesUtils(MainActivity.this, Constants.NETWORK_CONFIG_FILE_NAME);
        listView = findViewById(R.id.list_view);
        if (BaseActivity.isActivated) {

        }

    }

    private void initDataAndView() {
        initData();
        myAdapter = new MyAdapter(MainActivity.this, flights);
        listView.setAdapter(myAdapter);
//        flights.add(e);
//        flights.add(f);
    }

    private void initData() {
        flights = new Flight[4];
        flights[0] = new Flight("CA11111", Flight.STAGE_EP);
        flights[1] = new Flight("DH12322", Flight.STAGE_EP);
        flights[2] = new Flight("BG99999", Flight.STAGE_EP);
        flights[3] = new Flight("SD31132", Flight.STAGE_EP);
//        Flight e = new Flight("SD31132", Flight.STAGE_EP);
//        Flight f = new Flight("SD31132", Flight.STAGE_F);

    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void receive(EventMsg msg){
        if (msg.getTag().equals(Constants.RECEIVE)) {
            Log.e("TCP状态", "收到消息");
            String receive = msg.getMessage();
            boolean isValid = unpack(receive, flights);
            if (isValid) {
                for(int i = 0 ; i < 4; i++){
                    String info = receive.substring(receive.indexOf("{") + 1, receive.length() - 5);
                    String[] strings = info.split(",");
                    String[] flightInfo = strings[i].split("-");
                    flights[i].setFlight_number(flightInfo[0]);
                    if(flights[i].getStatus() == Flight.STAGE_CA){
                        flights[i].setStatus(Flight.STAGE_FEP);
                    }
                    else {
                        flights[i].setStatus(convertStatus(flightInfo[1]));
                    }
                    if(convertStatus(flightInfo[1]) == Flight.STAGE_RS){
                        flights[i].setStatus(Flight.STAGE_EP);
                    }
                }
                clearErrorFlag();

                Log.e("TCP状态","收到验证后的消息" + receive);
                Log.e("对象状态",Arrays.toString(flights));
                socketService.cancelTimer();
                //处理的业务逻辑
                String message = generatePacket(flights);
                socketService.sendBeatData(message, 300000);
            }

        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void handleMessage(EventMsg msg) {
        if (msg.getTag().equals(Constants.CONNET_SUCCESS)) {
            Log.e("TCP状态", "连接成功");
            if (isInitial) {
                socketService.sendOrder(Constants.PACKET_HEAD + "First-4,149" + Constants.PACKET_TAIL);
                isInitial = false;
            }
            String message = generatePacket(flights);
            socketService.sendOrder(message);
        } else if (msg.getTag().equals(Constants.CONNET_FAIL)) {
            socketService.releaseSocket();
            setErrorFlag();
            Log.e("TCP状态", "连接被拒绝或未找到相应地址");
        } else if (msg.getTag().equals(Constants.SEND_FAILURE)) {
            String message = msg.getMessage();
            socketService.releaseSocket();
            Log.e("TCP状态", "发送消息失败");
        } else if (msg.getTag().equals(Constants.NORMAL_SEND_SUCCESS)) {
            Log.e("TCP状态", "发送消息成功");
            String message = msg.getMessage();
            socketService.cancelTimer();
            socketService.scheduleCountdownTimer(message);
            socketService.sendBeatData(message, 30000);
            Log.e("TCP状态", "心跳包重新计时");
        } else if (msg.getTag().equals(Constants.ERROR)) {
            //设置故障标志
            setErrorFlag();
            Log.e("TCP状态", "设置故障");
        }  else if (msg.getTag().equals(Constants.REQUEST_SEND)) {
            Log.e("请求发送", Arrays.toString(flights));
            //发送消息
            String message = generatePacket(flights);
            socketService.cancelTimer();
            socketService.sendOrder(message);
            socketService.scheduleCountdownTimer(message);
            socketService.sendBeatData(message, 30000);
        }
    }

    private void clearErrorFlag() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (Flight flight : flights) {
                    flight.setErrorFlag(false);
                }
                myAdapter.notifyDataSetChanged();
            }
        });

    }

    private void setErrorFlag() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (Flight flight : flights) {
                    flight.setErrorFlag(true);
                }
                myAdapter.notifyDataSetChanged();
            }
        });
    }

    private void bindSocketService() {
        sc = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                SocketService.SocketBinder binder = (SocketService.SocketBinder) iBinder;
                socketService = binder.getService();
                Intent intent = new Intent(getApplicationContext(), SocketService.class);
                intent.putExtra(Constants.INTENT_IP, serverIp);
                intent.putExtra(Constants.INTENT_PORT, port);
                startService(intent);
                Log.e("服务", "已绑定");
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
            }
        };

        /*通过binder拿到service*/
        Intent intent = new Intent(getApplicationContext(), SocketService.class);
        intent.putExtra(Constants.INTENT_IP, serverIp);
        intent.putExtra(Constants.INTENT_PORT, port);
        bindService(intent, sc, BIND_AUTO_CREATE);

    }

    private void setSettingButton() {
        Button button_setting = findViewById(R.id.setting);
        button_setting.setOnClickListener(new View.OnClickListener() {
            long[] mHits = new long[3];

            @Override
            public void onClick(View v) {
                System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
                //获取离开机的时间
                mHits[mHits.length - 1] = SystemClock.uptimeMillis();
                //单击时间的间隔，以500毫秒为临界值
                if (mHits[0] >= (SystemClock.uptimeMillis() - 500)) {
                    Intent intent = new Intent(MainActivity.this, SettingPageActivity.class);
                    startActivity(intent);
                    mHits = null;
                    mHits = new long[3];

                }

            }
        });
    }

    /**
     * 判断服务是否运行
     */
    private boolean isServiceRunning(final String className) {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> info = activityManager.getRunningServices(Integer.MAX_VALUE);
        if (info == null || info.size() == 0) return false;
        for (ActivityManager.RunningServiceInfo aInfo : info) {
            if (className.equals(aInfo.service.getClassName())) return true;
        }
        return false;
    }

    private void stopServiceRunning() {
        if (isServiceRunning("com.debei.flightdisplay.service.SocketService")) {
            unbindService(sc);
            socketService.stopSelf();
            Log.e("socketService", "结束服务与绑定");
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        isInitial = true;
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        if (BaseActivity.isActivated) {
            serverIp = networkConfiguration.getString(Constants.IP_KEY);
            port = networkConfiguration.getString(Constants.PORT_KEY);
            initDataAndView();
            if (serverIp != null && port != null) {
                if (!isServiceRunning("com.debei.flightdisplay.service.SocketService")) {
                    bindSocketService();
                } else {
                    stopServiceRunning();
                    bindSocketService();
                }
            } else {
                Toast.makeText(MainActivity.this, "未进行网络配置或配置错误请前往设置", Toast.LENGTH_LONG).show();
            }
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterEventBus();
        stopServiceRunning();
    }

    private void unregisterEventBus() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    private String generatePacket(Flight[] flights) {
        StringBuilder builder = new StringBuilder();
        builder.append(Constants.PACKET_HEAD);
        for (Flight flight : flights) {
            builder.append(flight.getFlight_number());
            builder.append(appendCode(flight));
            builder.append(",");
        }
        int index = 0;
        String info = builder.toString().substring(6);
        char[] chars = info.toCharArray();
        for (char ch : chars) {
            index += ch;
        }
        index = index % 256;
        builder.append(String.format("%03d", index)).append(Constants.PACKET_TAIL);
        return builder.toString();
    }

    private String appendCode(Flight flight) {
        switch (flight.getStatus()) {
            case Flight.STAGE_EP:
            case Flight.STAGE_FEP:
                return "-EP";
            case Flight.STAGE_SB:
            case Flight.STAGE_BSB:
                return "-SB";
            case Flight.STAGE_FA:
                return "-FA";
            case Flight.STAGE_CA:
                return "-CA";
            case Flight.STAGE_FR:
                return "-FR";
            case Flight.STAGE_FU:
                return "-FU";
            case Flight.STAGE_LU:
                return "-LU";
            case Flight.STAGE_LA:
                return "-LA";
            case Flight.STAGE_LR:
                return "-LR";
            default:
                return "";
        }

    }

    private boolean unpack(String recieve, Flight[] flightList) {
        String info = recieve.substring(recieve.indexOf("{") + 1, recieve.length() - 5);
        String[] strings = info.split(",");
        if (strings.length != 4) {
            return false;
        }
        for (int i = 0; i < 4; i++) {
            Flight temp = flightList[i];
            int currentStatus = temp.getStatus();
            String[] flightInfo = strings[i].split("-");
            if (flightInfo.length != 2) {
                return false;
            }
            if (convertStatus(flightInfo[1]) == Flight.STAGE_RS) {
                continue;
            }
            switch (currentStatus) {
                case Flight.STAGE_SB:
                    if (!flightInfo[0].equals(temp.getFlight_number())) {
                        return false;
                    } else
                        return convertStatus(flightInfo[1]) == Flight.STAGE_SB;
                case Flight.STAGE_FA:
                case Flight.STAGE_FU:
                case Flight.STAGE_FR:
                    if (!flightInfo[0].equals(temp.getFlight_number()) || (convertStatus(flightInfo[1]) != Flight.STAGE_FU)) {
                        return false;
                    }
                    break;
                case Flight.STAGE_BSB:
                    if (!flightInfo[0].equals(temp.getFlight_number()) || (convertStatus(flightInfo[1]) != Flight.STAGE_SB)) {
                        return false;
                    }
                    break;
                case Flight.STAGE_LU:
                case Flight.STAGE_LA:
                case Flight.STAGE_LR:
                    if (!flightInfo[0].equals(temp.getFlight_number()) || (convertStatus(flightInfo[1]) != Flight.STAGE_LU)) {
                        return false;
                    }
                    break;
                case Flight.STAGE_FEP:
                case Flight.STAGE_EP:
                    if (convertStatus(flightInfo[1]) != Flight.STAGE_EP && convertStatus(flightInfo[1]) != Flight.STAGE_SB) {
                        return false;
                    }
                    break;
                case Flight.STAGE_CA:
                    if (convertStatus(flightInfo[1]) != Flight.STAGE_EP) {
                        return false;
                    }
                    break;
                default:
            }

        }


        clearErrorFlag();
        return true;


    }

    private int convertStatus(String s) {
        int x = hashMap.getOrDefault(s, -1);
        return x;


    }

    private void createHashMap() {
        if (hashMap == null) {
            hashMap = new HashMap<>();
            hashMap.put("EP", Flight.STAGE_EP);
            hashMap.put("SB", Flight.STAGE_SB);
            hashMap.put("FV", Flight.STAGE_FU);
            hashMap.put("LV", Flight.STAGE_LU);
            hashMap.put("FU", Flight.STAGE_FU);
            hashMap.put("LU", Flight.STAGE_LU);
            hashMap.put("RS", Flight.STAGE_RS);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterEventBus();
        stopServiceRunning();
    }
}