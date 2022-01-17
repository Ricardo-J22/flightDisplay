package com.debei.flightdisplay;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.debei.utils.SharedPreferencesUtils;
import com.debei.utils.VerificationUtil;

public class ActivationActivity extends AppCompatActivity {
    private EditText code;
    private Button button_activate;
    private Button button_paste;
    private boolean isActivated = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activation);
        isActivated = VerificationUtil.checkVerification(this);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        myToolbar.setTitle("设置");
        code = findViewById(R.id.activition_id);
        button_activate = findViewById(R.id.activate);
        button_paste = findViewById(R.id.paste);
        button_paste.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isActivated) {
                    ClipboardManager mClipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    if (mClipboardManager.hasPrimaryClip() && mClipboardManager.getPrimaryClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                        ClipData.Item item = mClipboardManager.getPrimaryClip().getItemAt(0);
                        CharSequence text = item.getText();
                        if (text != null) {
                            code.setText(text);
                        }

                    }
                } else
                    Toast.makeText(ActivationActivity.this, "设备已激活，不能重复激活", Toast.LENGTH_SHORT).show();
            }

        });
        button_activate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isActivated) {
                    String content = code.getText().toString().trim();
                    if (content.equals("")) {
                        Toast.makeText(ActivationActivity.this, "激活码不能为空", Toast.LENGTH_SHORT).show();
                    }
                    isActivated = VerificationUtil.verify(content, ActivationActivity.this);
                    if (isActivated) {
                        Toast.makeText(ActivationActivity.this, "激活成功", Toast.LENGTH_SHORT).show();
                        finish();
                    } else
                        Toast.makeText(ActivationActivity.this, "激活码错误请联系管理人员", Toast.LENGTH_SHORT).show();
                    return;
                } else
                    Toast.makeText(ActivationActivity.this, "设备已激活", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        TextView android_id = findViewById(R.id.device_id);
        android_id.setText(VerificationUtil.getAndroidId(ActivationActivity.this));
        if (isActivated) {
            code.setText("已激活");
            code.setFocusable(false);
            button_paste.setVisibility(View.INVISIBLE);
        }

    }
}