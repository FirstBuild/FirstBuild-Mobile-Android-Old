package com.firstbuild.commonframework.blemanager;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Hollis on 1/22/16.
 */
public class BleOperationReadCharacteristics extends BleOperation {
    private String TAG = BleOperationReadCharacteristics.class.getSimpleName();
    private String characteristicsUuid;

    public BleOperationReadCharacteristics(BluetoothDevice device, String characteristicsUuid) {
        super(device);
        this.characteristicsUuid = characteristicsUuid;
    }

    @Override
    public boolean hasCallback() {
        return true;
    }

    @Override
    public void execute(BluetoothGatt bluetoothGatt) {
        Log.d(TAG, "" + characteristicsUuid);

//        bluetoothGatt.getServices();
        List<BluetoothGattService> bleGattServices = bluetoothGatt.getServices();

        // Iterate services and characteristic
        for (BluetoothGattService service : bleGattServices) {
            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {

                if (characteristic.getUuid().toString().equalsIgnoreCase(characteristicsUuid)) {
                    Log.d(TAG, "Found Characteristic for reading: " + characteristic.getUuid().toString());
                    bluetoothGatt.readCharacteristic(characteristic);
                    break;
                } else {
                    // Do nothing
                }
            }
        }

    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        }

        if(!(o instanceof BleOperationReadCharacteristics)) {
            return false;
        }

        BleOperationReadCharacteristics other = (BleOperationReadCharacteristics)o;

        return this.characteristicsUuid.equalsIgnoreCase(other.characteristicsUuid);
    }

    @Override
    public int hashCode() {
        return characteristicsUuid.hashCode();
    }
}
