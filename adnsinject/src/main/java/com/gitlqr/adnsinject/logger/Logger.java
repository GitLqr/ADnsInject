package com.gitlqr.adnsinject.logger;

import android.util.Log;

public class Logger {

    private static String TAG = "adnsinject";
    private static boolean isEnable = false;

    private Logger() {
    }

    public static void setEnable(boolean enable) {
        isEnable = enable;
    }

    public static void d(String msg) {
        Log.d(TAG, msg);
    }
}
