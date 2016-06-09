/**
 * @file BleListener.java
 * @brief This class contains callback methods for handling important ble related event.
 * @author Ryan Lee (strike77@gmail.com)
 * @date Jun/29/2015
 */

package com.firstbuild.commonframework.blemanager;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;

import java.util.HashMap;
import java.util.List;


public abstract class BleListener {
    public void onScanDevices(final HashMap<String, BluetoothDevice> bluetoothDevices){};

    public void onScanStateChanged(final int status){}

    public void onConnectionStateChanged(final String address, final int status){}

    public void onServicesDiscovered(final String address, final List<BluetoothGattService> bleGattServices){}

    public void onCharacteristicRead(final String address, final String uuid, final byte[] value){}

    public void onCharacteristicWrite(final String address, final String uuid, final byte[] value){}

    public void onCharacteristicChanged(final String address, final String uuid, final byte[] value){}

    public void onDescriptorWrite(final String address, final String uuid, final byte[] value){}

}
