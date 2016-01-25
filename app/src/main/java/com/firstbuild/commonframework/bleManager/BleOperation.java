package com.firstbuild.commonframework.bleManager;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;

/**
 * Created by Hollis on 1/22/16.
 */
public abstract class BleOperation {
    public static final int CONNECT_TIMEOUT_IN_MILLIS = 30000;
    public static final int DEFAULT_TIMEOUT_IN_MILLIS = 10000;
    private int timeoutTime;
    private BluetoothDevice device;

    public BleOperation(BluetoothDevice device) {
        this.device = device;
        this.timeoutTime = DEFAULT_TIMEOUT_IN_MILLIS;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public abstract boolean hasCallback();

    public abstract void execute(BluetoothGatt bluetoothGatt);

    public int getTimoutTime() {
        return timeoutTime;
    }

    public void setTimeoutTime(int timeoutTime){
        this.timeoutTime = timeoutTime;
    }

}
