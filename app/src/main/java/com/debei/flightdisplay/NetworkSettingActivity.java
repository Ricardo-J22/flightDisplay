package com.debei.flightdisplay;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.debei.utils.Constants;
import com.debei.utils.SharedPreferencesUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NetworkSettingActivity extends BaseActivity {
    private  SharedPreferencesUtils networkConfiguration;
    private String serverIp;
    private String port;
    private Button exitButton, connectButton;
    private EditText ipText, portText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_setting);
        readLocal();
        bindElement();
        if(serverIp != null && port != null){
            ipText.setText(serverIp);
            portText.setText(port);
        }
        bindListener();

    }

    private void bindListener() {
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ip = ipText.getText().toString().trim();
                String port = portText.getText().toString().trim();

                if (TextUtils.isEmpty(ip) || TextUtils.isEmpty(port)) {
                    Toast.makeText(NetworkSettingActivity.this, "ip和端口号不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                else{
                    String pattern = "^((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})(\\.((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})){3}$";
                    Pattern r = Pattern.compile(pattern);
                    Matcher m = r.matcher(ip);
                    if(!m.matches()){
                        Toast.makeText(NetworkSettingActivity.this, "ip地址未正确输入", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                networkConfiguration.putValues(new SharedPreferencesUtils.ContentValue("serverIp", ip));
                networkConfiguration.putValues(new SharedPreferencesUtils.ContentValue("port", port));
                Toast.makeText(NetworkSettingActivity.this, "已保存", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void bindElement() {
        ipText = findViewById(R.id.ip);
        portText = findViewById(R.id.port);
        connectButton = findViewById(R.id.btn_save);
        exitButton = findViewById(R.id.exit_network_setting);
    }

    private void readLocal() {
        networkConfiguration = new SharedPreferencesUtils(this, Constants.NETWORK_CONFIG_FILE_NAME);
        serverIp = networkConfiguration.getString(Constants.IP_KEY);
        port = networkConfiguration.getString(Constants.PORT_KEY);
    }


}