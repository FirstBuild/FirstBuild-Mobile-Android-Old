package com.firstbuild.commonframework.blemanager;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;

/**
 * Created by Hollis on 1/22/16.
 */
public class BleOperationDeleteDevice extends BleOperation {

    public BleOperationDeleteDevice(BluetoothDevice device) {
        super(device);
    }

    @Override
    public boolean hasCallback() {
        return false;
    }

    @Override
    public void execute(BluetoothGatt bluetoothGatt) {
        bluetoothGatt.close();
    }
}
