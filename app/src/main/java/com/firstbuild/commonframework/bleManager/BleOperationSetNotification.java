package com.firstbuild.commonframework.blemanager;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import com.firstbuild.androidapp.OpalValues;

import java.util.List;
import java.util.UUID;

/**
 * Created by Hollis on 1/22/16.
 */
public class BleOperationSetNotification extends BleOperation {
    private String TAG = BleOperationSetNotification.class.getSimpleName();
    private String characteristicsUuid;
    private boolean isEnabled;

    private static final byte[] ENABLE_NOTIFICATION_INDICATION_VALUE = {0x03, 0x00};

    public BleOperationSetNotification(BluetoothDevice device, String characteristicsUuid, boolean isEnabled) {
        super(device);
        this.characteristicsUuid = characteristicsUuid;
        this.isEnabled = isEnabled;
    }

    @Override
    public boolean hasCallback() {
        return true;
    }

    @Override
    public void execute(BluetoothGatt bluetoothGatt) {
        Log.d(TAG, "" + characteristicsUuid + ", " + isEnabled);
        List<BluetoothGattService> bleGattServices = bluetoothGatt.getServices();

        // Iterate services and characteristic
        for (BluetoothGattService service : bleGattServices) {
            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {

                if (characteristic.getUuid().toString().equalsIgnoreCase(characteristicsUuid)) {
                    Log.d(TAG, "Found Characteristic for notification: " + characteristic.getUuid().toString());

                    if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) {
                        Log.d(TAG, characteristic.getUuid().toString() + "\tCurrent Characteristic is compatible with  BluetoothGattCharacteristic.PROPERTY_NOTIFY" );

                    }

                    if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0) {
                        Log.d(TAG, characteristic.getUuid().toString() + "\tCurrent Characteristic is compatible with  BluetoothGattCharacteristic.PROPERTY_INDICATE" );
                    }

                    // enable local notification
                    bluetoothGatt.setCharacteristicNotification(characteristic, isEnabled);

//                    try {
//                        Thread.sleep(1000);
//                    }
//                    catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }

                    // Enable remote notifications
                    BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(BleManager.CLIENT_CHARACTERISTIC_CONFIGURATION_UUID));

                    if (isEnabled) {

                        if(characteristicsUuid.equalsIgnoreCase(OpalValues.OPAL_OTA_CONTROL_COMMAND_CHAR_UUID)) {
                            // Enable both Notification and Indication for OpalValues.OPAL_OTA_CONTROL_COMMAND_CHAR_UUID
//                            descriptor.setValue(ENABLE_NOTIFICATION_INDICATION_VALUE);

                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);

                        } else {
                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        }

                    }
                    else {
                        descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                    }

                    bluetoothGatt.writeDescriptor(descriptor);
                    break;
                }
                else {
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

        if(!(o instanceof BleOperationSetNotification)) {
            return false;
        }

        BleOperationSetNotification other = (BleOperationSetNotification)o;

        return this.characteristicsUuid.equalsIgnoreCase(other.characteristicsUuid) &&
                this.isEnabled == other.isEnabled && this.getDevice().equals(other.getDevice());
    }

    @Override
    public int hashCode() {
        return characteristicsUuid.hashCode();
    }
}
