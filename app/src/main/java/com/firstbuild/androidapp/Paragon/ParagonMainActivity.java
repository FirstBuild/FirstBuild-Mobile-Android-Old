package com.firstbuild.androidapp.Paragon;

import android.app.Fragment;
import android.app.FragmentManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.firstbuild.androidapp.ParagonValues;
import com.firstbuild.androidapp.R;
import com.firstbuild.androidapp.sousvideUI.ReadyToPreheatFragment;
import com.firstbuild.commonframework.bleManager.BleListener;
import com.firstbuild.commonframework.bleManager.BleManager;
import com.firstbuild.commonframework.bleManager.BleValues;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class ParagonMainActivity extends ActionBarActivity {
    private String TAG = ParagonMainActivity.class.getSimpleName();

    // Bluetooth adapter handler
    private BluetoothAdapter bluetoothAdapter = null;
    private ParagonSteps currentStep = ParagonSteps.STEP_COOKING_METHOD_1;
    private float targetTemp;
    private float currentTemp;
    private int targetTime;
    private byte batteryLevel;
    private Queue requestQueue = new LinkedList();
    private String REQUEST_METHOD_READ = "READ";
    private String REQUEST_METHOD_NOTIFICATION = "NOTIFICATION";
    private BleListener bleListener = new BleListener() {
        @Override
        public void onScanDevices(HashMap<String, BluetoothDevice> bluetoothDevices) {
            super.onScanDevices(bluetoothDevices);

            Log.d(TAG, "onScanDevices IN");

            Log.d(TAG, "bluetoothDevices size: " + bluetoothDevices.size());
            for (Map.Entry<String, BluetoothDevice> entry : bluetoothDevices.entrySet()) {

                // Retrieves address and name
                BluetoothDevice device = entry.getValue();
                String address = device.getAddress();
                String name = device.getName();

                Log.d(TAG, "------------------------------------");
                Log.d(TAG, "Device address: " + address);
                Log.d(TAG, "Device name: " + name);

                if (ParagonValues.TARGET_DEVICE_NAME.equals(name)) {
                    Log.d(TAG, "device found: " + device.getName());

                    // Connect to device
                    BleManager.getInstance().connect(address);

                    // Stop ble device scanning
                    BleManager.getInstance().stopScan();

                    nextStep(ParagonSteps.STEP_COOKING_METHOD_1);


                    break;
                }
            }
            Log.d(TAG, "====================================");
        }

        @Override
        public void onScanStateChanged(int status) {
            super.onScanStateChanged(status);

            Log.d(TAG, "[onScanStateChanged] status: " + status);

            if (status == BleValues.START_SCAN) {
                Log.d(TAG, "Scanning BLE devices");
            }
            else {
                Log.d(TAG, "Stop scanning BLE devices");
            }
        }

        @Override
        public void onConnectionStateChanged(final String address, final int status) {
            super.onConnectionStateChanged(address, status);

            Log.d(TAG, "[onConnectionStateChanged] address: " + address + ", status: " + status);
        }

        @Override
        public void onServicesDiscovered(String address, List<BluetoothGattService> bleGattServices) {
            super.onServicesDiscovered(address, bleGattServices);

            Log.d(TAG, "[onServicesDiscovered] address: " + address);

            BleManager.getInstance().displayGattServices(address);

            // Get Initial values.
            requestQueue.offer(REQUEST_METHOD_READ + "/" + ParagonValues.CHARACTERISTIC_TARGET_TEMPERATURE);
            requestQueue.offer(REQUEST_METHOD_READ + "/" + ParagonValues.CHARACTERISTIC_BATTERY_LEVEL);

//            // Set notification
            requestQueue.offer(REQUEST_METHOD_NOTIFICATION + "/" + ParagonValues.CHARACTERISTIC_BATTERY_LEVEL);
            requestQueue.offer(REQUEST_METHOD_NOTIFICATION + "/" + ParagonValues.CHARACTERISTIC_CURRENT_TEMPERATURE);
//            BleManager.getInstance().setCharacteristicNotification(ParagonValues.CHARACTERISTIC_BATTERY_LEVEL, true);
//            BleManager.getInstance().setCharacteristicNotification(ParagonValues.CHARACTERISTIC_CURRENT_TEMPERATURE, true);

            nextCharacteristicRead();
        }

        @Override
        public void onCharacteristicRead(String address, String uuid, byte[] value) {
            super.onCharacteristicRead(address, uuid, value);

            Log.d(TAG, "[onCharacteristicRead] address: " + address + ", uuid: " + uuid);

            onReceivedData(uuid, value);

            nextCharacteristicRead();
        }

        @Override
        public void onCharacteristicWrite(String address, String uuid, byte[] value) {
            super.onCharacteristicWrite(address, uuid, value);

            Log.d(TAG, "[onCharacteristicWrite] address: " + address + ", uuid: " + uuid);
        }

        @Override
        public void onCharacteristicChanged(String address, String uuid, byte[] value) {
            super.onCharacteristicChanged(address, uuid, value);

            Log.d(TAG, "[onCharacteristicChanged] address: " + address + ", uuid: " + uuid);

            onReceivedData(uuid, value);

            nextCharacteristicRead();
        }

        @Override
        public void onDescriptorWrite(String address, String uuid, byte[] value) {
            super.onDescriptorWrite(address, uuid, value);

            Log.d(TAG, "[onDescriptorWrite] address: " + address + ", uuid: " + uuid);
        }
    };

    private void nextCharacteristicRead() {

        String nextRequest = (String) requestQueue.poll();

        if (nextRequest != null) {
            String method = nextRequest.split("/")[0];
            String characteristic = nextRequest.split("/")[1];

            if (method.equals(REQUEST_METHOD_READ)) {
                BleManager.getInstance().readCharacteristics(characteristic);
            }
            else if (method.equals(REQUEST_METHOD_NOTIFICATION)) {
                BleManager.getInstance().setCharacteristicNotification(characteristic, true);
            }
            else {

            }

        }
    }

    public int getTargetTime() {
        return targetTime;
    }

    public void setTargetTime(int targetTime) {
        this.targetTime = targetTime;
    }

    public float getTargetTemp() {
        return targetTemp;
    }

    public void setTargetTemp(int targetTemp) {
        this.targetTemp = targetTemp;
    }

    public float getCurrentTemp() {
        return currentTemp;
    }

    private void onReceivedData(String uuid, byte[] value) {

        Log.d(TAG, "onReceivedData :" + uuid);

        ByteBuffer byteBuffer = ByteBuffer.wrap(value);


        switch (uuid.toUpperCase()) {
            case ParagonValues.CHARACTERISTIC_CURRENT_TEMPERATURE:
                Log.d(TAG, "CHARACTERISTIC_CURRENT_TEMPERATURE");

                currentTemp = (byteBuffer.getShort() / 100.0f);

                Log.d(TAG, "currentTemp is :" + currentTemp);


                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Fragment fragment = getFragmentManager().findFragmentById(R.id.frame_content);

                                if (fragment instanceof PreheatingFragment) {
                                    ((PreheatingFragment) fragment).updateUiCurrentTemp();
                                }
                                else {

                                }

                            }
                        });
                    }
                }).start();
                break;

            case ParagonValues.CHARACTERISTIC_TARGET_TEMPERATURE:
                Log.d(TAG, "CHARACTERISTIC_TARGET_TEMPERATURE");
                targetTemp = (float) byteBuffer.getShort() / 100.0f;

                Log.d(TAG, "target temp is :" + targetTemp);
                break;

            case ParagonValues.CHARACTERISTIC_BATTERY_LEVEL:
                Log.d(TAG, "CHARACTERISTIC_BATTERY_LEVEL");
                batteryLevel = byteBuffer.get();

                Log.d(TAG, "battery level is :" + batteryLevel);
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paragon_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);


        // Use this check to determine whether BLE is supported on the device. Then
        // you can selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Log.d(TAG, "BLE is not supported - Stop activity!");

            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
        else {
            // Initializes Bluetooth adapter.
            final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            bluetoothAdapter = bluetoothManager.getAdapter();

            // Checks if Bluetooth is supported on the device.
            if (bluetoothAdapter == null) {
                Log.d(TAG, "Bluetooth is not supported - Stop activity!");

                Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
                finish();
            }
            else {
                // Do nothing
            }

        }

        // Initialize ble manager
        BleManager.getInstance().initBleManager(this);

        // Add ble event listener
        BleManager.getInstance().addListener(bleListener);


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        FragmentManager fm = getFragmentManager();

        if (fm.getBackStackEntryCount() > 1) {
            fm.popBackStack();
        }
        else {
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_paragon_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Check bluetooth adapter. If the adapter is disabled, enable it
        boolean result = BleManager.getInstance().isBluetoothEnabled();

        if (!result) {
            Log.d(TAG, "Bluetooth adapter is disabled. Enable bluetooth adapter.");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, BleValues.REQUEST_ENABLE_BT);
        }
        else {
            Log.d(TAG, "Bluetooth adapter is already enabled. Start scanning.");
            BleManager.getInstance().startScan();
        }

    }

    public void nextStep(ParagonSteps step) {
        Fragment fragment = null;

        switch (step) {
            case STEP_COOKING_METHOD_1:
                fragment = new Step1Fragment();
                break;

            case STEP_COOKING_METHOD_2:
                fragment = new Step2Fragment();
                break;

            case STEP_SOUSVIDE_BEEF:
                fragment = new BeefFragment();
                break;

            case STEP_SOUSVIDE_READY_PREHEAT:
                fragment = new ReadyToPreheatFragment();
                break;

            default:
                break;

        }

        if (fragment != null) {
            getFragmentManager().
                    beginTransaction().
                    replace(R.id.frame_content, fragment).
                    addToBackStack(null).
                    commit();
        }
        else {

        }


    }

    public enum ParagonSteps {
        STEP_COOKING_METHOD_1,
        STEP_COOKING_METHOD_2,
        STEP_SOUSVIDE_BEEF,
        STEP_SOUSVIDE_READY_PREHEAT,
        STEP_SOUSVIDE_PREHEATING,
        STEP_SOUSVIDE_READY_COOK,
        STEP_SOUSVIDE_COOKING,
    }


}
