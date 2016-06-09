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
            }
        };

        handlerDelay.postDelayed(runnable, 2000);

        ProductManager.getInstance().read();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_splash, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
