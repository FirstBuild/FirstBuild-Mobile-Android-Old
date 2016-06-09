package com.firstbuild.commonframework.blemanager;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;

/**
 * Created by Hollis on 1/22/16.
 */
public class BleOperationDisconnect extends BleOperation {

    public BleOperationDisconnect(BluetoothDevice device) {
        super(device);
    }

    @Override
    public boolean hasCallback() {
        return true;
    }

    @Override
    public void execute(BluetoothGatt bluetoothGatt) {
        bluetoothGatt.disconnect();
    }
}
