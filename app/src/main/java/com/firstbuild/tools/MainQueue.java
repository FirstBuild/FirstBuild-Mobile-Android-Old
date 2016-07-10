package com.firstbuild.tools;

import android.os.Handler;
import android.os.Message;

import com.firstbuild.androidapp.FirstBuildApplication;

/**
 * Created by hans on 2016. 3. 28..
 */
public class MainQueue {

    private static Handler handler;

    static {
        handler = new Handler(FirstBuildApplication.getInstance().getContext().getMainLooper());
    }

    public static boolean post(Runnable r) {
        return handler.post(r);
    }

    public static boolean postDelayed(Runnable r, long delayMillis) {
        return handler.postDelayed(r, delayMillis);
    }

    public static boolean sendMessage(Message m) {
        return handler.sendMessage(m);
    }

    public static boolean sendMessageDelayed(Message m, long delayMillis) {
        return handler.sendMessageDelayed(m, delayMillis);
    }

    public static void removeCallbacks(Runnable r) {
        handler.removeCallbacks(r);
    }

    public static void removeAllMessageAndRunnable() {
        handler.removeCallbacksAndMessages(null);
    }


}
