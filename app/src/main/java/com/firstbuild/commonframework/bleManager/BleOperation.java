package com.firstbuild.commonframework.bleManager;

import android.bluetooth.BluetoothGatt;

/**
 * Created by Hollis on 1/22/16.
 */
public abstract class BleOperation {
    private static final int DEFAULT_TIMEOUT_IN_MILLIS = 10000;
    private BleDevice device;

    public BleOperation(BleDevice device) {
        this.device = device;
    }

    public BleDevice getDevice() {
        return device;
    }

    public abstract boolean hasCallback();

    public abstract void execute(BluetoothGatt bluetoothGatt);

    public int getTimoutTime() {
        return DEFAULT_TIMEOUT_IN_MILLIS;
    }

}
