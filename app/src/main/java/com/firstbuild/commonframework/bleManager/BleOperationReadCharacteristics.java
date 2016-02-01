package com.firstbuild.commonframework.bleManager;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

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
}
