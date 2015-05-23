/**
 * @file Device.java
 * @brief A class keeping device's information
 * @author Ryan Lee - 320006284
 * @date May/23/2015
 * Copyright (c) 2014 General Electric Corporation - Confidential - All rights reserved.
 */

package com.firstbuild.commonframework.deviceManager;

public class Device {
    private String macAddress = "";
    private String serialNumber = "";
    private String modelNumber = "";
    private String nickName = "";

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
     * @return mac address
     */
    public String getMacAddress() {
        return macAddress;
    }

    /**
     * Set device's mac address
     * @param macAddress mac address
     */
    public void setMacAddress(String macAddress) {
        if(macAddress != null) {
            this.macAddress = macAddress;
        }
    }
}
