/**
 * @file BleManager.java
 * @brief BleManager handles all kind of interface with ble devices
 * @author Ryan Lee (strike77@gmail.com)
 * @date May/22/2015
 */

package com.firstbuild.commonframework.blemanager;

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
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.firstbuild.androidapp.productmanager.ProductManager;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BleManager {

    public final static String CLIENT_CONFIGURATION_UUID = "00002902-0000-1000-8000-00805f9b34fb";
    public static BleManager instance = new BleManager();
    private final String TAG = getClass().getSimpleName();
    // Flag for checking device scanning state
    public boolean isScanning = false;
    private HashMap<String, BluetoothDevice> scannedDevices = new HashMap<String, BluetoothDevice>();
    // Bluetooth adapter handler
    private BluetoothAdapter bluetoothAdapter = null;
    private BluetoothManager bluetoothManager = null;
    // Post Delayed handler
    private Handler handler = new Handler();
    private Runnable stopScanRunnable = null;
    private Context context = null;
    private HashMap<String, BleListener> callbacks = null;
    private HashMap<String, BluetoothGatt> connectedGatts = new HashMap<>(); // contains connected Gatt server
    private LinkedList<BleOperation> operations = new LinkedList<>();
    private AsyncTask<Void, Void, Void> currentOperationTimeout;
    private BleOperation currentOperation = null;
    /**
     * Device scan callback
     */
    private BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice bluetoothDevice, int rssi, byte[] scanRecord) {
            try {

                if (bluetoothDevice != null && bluetoothDevice.getName() != null) {
                    scannedDevices.put(bluetoothDevice.getAddress(), bluetoothDevice);

                    // Notify updates to other modules
                    sendUpdate("onScanDevices", new Object[]{scannedDevices});
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    public BleManager() {
        // Default constructor
    }

    public static BleManager getInstance() {
        return instance;
    }

    /**
     * Initialize BleManager
     *
     * @param context activity's context
     */
    public void initBleManager(Context context) {
        Log.d(TAG, "initBleManager IN");
        if (this.context == null) {
            this.context = context;
            bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);

            // Get bluetooth adaptor
            bluetoothAdapter = bluetoothManager.getAdapter();

        }
    }

    /**
     * Add a listener to subscribe ble event
     *
     * @param listener listener instance
     */
    public void addListener(BleListener listener) {
        if (callbacks == null) {
            callbacks = new HashMap<String, BleListener>();
        }
        else {
            // Do nothing
        }

        // Key is listener is casted string
        // Value is listener itself.
        callbacks.put(listener.toString(), listener);
    }

    /**
     * Remove a listener for ble event
     *
     * @param listener listener instance
     */
    public void removeListener(BleListener listener) {
        if (callbacks != null && !callbacks.isEmpty()) {
            callbacks.remove(listener.toString());
            Log.d(TAG, "Remove Listener: " + listener);
        }
        else {
            // Do nothing
        }
    }

    /**
     * Connect to a ble device
     *
     * @return connection success or failed
     */
    public BluetoothDevice connect(final String address) {
        Log.d(TAG, "connect IN");

        if (bluetoothAdapter == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return null;
        }

        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);

        Log.d(TAG, "Trying to create a new connection.");
        return device;
    }

    /**
     * Check bluetooth feature in the phone is enabled
     *
     * @return enabled or disabled
     */
    public boolean isBluetoothEnabled() {
        Log.d(TAG, "isBluetoothEnabled IN");

        if(bluetoothAdapter == null){
            return false;
        }

        return bluetoothAdapter.isEnabled();
    }




    /**
     * start device scan for duration
     *
     * @param duration 1 to 120 sec.
     */
    public boolean startScan(final int duration) {
        Log.d(TAG, "startScan IN");

        boolean result = false;

        if (duration > 0 && duration <= 120) {
            // Check duration
            isScanning = true;
            // TODO: hans 16. 5. 31. consider replacing this with startScan(List, ScanSettings, ScanCallback)
            bluetoothAdapter.startLeScan(leScanCallback);

            // Stops scanning after a pre-defined scan period.
            handler.postDelayed(setStopScanRunnable(), duration);

            result = true;
            sendUpdate("onScanStateChanged", new Object[]{BleValues.START_SCAN});
        }
        else {
            Log.d(TAG, "duration is out of range(1 - 120 sec): " + duration);
            result = false;
        }

        return result;
    }

    private Runnable setStopScanRunnable() {
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
     * Send update to subscribers
     *
     * @param listener subscriber
     * @param args     corresponding arguments
     */
    public void sendUpdate(String listener, Object... args) {
        Log.d(TAG, "sendUpdate IN");


        if (listener.equals("onCharacteristicChanged") ||
                listener.equals("onCharacteristicRead") ) {
            ProductManager.getInstance().updateErd((String) args[0], (String) args[1], (byte[]) args[2]);
        }
        else {
            // Do nothing
        }


        // Clone hashmap to avoid java.util.ConcurrentModificationException
        HashMap<String, BleListener> callbackClone = (HashMap) callbacks.clone();
        for (Map.Entry<String, BleListener> entry : callbackClone.entrySet()) {
            BleListener callback = entry.getValue();
            Log.d(TAG, "sendUpdate LOOP");

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
            else if (callback != null && listener.equals("onCharacteristicChanged")) {
                callback.onCharacteristicChanged((String) args[0], (String) args[1], (byte[]) args[2]);
            }
            else if (callback != null && listener.equals("onCharacteristicRead")) {
                callback.onCharacteristicRead((String) args[0], (String) args[1], (byte[]) args[2]);
            }
            else if (callback != null && listener.equals("onCharacteristicWrite")) {
                callback.onCharacteristicWrite((String) args[0], (String) args[1], (byte[]) args[2]);
            }
            else if (callback != null && listener.equals("onDescriptorWrite")) {
                callback.onDescriptorWrite((String) args[0], (String) args[1], (byte[]) args[2]);
            }
            else {
                // Do nothing
            }
        }

        Log.d(TAG, "sendUpdate OUT");
    }

    /**
     * Stop scanning BLE devices
     */
    public void stopScan() {
        Log.d(TAG, "stopScan IN");

        isScanning = false;
        bluetoothAdapter.stopLeScan(leScanCallback);

        // Stop postDelayed method
        if (stopScanRunnable != null) {
            Log.d(TAG, "Remove delayed stopScan task");
            handler.removeCallbacks(stopScanRunnable);
        }

        sendUpdate("onScanStateChanged", new Object[]{BleValues.STOP_SCAN});
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
     * Print GATT services and characteristics
     */
    public void displayGattServices(BluetoothGatt gatt) {
        Log.d(TAG, "displayGattServices IN");

        List<BluetoothGattService> bleGattServices = gatt.getServices();

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

                    if (gattCharacteristic.getValue() != null) {
                        printGattValue(gattCharacteristic.getValue());
                    }


                }

                Log.d(TAG, "=====================================");
            }
        }
    }

    /**
     * Add ReadChracteristics operation to the queue.
     *
     * @param device              BluetoothDevice object.
     * @param characteristicsUuid UUID to read.
     */
    public void readCharacteristics(BluetoothDevice device, String characteristicsUuid) {
        Log.d(TAG, "operations.add readCharacteristics");
        operations.add(new BleOperationReadCharacteristics(device, characteristicsUuid));

        doOperation();
    }

    /**
     * Add WriteChracteristics operation to the queue.
     *
     * @param device              BluetoothDevice object.
     * @param characteristicsUuid UUID to write.
     * @param values              actual value to write.
     */
    public void writeCharacteristics(BluetoothDevice device, String characteristicsUuid, byte[] values) {
        Log.d(TAG, "operations.add writeCharacteristics");
        operations.add(new BleOperationWriteCharateristics(device, characteristicsUuid, values));

        doOperation();
    }

    /**
     * Add Notification operation to the queue.
     *
     * @param device              BluetoothDevice object.
     * @param characteristicsUuid UUID for notification.
     * @param isEnabled           enable/disable of notification.
     */
    public void setCharacteristicNotification(BluetoothDevice device, String characteristicsUuid, boolean isEnabled) {
        Log.d(TAG, "operations.add setCharacteristicNotification");
        operations.add(new BleOperationSetNotification(device, characteristicsUuid, isEnabled));

        doOperation();
    }

    /**
     * Add Disconnect operation to the queue.
     *
     * @param device BluetoothDevice object.
     */
    public void disconnect(BluetoothDevice device) {
        Log.d(TAG, "operations.add disconnect");
        operations.add(new BleOperationDisconnect(device));

        doOperation();
    }

    /**
     * Cancel current operation due to expired timer.
     */
    public synchronized void cancelCurrentOperation() {
        Log.d(TAG, "cancelCurrentOperation ****************");

        if(currentOperation != null) {
            LinkedList<BleOperation> tempOperations = new LinkedList<>();

            for (Iterator<BleOperation> iterator = operations.iterator(); iterator.hasNext();) {
                BleOperation operation = iterator.next();

                if(operation.getDevice() == currentOperation.getDevice()) {
                    Log.d(TAG, "cancelCurrentOperation removed operations has same device :"+currentOperation.getDevice().getAddress());
                    tempOperations.add(operation);
                    iterator.remove();
                }
            }

            for( BleOperation tempOperation : tempOperations){
                operations.add(tempOperation);
            }

            operations.add(currentOperation);
        }

        currentOperation = null;
        doOperation();
    }

    /**
     * Pick the next operation from the queue and execute.
     */
    private synchronized void doOperation() {
        if (currentOperation != null) {
            Log.d(TAG, "tried to doOperation, but currentOperation was not null, " + currentOperation);
            return;
        }
        if (operations.size() == 0) {
            Log.d(TAG, "Queue empty, doOperation loop stopped.");
            currentOperation = null;
            return;
        }

        final BleOperation operation = operations.poll();
        final BluetoothDevice device = operation.getDevice();

        Log.d(TAG, "Driving Gatt queue, size will now : " + operations.size());
        setCurrentOperation(operation);

        if (currentOperationTimeout != null) {
            Log.d(TAG, "Good to cancel timer and go to next operation since we got call back before the time out");
            currentOperationTimeout.cancel(true);
        }

        if (!connectedGatts.containsKey(device.getAddress())) {
            operation.setTimeoutTime(BleOperation.CONNECT_TIMEOUT_IN_MILLIS);
        }

        currentOperationTimeout = new AsyncTask<Void, Void, Void>() {
            @Override
            protected synchronized Void doInBackground(Void... voids) {
                try {
                    Log.d(TAG, "Starting to do a background timeout");
                    wait(operation.getTimoutTime());
                }
                catch (InterruptedException e) {
                    Log.d(TAG, "was interrupted out of the timeout");
                }

                if (isCancelled()) {
                    Log.d(TAG, "The timeout was cancelled, so we do nothing.");
                    return null;
                }

                Log.d(TAG, "Timeout ran to completion, time to Abort!!!!!!!!!!!!!");
                cancelCurrentOperation();
                return null;
            }

            @Override
            protected synchronized void onCancelled() {
                super.onCancelled();
                notify();
            }
        }.execute();


        if (connectedGatts.containsKey(device.getAddress())) {
            Log.d(TAG, "found address in connectedGatts");
            executeOperation(connectedGatts.get(device.getAddress()), operation);
        }
        else {
            Log.d(TAG, "not found address in connectedGatts");
            device.connectGatt(context, true, new BluetoothGattCallback() {
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                    super.onConnectionStateChange(gatt, status, newState);
                    Log.d(TAG, "onConnectionStateChange IN");

                    String address = gatt.getDevice().getAddress();

                    if (status == 133) {
                        Log.d(TAG, "onConnectionStateChange " + status + ", this device might be off!!!");
                        gatt.close();
                        if (connectedGatts.containsKey(address)) {
                            connectedGatts.remove(address);
                        }

                        sendUpdate("onConnectionStateChanged", new Object[]{address, BluetoothProfile.STATE_DISCONNECTED});
                        return;
                    }

                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        Log.i(TAG, "Connected to GATT server. " + address);

                        connectedGatts.put(address, gatt);
                        gatt.discoverServices();
                    }
                    else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        Log.i(TAG, "Disconnected from GATT server." + address);


                        if (connectedGatts.containsKey(address)) {
                            connectedGatts.remove(address);
                        }

                        setCurrentOperation(null);
                        gatt.close();

                        doOperation();
                    }

                    sendUpdate("onConnectionStateChanged", new Object[]{address, newState});
                }

                @Override
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    super.onServicesDiscovered(gatt, status);
                    String address = gatt.getDevice().getAddress();

                    Log.d(TAG, "onServicesDiscovered " + address);

                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        displayGattServices(gatt);
                        List<BluetoothGattService> bleGattServices = gatt.getServices();

                        sendUpdate("onServicesDiscovered", new Object[]{address, bleGattServices});
                        executeOperation(gatt, operation);
                    }
                    else {
                        Log.d(TAG, "onServicesDiscovered NOT GATT_SUCCESS: " + status);
                    }
                }

                @Override
                public void onCharacteristicRead(BluetoothGatt gatt,
                                                 BluetoothGattCharacteristic characteristic,
                                                 int status) {
                    super.onCharacteristicRead(gatt, characteristic, status);
                    Log.d(TAG, "onCharacteristicRead IN");

                    // Retrieves address
                    String address = gatt.getDevice().getAddress();

                    // Retrieves uuid and value
                    String uuid = characteristic.getUuid().toString();
                    Log.d(TAG, "Read Characteristic UUID: " + uuid);

                    byte[] value = characteristic.getValue();
                    if (value != null) {
                        printGattValue(value);
                    }

                    sendUpdate("onCharacteristicRead", new Object[]{address, uuid, value});

                    setCurrentOperation(null);
                    doOperation();
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

                    setCurrentOperation(null);
                    doOperation();
                }

                @Override
                public void onCharacteristicChanged(BluetoothGatt gatt,
                                                    BluetoothGattCharacteristic characteristic) {
                    super.onCharacteristicChanged(gatt, characteristic);
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

                    setCurrentOperation(null);
                    doOperation();
                }

            });
        }
    }

    /**
     * Execute given operation.
     *
     * @param bluetoothGatt BluetoothDevice object.
     * @param operation     BleOperation object.
     */
    private void executeOperation(BluetoothGatt bluetoothGatt, BleOperation operation) {
        Log.d(TAG, "executeOperation IN");
        if (operation != currentOperation) {
            Log.d(TAG, "current operation is not null");
            return;
        }

        Log.d(TAG, "execute operation");
        operation.execute(bluetoothGatt);

        if (!operation.hasCallback()) {
            setCurrentOperation(null);
            doOperation();
        }
    }

    /**
     * Put given operation into currentOperation
     *
     * @param operation BleOperation object.
     */
    private synchronized void setCurrentOperation(BleOperation operation) {
        this.currentOperation = operation;
    }

    public void printGattValue(byte[] values) {
        StringBuilder hexValue = new StringBuilder();

        // Print value in hexa decimal format
        System.out.print("Value: ");
        for (byte value : values) {
            hexValue.append(String.format("0x%02x ", value));
        }

        System.out.println(hexValue);
    }

    public boolean setDeviceName(String name) {
        boolean result = false;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter != null) {
            bluetoothAdapter.setName(name);

            result = true;
        }

        return result;
    }

    public String getDeviceName() {
        String result = null;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null) {
            result = bluetoothAdapter.getName();
        }

        Log.d(TAG, "device name: " + result);

        return result;
    }


    public void pairDevice(BluetoothDevice device) {
        Log.d(TAG, "pairDevice IN");

        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        Log.d(TAG, "pairDevice OUT");

    }


    public boolean unpair(final String address) {
        boolean result = false;

        if (address != null) {

            // Retrieves paired device list
            Set<BluetoothDevice> pairedDevice = bluetoothAdapter.getBondedDevices();
            if (pairedDevice != null && pairedDevice.size() > 0) {

                // Iterate all the device in the list
                for (BluetoothDevice bluetoothDevice : pairedDevice) {
                    Log.d(TAG, bluetoothDevice.getName() + "\n" + bluetoothDevice.getAddress());

                    if (bluetoothDevice.getAddress().equals(address)) {

                        // Device found
                        unpairDevice(bluetoothDevice);
                        result = true;
                    }
                }
            }
        }

        return result;
    }


    public void unpairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("removeBond", (Class[]) null);
            method.invoke(device, (Object[]) null);

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}





