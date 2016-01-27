/**
 * @file BleDevice.java
 * @brief This class keeps BLE device's information
 * @author Ryan Lee (strike77@gmail.com)
 * @date Jun/29/2015
 */

package com.firstbuild.commonframework.bleManager;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import java.util.List;


public class BleDevice {
    // Keeping ble device's address
//    private String address = "";

    // Keeping ble device's nick name
    private String nickName = "";

    // Keeping bleGattServices
    private List<BluetoothGattService> bleGattServices = null;

    // state for connection.
    public int connectionState = BleValues.STATE_DISCONNECTED;

//    // Blue tooth Gatt handler
//    public BluetoothGatt bluetoothGatt;

    // BleDevice Object variable
    public BluetoothDevice bluetoothDevice = null;

    /**
     * Default constructor
     */
    public BleDevice(){
        // Default constructor
    }

    /**
     * Retrieves characteristic value
     * @param characteristicUuid a desired characteristic's UUID
     * @return a desired characteristic's value
     */
    public byte[] getValue(String characteristicUuid){

        byte[] result = null;

        // Check bleGattServices size.
        if(bleGattServices != null && bleGattServices.size() > 0) {

            // Iterate services and characteristic and find a desired value
            for (BluetoothGattService service : bleGattServices) {
                for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {

                    if (characteristic.getUuid().toString().equals(characteristicUuid)) {
                        // Characteristic found
                        result = characteristic.getValue();
                    }
                }
            }
        }
        else{
            // Do nothing
        }

        return result;
    }

    /**
     * Retrieves device's nick name
     * @return nick name
     */
    public String getNickName() {
        return nickName;
    }

    /**
     * Set device's nick name
     * @param nickName nick name
     */
    public void setNickName(String nickName) {
        if(nickName != null) {
            this.nickName = nickName;
        }
    }

    /**
     * Retrieves device's mac address
     * @return address
     */
    public String getAddress() {
        return bluetoothDevice.getAddress();
    }

//    /**
//     * Set device's mac address
//     * @param address mac address
//     */
//    public void setAddress(String address) {
//        if(address != null) {
//            this.address = address;
//        }
//    }

    /**
     * Set bluetooth Gatt service
     * @param bleGattServices services retrieves from bluetooth device
     */
    public void setBluetoothServices(List<BluetoothGattService> bleGattServices){
        if(bleGattServices != null) {
            this.bleGattServices = bleGattServices;
        }
    }

    /**
     * Retrieves bluetooth service list
     * @return
     */
    public List<BluetoothGattService> getBluetoothService(){
        return bleGattServices;
    }

    /**
     * reset all the ble related variables
     */
    public void resetAllData(){
        // Clear all the data
//        address = "";
        nickName = "";
        bleGattServices = null;
    }
}
