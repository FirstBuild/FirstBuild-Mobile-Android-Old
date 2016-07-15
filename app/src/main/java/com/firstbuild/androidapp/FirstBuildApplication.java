package com.firstbuild.androidapp;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import com.firstbuild.tools.AppCloseObserver;

/**
 * Created by Hollis on 11/30/15.
 */
public class FirstBuildApplication extends Application {

    // Appliance context.
    private static Context context;
    private static FirstBuildApplication instance;

    private String appVersionName;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        instance = this;

        startService(new Intent(getBaseContext(), AppCloseObserver.class));

        try {
            appVersionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static FirstBuildApplication getInstance() {
        return instance;
    }

    public String getAppVersion() {
        return appVersionName;
    }

    public Context getContext() {
        return context;
    }
}
