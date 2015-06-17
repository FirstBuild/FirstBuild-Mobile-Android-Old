package com.firstbuild.commonframework.deviceManager;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ryanlee on 5/22/15.
 */
public class DeviceManager {

    private String address = "";
    private String serialNumber = "";
    private String modelNumber = "";
    private String nickName = "";
    private String batteryLevel = "";
    private String remainingTime ="";
    private String targetTemperature = "";
    private String currentTemperature = "";

    private List<BluetoothGattService> bleGattServices = null;

    // A Singleton object
    private static DeviceManager instance = new DeviceManager();

    public static DeviceManager getInstance() {
        return instance;
    }

    private DeviceManager() {
        // Default constructor
    }

    public void setServices(List<BluetoothGattService> bleGattServices){
        this.bleGattServices = bleGattServices;
    }

    public List<BluetoothGattService> getServices(){
        return bleGattServices;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getModelNumber() {
        return modelNumber;
    }

    public void setModelNumber(String modelNumber) {
        this.modelNumber = modelNumber;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(String batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public String getRemainingTime() {
        return remainingTime;
    }

    public void setRemainingTime(String remainingTime) {
        this.remainingTime = remainingTime;
    }

    public String getTargetTemperature() {
        return targetTemperature;
    }

    public void setTargetTemperature(String targetTemperature) {
        this.targetTemperature = targetTemperature;
    }

    public String getCurrentTemperature() {
        return currentTemperature;
    }

    public void setCurrentTemperature(String currentTemperature) {
        this.currentTemperature = currentTemperature;
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

        List<BluetoothGattService> bleGattServices = null;
    }
}

