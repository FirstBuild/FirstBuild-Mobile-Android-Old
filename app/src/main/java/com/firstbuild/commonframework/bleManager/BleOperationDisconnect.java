package com.firstbuild.commonframework.bleManager;

import android.bluetooth.BluetoothGatt;

/**
 * Created by Hollis on 1/22/16.
 */
public class BleOperationDisconnect extends BleOperation {

    public BleOperationDisconnect(BleDevice device) {
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
