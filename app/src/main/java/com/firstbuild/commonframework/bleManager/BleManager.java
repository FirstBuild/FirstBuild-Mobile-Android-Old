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
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.firstbuild.commonframework.deviceManager.Device;
import com.firstbuild.commonframework.deviceManager.DeviceManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class BleManager {

    private final String TAG = getClass().getSimpleName();

    private final String TARGET_DEVICE_NAME = "BLE ACM";
//    private final String TARGET_DEVICE_NAME = "CC2650 SensorTag";

    private final String GATT_SERVER_SERVICE_UUID = "0db95663-c52e-2fac-2040-c276b2b63090";

    // Set enable bluetooth feature
    private static final int REQUEST_ENABLE_BT = 1;

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    // Bluetooth adapter handler
    private BluetoothAdapter bluetoothAdapter = null;

    private BluetoothLeService bluetoothLeService = null;

    private BluetoothManager bluetoothManager = null;

    private BluetoothGattServer bluetoothGattServer = null;

    // Flag for checking device scanning state
    public boolean isScanning = false;

    // Flag for checking ble connection established
    public boolean isConnected = false;

    // Post Delayed handler
    private Handler handler = new Handler();

    private String deviceAddress = "";

    private Context context = null;

    private HashMap<String, BleListener> callbacks = null;

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

        try {
            startGattServer();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public void addListener(BleListener listener){
        if (callbacks == null) {
            callbacks = new HashMap<String, BleListener>();
        }
        else{
            // Do nothing
        }

        // Key is listener is casted string
        // Value is listener itself.
        callbacks.put(listener.toString(), listener);
    }

    public void removeListener(BleListener listener){
        if (callbacks != null && !callbacks.isEmpty()) {
            callbacks.remove(listener.toString());
            Log.d(TAG, "Remove Listener: " + listener);
        }
        else{
            // Do nothing
        }
    }

    /**
     * Send update to subscribers
     * @param listener subscriber
     * @param args corresponding arguments
     */
    public void sendUpdate(String listener, Object... args){
        Log.d(TAG, "sendUpdate: " + "IN");

        // Clone hashmap to avoid java.util.ConcurrentModificationException
        HashMap<String, BleListener> callbackClone = (HashMap) callbacks.clone();
        for (Map.Entry<String, BleListener> entry : callbackClone.entrySet()) {
            BleListener callback = entry.getValue();

            if (callback != null && listener.equals("onScan")) {
                callback.onScan((String) args[0], (String) args[1]);
            }
            else if (callback != null && listener.equals("onConnectionStateChanged")) {
                callback.onConnectionStateChange((String) args[0], (String) args[1]);
            }
            else if (callback != null && listener.equals("onServicesDiscovered")) {
                callback.onServicesDiscovered((String) args[0], (List<BluetoothGattService>) args[1]);
            }
            else if (callback != null && listener.equals("onCharacteristicChanged")){
                callback.onCharacteristicChanged((String) args[0], (String) args[1], (String) args[2]);
            }
            else{
                // Do nothing
            }
        }
    }

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            Log.d(TAG, "onServiceConnected");
            bluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!bluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
//                finish();
                // TODO:  Notify end service
            }

            // Automatically connects to the device upon successful start-up initialization.
            if (!deviceAddress.equals("")) {

                Log.d(TAG, "Address: " + deviceAddress);
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
//            startScan();

            if(isDevicePaired()){
                // Register Broadcast listener
                context.registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter());

                // Try to connect target ble device
                if (bluetoothLeService != null) {
                    Log.d(TAG, "Connecting...");
                    final boolean result = bluetoothLeService.connect(deviceAddress);
                    Log.d(TAG, "Connect request result: " + result);
                }
            }
            else{
                startScan();
            }
        }
    }

    private boolean isDevicePaired() {
        boolean result = false;

        Set<BluetoothDevice> pairedDevice = bluetoothAdapter.getBondedDevices();
        if(pairedDevice.size( ) > 0)
        {
            for(BluetoothDevice device : pairedDevice) {
                Log.d(TAG, device.getName() + "\n" + device.getAddress());

                if(device.getName().equals(TARGET_DEVICE_NAME)){
                    deviceAddress = device.getAddress();
                    result = true;
                }
            }
        }

        return result;
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

        sendUpdate("onScan", new Object[]{"start", ""});
    }

    /**
     * Stop scanning BLE devices
     */
    private void stopScan() {
        Log.d(TAG, "stopScan IN");

        isScanning = false;
        bluetoothAdapter.stopLeScan(leScanCallback);

        sendUpdate("onScan", new Object[]{"stop", deviceAddress});
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
            final String address = bluetoothLeService.getBluetoothDeviceAddress();
            final String action = intent.getAction();

            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                Log.d(TAG, "BLE device is connected!");

                isConnected = true;
                DeviceManager.getInstance().add(new Device(address));

                sendUpdate("onConnectionStateChanged", new Object[]{address, action});
            }
            else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                Log.d(TAG, "BLE device is disconnected!");

                isConnected = false;
                DeviceManager.getInstance().remove(address);

                sendUpdate("onConnectionStateChanged", new Object[]{address, action});
            }
            else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                Log.d(TAG, "BLE service discovered");

                List<BluetoothGattService> bleGattServices = bluetoothLeService.getSupportedGattServices();
                DeviceManager.getInstance().setServices(address, bleGattServices);

                // Show all the supported services and characteristics on the user interface.
                displayGattServices(address);

                // Stop Scan
                stopScan();

                // Stop GATT Server
                stopGattServer();

                sendUpdate("onServicesDiscovered", new Object[]{address, bleGattServices});
            }
            else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                Log.d(TAG, "BLE data available");
                String data = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                Log.d(TAG, "Received Data: " + data);

                sendUpdate("onCharacteristicChanged", new Object[]{address, data});
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

    public void displayGattServices(String address) {
        Log.d(TAG, "displayGattServices IN");

        List<BluetoothGattService> bleGattServices = DeviceManager.getInstance().getServices(deviceAddress);

        if (bleGattServices != null) {
            // Loops through available GATT Services.
            for (BluetoothGattService gattService : bleGattServices) {
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

    public void readCharacteristics(String Uuid){
        Log.d(TAG, "readCharacteristics IN");

        BluetoothGattService service = new  BluetoothGattService(UUID.fromString(Uuid), BluetoothGattService.SERVICE_TYPE_PRIMARY);
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(Uuid));
        bluetoothLeService.readCharacteristic(characteristic);
    }

    public void writeCharateristics(String Uuid, byte[] value){

        BluetoothGattService service = new BluetoothGattService(UUID.fromString(Uuid), BluetoothGattService.SERVICE_TYPE_PRIMARY);
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(Uuid));

        if (characteristic == null) {
            Log.e(TAG, "char not found!");
        }else{
            characteristic.setValue(value);
        }
    }

    public void disconnect(){
        bluetoothLeService.disconnect();
    }

    public void startGattServer() throws InterruptedException {
        Log.d(TAG, "startGattServer IN");
        BluetoothGattService service;

        bluetoothGattServer = bluetoothManager.openGattServer(context, gattServerCallback);
        Thread.sleep(10);
        service = new BluetoothGattService(UUID.fromString(GATT_SERVER_SERVICE_UUID), BluetoothGattService.SERVICE_TYPE_PRIMARY);
        bluetoothGattServer.addService(service);
        Thread.sleep(100);
        Log.d(TAG, "startGattServer OUT");
    }

    public void stopGattServer(){
        Log.d(TAG, "stopGattServer IN");
        bluetoothGattServer.close();
    }


    private final BluetoothGattServerCallback gattServerCallback = new BluetoothGattServerCallback() {
        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            Log.d(TAG, "Our gatt server connection state changed, new state: " + Integer.toString(newState));
            Log.d(TAG, "Device:" + device.getAddress());

            super.onConnectionStateChange(device, status, newState);
        }
 
        @Override
        public void onServiceAdded(int status, BluetoothGattService service) {
            Log.d(TAG, "Our gatt server service was added.");
            super.onServiceAdded(status, service);
        }

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
            Log.d(TAG, "Our gatt characteristic was read.");
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
        }

        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            Log.d(TAG, "We have received a write request for one of our hosted characteristics");

            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);
        }

        @Override
        public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor) {
            Log.d(TAG, "Our gatt server descriptor was read.");
            super.onDescriptorReadRequest(device, requestId, offset, descriptor);
        }

        @Override
        public void onDescriptorWriteRequest(BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            Log.d(TAG, "Our gatt server descriptor was written.");
            super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value);
        }

        @Override
        public void onExecuteWrite(BluetoothDevice device, int requestId, boolean execute) {
            Log.d(TAG, "Our gatt server on execute write.");
            super.onExecuteWrite(device, requestId, execute);
        }
    };
}



