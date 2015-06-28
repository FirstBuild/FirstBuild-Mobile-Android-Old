package com.firstbuild.commonframework.bleManager;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import com.firstbuild.androidapp.ParagonValues;

import java.util.List;

/**
 * Created by RyanLee on 6/21/15/FW26.
 */
public class BleDevice {
    private String address = "";
    private String nickName = "";
    private String batteryLevel = "";
    private String remainingTime ="";
    private String cookTime ="";
    private String targetTemperature = "";
    private String currentTemperature = "";

    private List<BluetoothGattService> bleGattServices = null;

    public BleDevice(){
        // Default constructor
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
        return address;
    }

    /**
     * Set device's mac address
     * @param address mac address
     */
    public void setAddress(String address) {
        if(address != null) {
            this.address = address;
        }
    }

    /**
     * Retrieves ble device's battery level in percentage
     * @return battery level
     */
    public String getBatteryLevel() {
        return batteryLevel;
    }

    /**
     * Set battery level read from a ble device
     * @param batteryLevel battery level in percentage
     */
    public void setBatteryLevel(String batteryLevel) {
        if(batteryLevel != null) {
            this.batteryLevel = batteryLevel;
        }
    }

    /**
     * Retrieves current remaining cook time
     * @return remaining time
     */
    public String getRemainingTime() {
        return remainingTime;
    }

    /**
     * Set remaining cook time.
     * @param remainingTime remaining time fed from a ble device
     */
    public void setRemainingTime(String remainingTime) {
        if(remainingTime != null){
            this.remainingTime = remainingTime;
        }
    }

    /**
     * Retrieves target temperature
     * @return target temperature
     */
    public String getTargetTemperature() {
        return targetTemperature;
    }

    /**
     * Set target temperature provided by a app
     * @param targetTemperature target Temperature
     */
    public void setTargetTemperature(String targetTemperature) {
        if (targetTemperature != null){
            this.targetTemperature = targetTemperature;
        }

        // TODO send message to a ble device
    }

    /**
     * Retrieve current temperature a ble device reads
     * @return current temperature
     */
    public String getCurrentTemperature() {
        return currentTemperature;
    }

    /**
     * Set current temperature a ble device reads
     * @param currentTemperature current temperature
     */
    public void setCurrentTemperature(String currentTemperature) {
        if(currentTemperature != null) {
            this.currentTemperature = currentTemperature;
        }
    }

    /**
     * Set bluetooth Gatt service
     * @param bleGattServices services retrieves from bluetooth device
     */
    public void setBluetoothServices(List<BluetoothGattService> bleGattServices){
        if(bleGattServices != null) {
            this.bleGattServices = bleGattServices;

            // Iterate services and characteristic and save values
            for(BluetoothGattService service : bleGattServices){
                for(BluetoothGattCharacteristic characteristic : service.getCharacteristics()){

                    if(characteristic.getUuid().toString().equals(ParagonValues.CHARACTERISTIC_BATTERY_LEVEL)){
//                        this.batteryLevel = characteristic.getValue();
                    }
                    else if(characteristic.getUuid().toString().equals(ParagonValues.CHARACTERISTIC_PROBE_FIRMWARE_INFO)){

                    }
                    else if(characteristic.getUuid().toString().equals(ParagonValues.CHARACTERISTIC_SPECIAL_FEATURES)){

                    }
                    else if(characteristic.getUuid().toString().equals(ParagonValues.CHARACTERISTIC_ERROR_STATE)){

                    }
                    else if(characteristic.getUuid().toString().equals(ParagonValues.CHARACTERISTIC_APP_INFO)){

                    }
                    else if(characteristic.getUuid().toString().equals(ParagonValues.CHARACTERISTIC_PROBE_CONNECTION_STATE)){

                    }
                    else if(characteristic.getUuid().toString().equals(ParagonValues.CHARACTERISTIC_BURNER_STATUS)){

                    }
                    else if(characteristic.getUuid().toString().equals(ParagonValues.CHARACTERISTIC_ELAPSED_TIME)){

                    }
                    else if(characteristic.getUuid().toString().equals(ParagonValues.CHARACTERISTIC_COOK_TIME)){

                    }
                    else if(characteristic.getUuid().toString().equals(ParagonValues.CHARACTERISTIC_TARGET_TEMPERATURE)){

                    }
                    else if(characteristic.getUuid().toString().equals(ParagonValues.CHARACTERISTIC_CURRENT_TEMPERATURE)){

                    }
                }
            }
        }
    }

    /**
     * Retrieves bluetooth service list
     * @return
     */
    public List<BluetoothGattService> getBluetoothService(){
        return bleGattServices;
    }

    public void resetAllData(){
        address = "";
        nickName = "";
        batteryLevel = "";
        remainingTime ="";
        targetTemperature = "";
        currentTemperature = "";
        bleGattServices = null;
    }
}
