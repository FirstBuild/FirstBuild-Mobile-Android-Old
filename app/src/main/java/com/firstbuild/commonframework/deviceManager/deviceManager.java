package com.firstbuild.commonframework.deviceManager;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ryanlee on 5/22/15.
 */
public class DeviceManager {

    private final String TAG = this.getClass().getSimpleName();

    private ArrayList<Device> devices = new ArrayList();

    // A Singleton object
    private static DeviceManager instance = new DeviceManager();

    public static DeviceManager getInstance() {
        return instance;
    }

    private DeviceManager() {
        // Default constructor
    }

    public int size(){
        Log.d(TAG, "size IN");
        return devices.size();
    }

    public void add(Device device){
        Log.d(TAG, "add IN");
        if(device != null) {
            devices.add(device);
        }
    }

    public void remove(String address){
        Log.d(TAG, "remove IN");

        if(address != null){
            for(Device device : devices){
                if(device.getAddress().equals(address)){
                    devices.remove(device);
                    Log.d(TAG, "Removed device: " +  device.getAddress());
                    break;
                }
                else{
                    // Do nothing
                }
            }
        }
    }

    public Device getDevice(String address){
        Log.d(TAG, "getDevice IN");

        Device device = null;

        if(address != null){
            for(Device item : devices){
                if(item.getAddress().equals(address)){
                    device = item;
                    Log.d(TAG, "Found device: " +  device.getAddress());
                    break;
                }
                else{
                    // Do nothing
                }
            }
        }

        return device;
    }

    public void setServices(String address, List<BluetoothGattService> bleGattServices){
        Log.d(TAG, "setService");

        if(address != null){
            for(Device device : devices){
                if(device.getAddress().equals(address)){
                    device.setBluetoothServices(bleGattServices);
                }
            }
        }
    }

    public List<BluetoothGattService> getServices(String address){

        List<BluetoothGattService> bleGattServices = null;

        if(address != null){
            for(Device device : devices){
                if(device.getAddress().equals(address)){
                    Log.d(TAG, "Found ble services: " +  device.getAddress());
                    bleGattServices = device.getBluetoothService();
                    break;
                }
                else{
                    // Do nothing
                }
            }
        }

        return bleGattServices;
    }
}

