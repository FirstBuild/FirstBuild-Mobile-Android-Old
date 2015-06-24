package com.firstbuild.commonframework.bleManager;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;

import java.util.List;

/**
 * Created by ryanlee on 3/17/15.
 */
public abstract class BleListener {
    public void onScan(final String action, final String address){}

    public void onConnectionStateChange(final String address, final String status){}

    public void onServicesDiscovered(final String address, final List<BluetoothGattService> bleGattServices){}

    public void onCharacteristicChanged(final String address, final String UUID, final String value){}

    public void onCharacteristicWrite(final String address, final String UUID, final String value){}

    public void onCharacteristicRead(final String address, final String UUID, final String value){}
}
