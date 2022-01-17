package com.debei.flightdisplay;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.debei.utils.VerificationUtil;

public class BaseActivity extends AppCompatActivity {
    public static boolean isActivated = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        if(!isActivated){
            checkVerification();
        }


    }

    public void checkVerification(){
        isActivated = VerificationUtil.checkVerification(this);
        if(!isActivated){
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("提示")
                    .setMessage("设备未激活")
                    .setPositiveButton("前往激活", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(BaseActivity.this, ActivationActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    })
                    .setCancelable(false)
                    .create();
            dialog.show();
        }
    }
}
