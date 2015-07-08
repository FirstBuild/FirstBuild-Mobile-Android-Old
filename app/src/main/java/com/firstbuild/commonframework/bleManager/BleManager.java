/**
 * @file BleManager.java
 * @brief BleManager handles all kind of interface with ble devices
 * @author Ryan Lee (strike77@gmail.com)
 * @date May/22/2015
 */

package com.firstbuild.commonframework.bleManager;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class BleManager {

    private final String TAG = getClass().getSimpleName();

    // Set enable bluetooth feature
    private int connectionState = BleValues.STATE_DISCONNECTED;

    private HashMap<String, BluetoothDevice> scannedDevices = new HashMap<String, BluetoothDevice>();

    // Blue tooth Gatt handler
    private BluetoothGatt bluetoothGatt;

    // Bluetooth adapter handler
    private BluetoothAdapter bluetoothAdapter = null;
    private BluetoothManager bluetoothManager = null;

    // Flag for checking device scanning state
    public boolean isScanning = false;

    // Post Delayed handler
    private Handler handler = new Handler();
    private Runnable stopScanRunnable = null;
    private Context context = null;
    private HashMap<String, BleListener> callbacks = null;

    // BleDevice Object variable
    private BleDevice bleDevice = new BleDevice();

    public static BleManager instance = new BleManager();
    public static BleManager getInstance(){
        return instance;
    }

    public BleManager(){
        // Default constructor
    }

    /**
     * Initialize BleManager
     * @param context activity's context
     */
    public void initBleManager(Context context){
        Log.d(TAG, "initBleManager IN");

        this.context = context;
        bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);

        // Get bluetooth adaptor
        bluetoothAdapter = bluetoothManager.getAdapter();
    }

    /**
     * Add a listener to subscribe ble event
     * @param listener listener instance
     */
    public void addListener(BleListener listener){
        if (callbacks == null) {
            callbacks = new HashMap<String, BleListener>();
        }
        else{
            // Do nothing
        }

        // Key is listener is casted string
        // Value is listener itself.
        callbacks.put(listener.toString(), listener);
    }

    /**
     * Remove a listener for ble event
     * @param listener listener instance
     */
    public void removeListener(BleListener listener){
        if (callbacks != null && !callbacks.isEmpty()) {
            callbacks.remove(listener.toString());
            Log.d(TAG, "Remove Listener: " + listener);
        }
        else{
            // Do nothing
        }
    }

    /**
     * Send update to subscribers
     * @param listener subscriber
     * @param args corresponding arguments
     */
    public void sendUpdate(String listener, Object... args){

        // Clone hashmap to avoid java.util.ConcurrentModificationException
        HashMap<String, BleListener> callbackClone = (HashMap) callbacks.clone();
        for (Map.Entry<String, BleListener> entry : callbackClone.entrySet()) {
            BleListener callback = entry.getValue();

            if (callback != null && listener.equals("onScanStateChanged")) {
                callback.onScanStateChanged((int) args[0]);
            }
            else if (callback != null && listener.equals("onScanDevices")) {
                callback.onScanDevices((HashMap<String, BluetoothDevice>) args[0]);
            }
            else if (callback != null && listener.equals("onConnectionStateChanged")) {
                callback.onConnectionStateChanged((String) args[0], (int) args[1]);
            }
            else if (callback != null && listener.equals("onServicesDiscovered")) {
                callback.onServicesDiscovered((String) args[0], (List<BluetoothGattService>) args[1]);
            }
            else if (callback != null && listener.equals("onCharacteristicChanged")){
                callback.onCharacteristicChanged((String) args[0], (String) args[1], (byte[]) args[2]);
            }
            else if(callback != null && listener.equals("onCharacteristicRead")){
                callback.onCharacteristicRead((String) args[0], (String) args[1], (byte[]) args[2]);
            }
            else if(callback != null && listener.equals("onCharacteristicWrite")){
                callback.onCharacteristicRead((String) args[0], (String) args[1], (byte[]) args[2]);
            }
            else if(callback != null && listener.equals("onDescriptorWrite")){
                callback.onDescriptorWrite((String) args[0], (String) args[1], (byte[]) args[2]);
            }
            else{
                // Do nothing
            }
        }
    }

    /**
     * Connect to a ble device
     * @param address ble device's address
     * @return connection success or failed
     */
    public boolean connect(final String address) {
        Log.d(TAG, "connect IN");

        if (bluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (address != null && address.equals(address) && bluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing BluetoothGatt for connection.");
            if (bluetoothGatt.connect()) {
                connectionState = BleValues.STATE_CONNECTING;
                return true;
            }
            else {
                return false;
            }
        }

        final BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);

        if (bleDevice == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }

        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        bluetoothGatt = bluetoothDevice.connectGatt(context, false, bluetoothGattCallback);

        Log.d(TAG, "Trying to create a new connection.");
        bleDevice.setAddress(address);

        connectionState = BleValues.STATE_CONNECTING;
        return true;
    }

    /**
     * Disconnect from a ble device
     */
    public void disconnect(){
        Log.d(TAG, "disconnect IN");

        // Disconnect bluetooth connection
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
        }
        else{
            // Do nothing
        }
    }

    /**
     * Check bluetooth feature in the phone is enabled
      * @return enabled or disabled
     */
    public boolean isBluetoothEnabled(){
        Log.d(TAG, "isBluetoothEnabled IN");

        return bluetoothAdapter.isEnabled();
    }

    /**
     * Check device is already bonded one.
     * @param address BLE device's address.
     * @return true or false
     */
    private boolean isDevicePaired(final String address) {
        boolean result = false;

        if(address != null) {

            // Retrieves paired device list
            Set<BluetoothDevice> pairedDevice = bluetoothAdapter.getBondedDevices();
            if (pairedDevice != null && pairedDevice.size() > 0) {

                // Iterate all the device in the list
                for (BluetoothDevice bluetoothDevice : pairedDevice) {
                    Log.d(TAG, bluetoothDevice.getName() + "\n" + bluetoothDevice.getAddress());

                    if (bluetoothDevice.getAddress().equals(address)) {

                        // Device found
                        bleDevice.setAddress(bluetoothDevice.getAddress());
                        result = true;
                    }
                }
            }
        }

        return result;
    }


    /**
     * start device scan for duration
     * @param duration 1 to 120 sec.
     */
    public boolean startScan(final int duration) {
        Log.d(TAG, "startScan IN");

        boolean result = false;

        if(duration > 0 && duration <= 120) {
            // Check duration
            isScanning = true;
            bluetoothAdapter.startLeScan(leScanCallback);

            // Stops scanning after a pre-defined scan period.
            handler.postDelayed(setStopScanRunnable(), duration);

            result = true;
            sendUpdate("onScanStateChanged", new Object[]{BleValues.START_SCAN});
        }
        else{
            Log.d(TAG, "duration is out of range(1 - 120 sec): " + duration);
            result = false;
        }

        return result;
    }

    /**
     * Start device scan for 10 sec
     */
    public void startScan() {
        Log.d(TAG, "startScan IN");

        isScanning = true;

        // Set callback to leScanCallback
        bluetoothAdapter.startLeScan(leScanCallback);

        // Stops scanning after a pre-defined scan period.
        handler.postDelayed(setStopScanRunnable(), BleValues.SCAN_PERIOD);

        sendUpdate("onScanStateChanged", new Object[]{BleValues.START_SCAN});
    }

    /**
     * Stop scanning BLE devices
     */
    public void stopScan() {
        Log.d(TAG, "stopScan IN");

        isScanning = false;
        bluetoothAdapter.stopLeScan(leScanCallback);

        // Stop postDelayed method
        if(stopScanRunnable != null){
            Log.d(TAG, "Remove delayed stopScan task");
            handler.removeCallbacks(stopScanRunnable);
        }

        sendUpdate("onScanStateChanged", new Object[]{BleValues.STOP_SCAN});
    }

    private Runnable setStopScanRunnable(){
        // Create runnable object for stop scanning
        stopScanRunnable = new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Scan ble device time out!");
                stopScan();
            }
        };

        return stopScanRunnable;
    }

    /**
     * Print GATT services and characteristics
     * @param address
     */
    public void displayGattServices(String address) {
        Log.d(TAG, "displayGattServices IN");

        List<BluetoothGattService> bleGattServices = bleDevice.getBluetoothService();

        if (bleGattServices != null) {

            // Loops through available GATT Services.
            for (BluetoothGattService gattService : bleGattServices) {
                String serviceUuid = gattService.getUuid().toString();

                Log.d(TAG, "Service UUID: " + serviceUuid);

                List<BluetoothGattCharacteristic> gattCharacteristics =
                        gattService.getCharacteristics();

                // Loops through available Characteristics.
                for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                    String CharacteristicUuid = gattCharacteristic.getUuid().toString();
                    Log.d(TAG, "Characteristic UUID: " + CharacteristicUuid +
                            ", Permission: " + gattCharacteristic.getPermissions() +
                            ", Write Type: " + gattCharacteristic.getWriteType());
                }

                Log.d(TAG, "=====================================");
            }
        }
    }

    /**
     * Read a characteristic value from the gatt server
     * @param characteristicsUuid bluetooth Characteristic UUID
     */
    public boolean readCharacteristics(final String characteristicsUuid){
        Log.d(TAG, "readCharacteristics IN");

        boolean result = false;

        if (bluetoothAdapter == null || bluetoothGatt == null) {
            //Check BluetoothGatt is available
            Log.w(TAG, "BluetoothAdapter not initialized");
            result = false;
        }
        else{
            // Read a value from the characteristic
            List<BluetoothGattService> bleGattServices = bleDevice.getBluetoothService();

            // Iterate services and characteristic
            for(BluetoothGattService service : bleGattServices){
                for(BluetoothGattCharacteristic characteristic : service.getCharacteristics()){

                    if(characteristic.getUuid().toString().equalsIgnoreCase(characteristicsUuid)){
                        Log.d(TAG, "Found Characteristic: " + characteristic.getUuid().toString());
                        result = bluetoothGatt.readCharacteristic(characteristic);
                        break;
                    }
                }
            }
        }

        return result;
    }

    /**
     * Write a value to a given characteristic in the gatt server.
     * @param characteristicsUuid Characteristic to act on.
     * @param values value to be written.
     */
    public boolean writeCharateristics(final String characteristicsUuid, byte[] values){
        Log.d(TAG, "writeCharateristics IN");

        boolean result = false;

        if (bluetoothAdapter == null || bluetoothGatt == null) {
            // Check BluetoothGatt is available
            Log.w(TAG, "BluetoothAdapter not initialized");
            result = false;
        }
        else{
            // Read a value from the characteristic
            List<BluetoothGattService> bleGattServices = bleDevice.getBluetoothService();

            // Iterate services and characteristic
            for(BluetoothGattService service : bleGattServices){
                for(BluetoothGattCharacteristic characteristic : service.getCharacteristics()){

                    if(characteristic.getUuid().toString().equalsIgnoreCase(characteristicsUuid)){
                        Log.d(TAG, "Found Characteristic: " + characteristic.getUuid().toString());

                        characteristic.setValue(values);
                        result = bluetoothGatt.writeCharacteristic(characteristic);
                        break;
                    }
                }
            }
        }

        return result;
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristicsUuid Characteristic to act on.
     * @param enabled If true, enable notification.  False otherwise.
     */
    public boolean setCharacteristicNotification(final String characteristicsUuid,
                                              boolean enabled) {
        Log.d(TAG, "setCharacteristicNotification IN");

        boolean result = false;

        if (bluetoothAdapter == null || bluetoothGatt == null) {
            // Check BluetoothGatt is available
            Log.w(TAG, "BluetoothAdapter not initialized");
            result = false;
        }
        else {

            // Read a value from the characteristic
            List<BluetoothGattService> bleGattServices = bleDevice.getBluetoothService();

            // Iterate services and characteristic
            for(BluetoothGattService service : bleGattServices){
                for(BluetoothGattCharacteristic characteristic : service.getCharacteristics()){

                    if(characteristic.getUuid().toString().equalsIgnoreCase(characteristicsUuid)){
                        Log.d(TAG, "Found Characteristic for notification: " + characteristic.getUuid().toString());

                        // Set notification
                        result = bluetoothGatt.setCharacteristicNotification(characteristic, enabled);
                        break;
                    }
                }
            }
        }

        return result;
    }

    public boolean writeDescriptor(final String characteristicsUuid){

        Log.d(TAG, "writeDescriptor IN");

        boolean result = false;

        if (bluetoothAdapter == null || bluetoothGatt == null) {
            // Check BluetoothGatt is available
            Log.w(TAG, "BluetoothAdapter not initialized");
            result = false;
        }
        else {

            // Read a value from the characteristic
            List<BluetoothGattService> bleGattServices = bleDevice.getBluetoothService();

            // Iterate services and characteristic
            for(BluetoothGattService service : bleGattServices){
                for(BluetoothGattCharacteristic characteristic : service.getCharacteristics()){

                    if(characteristic.getUuid().toString().equalsIgnoreCase(characteristicsUuid)){
                        Log.d(TAG, "Found  characteristic for write descriptor: " + characteristic.getUuid().toString());

                        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        bluetoothGatt.writeDescriptor(descriptor); //descriptor write operation successfully started?
                        break;
                    }
                }
            }
        }

        return result;
    }


    /**
     * Returns a BleDevice's instance
     * @return BleDevice's instance
     */
    public BleDevice bleDevice(){
        return bleDevice;
    }

    public void printGattValue(byte[] values){
        StringBuilder hexValue = new StringBuilder();

        // Print value in hexa decimal format
        System.out.print("Value: ");
        for(byte value : values){
            hexValue.append(String.format("0x%02x ", value));
        }

        System.out.println(hexValue);
    }

    /**
     * Device scan callback
     */
    private BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice bluetoothDevice, int rssi, byte[] scanRecord) {
            try {
                if (bluetoothDevice != null && bluetoothDevice.getName() != null){
                    scannedDevices.put(bluetoothDevice.getAddress(), bluetoothDevice);

                    // Notify updates to other modules
                    sendUpdate("onScanDevices", new Object[]{scannedDevices});
                }
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
    };


    /**
     * Implements callback methods for GATT events that the app cares about.
     * For example, connection change and services discovered.
     */
    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.d(TAG, "onConnectionStateChange IN");

            String address = gatt.getDevice().getAddress();

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG, "Connected to GATT server.");

                connectionState = BleValues.STATE_CONNECTED;

                // Set address
                bleDevice.setAddress(address);

                sendUpdate("onConnectionStateChanged", new Object[]{address, connectionState});

                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery: " + bluetoothGatt.discoverServices());
            }
            else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "Disconnected from GATT server.");

                connectionState = BleValues.STATE_DISCONNECTED;

                sendUpdate("onConnectionStateChanged", new Object[]{address, connectionState});
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.d(TAG, "onServicesDiscovered");

            String address = gatt.getDevice().getAddress();

            if (status == BluetoothGatt.GATT_SUCCESS) {
                List<BluetoothGattService> bleGattServices = gatt.getServices();
                bleDevice.setBluetoothServices(bleGattServices);

                // Show all the supported services and characteristics on the user interface.
                displayGattServices(address);

                // Stop Scan
                stopScan();

                sendUpdate("onServicesDiscovered", new Object[]{address, bleGattServices});
            }
            else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            Log.d(TAG, "onCharacteristicRead IN");

            // Retrieves address
            String address = gatt.getDevice().getAddress();

            // Retrieves uuid and value
            String uuid = characteristic.getUuid().toString();
            Log.d(TAG, "Read Characteristic UUID: " + uuid);

            byte[] value = characteristic.getValue();
            printGattValue(value);

            sendUpdate("onCharacteristicRead", new Object[]{address, uuid, value});
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic,
                                          int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.d(TAG, "onCharacteristicWrite");

            // Retrieves address
            String address = gatt.getDevice().getAddress();

            // Retrieves uuid and value
            String uuid = characteristic.getUuid().toString();
            Log.d(TAG, "Write Characteristic UUID: " + uuid);

            byte[] value = characteristic.getValue();
            printGattValue(value);

            sendUpdate("onCharacteristicWrite", new Object[]{address, uuid, value});
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            Log.d(TAG, "onCharacteristicChanged");

            // Retrieves address
            String address = gatt.getDevice().getAddress();

            // Retrieves uuid and value
            String uuid = characteristic.getUuid().toString();
            Log.d(TAG, "Characteristic UUID: " + uuid);

            byte[] value = characteristic.getValue();
            printGattValue(value);

            sendUpdate("onCharacteristicChanged", new Object[]{address, uuid, value});
        }


        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            Log.d(TAG, "onDescriptorWrite");

            // Retrieves address
            String address = gatt.getDevice().getAddress();

            // Retrieves uuid and value
            String uuid = descriptor.getUuid().toString();
            Log.d(TAG, "Characteristic UUID: " + uuid);

            byte[] value = descriptor.getValue();

            sendUpdate("onDescriptorWrite", new Object[]{address, uuid, value});

        }
    };
}





