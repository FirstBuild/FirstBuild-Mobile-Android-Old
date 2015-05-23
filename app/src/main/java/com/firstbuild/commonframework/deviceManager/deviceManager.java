package com.firstbuild.commonframework.deviceManager;

import android.bluetooth.BluetoothDevice;

import java.util.ArrayList;

/**
 * Created by ryanlee on 5/22/15.
 */
public class DeviceManager {

    ArrayList<Device> devices = new ArrayList<>();

    // A Singleton object
    private static DeviceManager instance = new DeviceManager();

    public static DeviceManager getInstance() {
        return instance;
    }

    private DeviceManager() {
        // Default constructor
    }

    public int getSize(){
        return devices.size();
    }

    public void add(Device device){
        if(device != null) {
            devices.add(device);
        }
    }

    public void remove(String address){
        if(address != null){
            for(Device device : devices){
                if(device.getMacAddress().equals(address)){
                    devices.remove(device);
                    break;
                }
                else{
                    // Do nothing
                }
            }
        }
    }

    public Device getDevice(String address){
        Device device = null;
        if(address != null){
            for(Device item : devices){
                if(item.getMacAddress().equals(address)){
                    device = item;
                    break;
                }
                else{
                    // Do nothing
                }
            }
        }

        return device;
    }
}
