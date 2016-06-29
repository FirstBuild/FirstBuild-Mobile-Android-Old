package com.firstbuild.tools;

/**
 * Created by hans on 16. 6. 28..
 */

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.firstbuild.commonframework.blemanager.BleManager;

/**
 * Created by hans on 2016. 3. 28..
 */
public class AppCloseObserver extends Service {

    private static final String TAG = AppCloseObserver.class.getSimpleName();

    public AppCloseObserver() {
        super();
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "AppCloseObserver" + "created");
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * This function make an app stop listening to network status
     * @param rootIntent
     */
    @Override
    public void onTaskRemoved(Intent rootIntent){
        Log.d(TAG, "AppCloseObserver" + "onTaskRemoved");

        BleManager.getInstance().closeAllActiveGattClients();
        super.onTaskRemoved(rootIntent);
    }
}
