package com.debei.flightdisplay;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class SettingPageActivity extends BaseActivity {

    private Button network;
    private Button activation;
    private Button home;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_page);
        network = findViewById(R.id.jumpToNetwork);
        activation = findViewById(R.id.jumpToActivation);
        network.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), NetworkSettingActivity.class);
                startActivity(intent);
            }
        });
        activation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingPageActivity.this, ActivationActivity.class);
                startActivity(intent);
            }
        });
        home = findViewById(R.id.jumpToHome);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}