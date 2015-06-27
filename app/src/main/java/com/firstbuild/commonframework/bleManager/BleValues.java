package com.firstbuild.commonframework.bleManager;

/**
 * Created by RyanLee on 6/26/15/FW26.
 */
public class BleValues {

    // Scan related variables
    public static final int STOP_SCAN = 0;
    public static final int START_SCAN = 1;

    // bluetooth connection state related variables
    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;

    public static final String ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public static final String ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public static final String ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public static final String ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public static final String EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA";

    public static final int REQUEST_ENABLE_BT = 1;

    // Stops scanning after 10 seconds.
    public static final long SCAN_PERIOD = 10000;
}
