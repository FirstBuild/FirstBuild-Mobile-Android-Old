package com.firstbuild.commonframework.deviceManager;

import android.bluetooth.BluetoothGattService;

import java.util.List;

/**
 * Created by RyanLee on 6/21/15/FW26.
 */
public class Device {
    private String address = "";
    private String serialNumber = "";
    private String modelNumber = "";
    private String nickName = "";
    private String batteryLevel = "";
    private String remainingTime ="";
    private String targetTemperature = "";
    private String currentTemperature = "";

    private List<BluetoothGattService> bleGattServices = null;

    public Device(String address){
        this.address = address;
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
     * Retrieves device's model number
     * @return model number
     */
    public String getModelNumber() {
        return modelNumber;
    }

    /**
     * Set device's model number
     * @param modelNumber model number
     */
    public void setModelNumber(String modelNumber) {
        if(modelNumber != null) {
            this.modelNumber = modelNumber;
        }
    }

    /**
     * Retrieves device's serial number
     * @return serial number
     */
    public String getSerialNumber() {
        return serialNumber;
    }

    /**
     * Set device's serial number
     * @param serialNumber serial number
     */
    public void setSerialNumber(String serialNumber) {
        if(serialNumber != null) {
            this.serialNumber = serialNumber;
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
        serialNumber = "";
        modelNumber = "";
        nickName = "";
        batteryLevel = "";
        remainingTime ="";
        targetTemperature = "";
        currentTemperature = "";
        bleGattServices = null;
    }
}
