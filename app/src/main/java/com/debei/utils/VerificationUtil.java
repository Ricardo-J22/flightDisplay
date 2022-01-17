package com.debei.utils;

import android.content.Context;
import android.provider.Settings;

public class VerificationUtil {

    public static boolean checkVerification(Context context){
        SharedPreferencesUtils status = new SharedPreferencesUtils(context, Constants.ACTIVATED_STATUS_FILE_NAME);
        if (status.getString(Constants.IS_ACTIVATED_KEY) == null || !status.getString(Constants.IS_ACTIVATED_KEY).equals("true")) {
            return false;
        }
        return true;
    }

    public static String getAndroidId(Context context) {
        String ANDROID_ID = Settings.System.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        return ANDROID_ID;
    }

    public static boolean verify(String content, Context context){
        String x = "";
        String id = getAndroidId(context);
        try {
            x = AESUtils.aesDecrypt(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(id.equals(x)){
            SharedPreferencesUtils status = new SharedPreferencesUtils(context, Constants.ACTIVATED_STATUS_FILE_NAME);
            status.putValues(new SharedPreferencesUtils.ContentValue(Constants.IS_ACTIVATED_KEY, "true"));
        }
        return id.equals(x);
    }
}
