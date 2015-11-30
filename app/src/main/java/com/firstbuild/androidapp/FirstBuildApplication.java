package com.firstbuild.androidapp;

import android.app.Application;
import android.content.Context;

/**
 * Created by Hollis on 11/30/15.
 */
public class FirstBuildApplication extends Application {

    // Appliance context.
    private static Context context;


    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();

    }

    public static Context getContext() {
        return context;
    }
}
