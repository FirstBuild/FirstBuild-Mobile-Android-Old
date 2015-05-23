package com.firstbuild.commonframework.bleManager;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.content.Intent;

/**
 * Created by ryanlee on 3/17/15.
 */
public abstract class BluetoothListener {
    public void onScan(String action){}

    public void onPairing(Context context, Intent intent){}

    public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState){}

    public void onServicesDiscovered(final BluetoothGatt gatt, final int status){}

    public void onCharacteristicWrite(BluetoothGatt gatt,
                                      final BluetoothGattCharacteristic characteristic,
                                      int status){}

    public void onCharacteristicRead(BluetoothGatt gatt,
                                     final BluetoothGattCharacteristic characteristic,
                                     int status){}
}
