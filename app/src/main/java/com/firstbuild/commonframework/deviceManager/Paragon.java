/**
 * @file Paragon.java
 * @brief A paragon device class derived from device. this class keeps paragon's data
 * @author Ryan Lee - 320006284
 * @date May/23/2015
 * Copyright (c) 2014 General Electric Corporation - Confidential - All rights reserved.
 */

package com.firstbuild.commonframework.deviceManager;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;

import java.util.List;

public class Paragon extends Device {
    // Paragon related variables
    private String batteryLevel = "";
    private String remainingTime ="";
    private String targetTemperature = "";
    private String currentTemperature = "";

    // Bluetooth related variables
    private BluetoothDevice bluetoothDevice = null;
    private List<BluetoothGattService> bluetoothServices = null;

    /**
     * Constructor
     * @param bluetoothDevice
     */
    public Paragon(BluetoothDevice bluetoothDevice){
        if(bluetoothDevice != null) {
            this.bluetoothDevice = bluetoothDevice;
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
     * Retrieves bluetooth device object
     * @return bluetoothDevice object
     */
    public BluetoothDevice getBluetoothDevice(){
        return bluetoothDevice;
    }

    /**
     * Set bluetooth Gatt service
     * @param bluetoothServices services retrieves from bluetooth device
     */
    public void setBluetoothServices(List<BluetoothGattService> bluetoothServices){
        if(bluetoothServices != null) {
            this.bluetoothServices = bluetoothServices;
        }
    }

    /**
     * Retrieves bluetooth service list
     * @return
     */
    public List<BluetoothGattService> getBluetoothService(){
        return bluetoothServices;
    }
}
