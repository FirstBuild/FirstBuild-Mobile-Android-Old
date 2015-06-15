/**
 * @file BleManager.java
 * @brief BleManager handles all kind of interface with ble devices
 * @author Ryan Lee - 320006284
 * @date May/22/2015
 * Copyright (c) 2014 General Electric Corporation - Confidential - All rights reserved.
 */

package com.firstbuild.commonframework.bleManager;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.firstbuild.commonframework.deviceManager.DeviceManager;
import com.firstbuild.commonframework.deviceManager.Paragon;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BleManager {

    private final String TAG = getClass().getSimpleName();

    //    private final String TARGET_DEVICE_NAME = "BLE ACM";
    private final String TARGET_DEVICE_NAME = "CC2650 SensorTag";

    // Set enable bluetooth feature
    private static final int REQUEST_ENABLE_BT = 1;

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    // Bluetooth adapter handler
    private BluetoothAdapter bluetoothAdapter = null;

    private BluetoothLeService bluetoothLeService = null;

    private BluetoothManager bluetoothManager = null;

    // Flag for checking device scanning state
    public boolean isScanning = false;

    // Flag for checking ble connection established
    public boolean isConnected = false;

    // Post Delayed handler
    private Handler handler = new Handler();

    private String deviceAddress = null;

    private Context context = null;

    public static BleManager instance = new BleManager();

    public static BleManager getInstance(){
        return instance;
    }

    public BleManager(){
        // Default constructor
    }

    public void initBleManager(Context context){
        Log.d(TAG, "initBleManager IN");

        this.context = context;
        bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        Intent gattServiceIntent = new Intent(context, BluetoothLeService.class);
        context.bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            bluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!bluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
//                finish();
                // TODO:  Notify end service
            }

            // Automatically connects to the device upon successful start-up initialization.
            if (deviceAddress != null) {
                bluetoothLeService.connect(deviceAddress);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bluetoothLeService = null;
        }
    };

    /**
     * Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
     * fire an intent to display a dialog asking the user to grant permission to enable it.
     */
    public void enableBluetoothAndStartScan(){
        Log.d(TAG, "enableBluetoothAndStartScan IN");

        if (!bluetoothAdapter.isEnabled()) {
            Log.d(TAG, "Bluetooth adapter is disabled. Enable bluetooth adapter.");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            ((Activity) context).startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        else {
            Log.d(TAG, "Bluetooth adapter is enabled!");
            startScan();
        }
    }

    /**
     * Start scanning BLE devices
     */
    private void startScan() {
        Log.d(TAG, "startScan IN");

        isScanning = true;
        bluetoothAdapter.startLeScan(leScanCallback);

        // Stops scanning after a pre-defined scan period.
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Scan ble device time out!");
                stopScan();
            }
        }, SCAN_PERIOD);
    }

    /**
     * Stop scanning BLE devices
     */
    private void stopScan() {
        Log.d(TAG, "stopScan IN");

        isScanning = false;
        bluetoothAdapter.stopLeScan(leScanCallback);
    }

    public void unregisterService(){
        Log.d(TAG, "unregisterService IN");

    }

    /**
     * Device scan callback
     */
    private BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            Log.d(TAG, "onLeScan: device: " + device.getName() + ", address: " + device.getAddress() + ", RSSI: " + rssi);
            Log.d(TAG, "---------------------------------------------");

            try {
                Log.d(TAG, "device: " + device);
                Log.d(TAG, "device's name: " + device.getName());

                if (device != null &&
                    device.getName() != null &&
                    device.getName().equals(TARGET_DEVICE_NAME)) {
                    Log.d(TAG, "device found: " + device.getName());

                    deviceAddress = device.getAddress();

                    // Stop ble device scanning
                    stopScan();

                    // Register Broadcast listener
                    context.registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter());

                    // Try to connect target ble device
                    if (bluetoothLeService != null) {
                        final boolean result = bluetoothLeService.connect(deviceAddress);
                        Log.d(TAG, "Connect request result: " + result);
                    }
                } else {
                    // Do nothing
                }
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver gattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                Log.d(TAG, "BLE device is connected!");
                isConnected = true;
//                updateConnectionState(R.string.connected);
//                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                Log.d(TAG, "BLE device is disconnected!");
                isConnected = false;
//                updateConnectionState(R.string.disconnected);
//                invalidateOptionsMenu();
//                clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                Log.d(TAG, "BLE service discovered");
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(bluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                Log.d(TAG, "BLE data available");
//                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    private void displayGattServices(List<BluetoothGattService> gattServices) {
        Log.d(TAG, "displayGattServices IN");

        if (gattServices != null) {
            // Loops through available GATT Services.
            for (BluetoothGattService gattService : gattServices) {
                String serviceUuid = gattService.getUuid().toString();

                Log.d(TAG, "Service UUID: " + serviceUuid);

                List<BluetoothGattCharacteristic> gattCharacteristics =
                        gattService.getCharacteristics();

                // Loops through available Characteristics.
                for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                    String CharacteristicUuid = gattCharacteristic.getUuid().toString();
                    Log.d(TAG, "Characteristic UUID: " + CharacteristicUuid);
                }
                Log.d(TAG, "=====================================");
            }
        }
    }
}



