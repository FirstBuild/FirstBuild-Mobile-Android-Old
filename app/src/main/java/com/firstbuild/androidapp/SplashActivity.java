package com.firstbuild.androidapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.firstbuild.androidapp.dashboard.DashboardActivity;
import com.firstbuild.androidapp.productmanager.ProductManager;


public class SplashActivity extends Activity {
    private final String TAG = getClass().getSimpleName();
    private Runnable runnable;
    private Handler handlerDelay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate IN");

        setContentView(R.layout.activity_splash);

        handlerDelay = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, DashboardActivity.class);
                startActivity(intent);
                finish();
            }
        };

        handlerDelay.postDelayed(runnable, 2000);

        ProductManager.getInstance().read();
    }
}
