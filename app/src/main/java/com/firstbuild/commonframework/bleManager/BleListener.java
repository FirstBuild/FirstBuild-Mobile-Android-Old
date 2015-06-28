package com.firstbuild.commonframework.bleManager;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;

import java.util.HashMap;
import java.util.List;

/**
 * Created by ryanlee on 3/17/15.
 */
public abstract class BleListener {
    public void onScanDevices(final HashMap<String, BluetoothDevice> bluetoothDevices){};

    public void onScanStateChanged(final int status){}

    public void onConnectionStateChanged(final String address, final int status){}

    public void onServicesDiscovered(final String address, final List<BluetoothGattService> bleGattServices){}

    public void onCharacteristicRead(final String address, final String uuid, final byte[] value){}

    public void onCharacteristicWrite(final String address, final String uuid, final byte[] value){}

    public void onCharacteristicChanged(final String address, final String uuid, final byte[] value){}
}
