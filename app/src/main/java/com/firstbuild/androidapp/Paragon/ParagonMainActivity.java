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

    Toolbar toolbar;

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

                    //TODO: block below code for test.
//                    nextStep(ParagonSteps.STEP_COOKING_METHOD_1);
                    nextStep(ParagonSteps.STEP_COOKING_MODE);


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
            } else {
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
            BleManager.getInstance().readCharacteristics(ParagonValues.CHARACTERISTIC_TARGET_TEMPERATURE);
            BleManager.getInstance().readCharacteristics(ParagonValues.CHARACTERISTIC_BATTERY_LEVEL);
            BleManager.getInstance().readCharacteristics(ParagonValues.CHARACTERISTIC_BURNER_STATUS);
            BleManager.getInstance().readCharacteristics(ParagonValues.CHARACTERISTIC_ELAPSED_TIME);

            BleManager.getInstance().setCharacteristicNotification(ParagonValues.CHARACTERISTIC_BATTERY_LEVEL, true);
            BleManager.getInstance().setCharacteristicNotification(ParagonValues.CHARACTERISTIC_CURRENT_TEMPERATURE, true);
            BleManager.getInstance().setCharacteristicNotification(ParagonValues.CHARACTERISTIC_ELAPSED_TIME, true);
            BleManager.getInstance().setCharacteristicNotification(ParagonValues.CHARACTERISTIC_BURNER_STATUS, true);
        }

        @Override
        public void onCharacteristicRead(String address, String uuid, byte[] value) {
            super.onCharacteristicRead(address, uuid, value);

            Log.d(TAG, "[onCharacteristicRead] address: " + address + ", uuid: " + uuid);

            onReceivedData(uuid, value);

//            nextCharacteristicRead();
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
    private int MAX_BURNER = 5;

    private void nextCharacteristicRead() {

        String nextRequest = (String) requestQueue.poll();

        if (nextRequest != null) {
            String method = nextRequest.split("/")[0];
            String characteristic = nextRequest.split("/")[1];

            if (method.equals(REQUEST_METHOD_READ)) {
                BleManager.getInstance().readCharacteristics(characteristic);
            } else if (method.equals(REQUEST_METHOD_NOTIFICATION)) {
                BleManager.getInstance().setCharacteristicNotification(characteristic, true);
            } else {

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

    public void setTargetTemp(float targetTemp) {
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

                                if (fragment instanceof SousvideStatusFragment) {
                                    ((SousvideStatusFragment) fragment).updateUiCurrentTemp();

//                                    if(currentTemp >= targetTemp){
//                                        nextStep(ParagonSteps.STEP_SOUSVIDE_READY_COOK);
//                                    }
//                                    else{
//                                        //do nothing
//                                    }
                                } else {
                                    //do nothing
                                }

                            }
                        });
                    }
                }).start();
                break;

            case ParagonValues.CHARACTERISTIC_TARGET_TEMPERATURE:
                targetTemp = (float) byteBuffer.getShort() / 100.0f;

                Log.d(TAG, "CHARACTERISTIC_TARGET_TEMPERATURE :" + targetTemp);
                break;

            case ParagonValues.CHARACTERISTIC_BATTERY_LEVEL:
                batteryLevel = byteBuffer.get();

                Log.d(TAG, "CHARACTERISTIC_BATTERY_LEVEL :" + batteryLevel);


                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
//                                ((TextView) toolbar.findViewById(R.id.text_battery_level)).setText(batteryLevel + "%");
                            }
                        });
                    }
                }).start();
                break;

            case ParagonValues.CHARACTERISTIC_ELAPSED_TIME:
                Log.d(TAG, "CHARACTERISTIC_ELAPSED_TIME :" + byteBuffer.getShort());

                break;


            case ParagonValues.CHARACTERISTIC_BURNER_STATUS:
                Log.d(TAG, "CHARACTERISTIC_BURNER_STATUS :" +
                        String.format("%02x", value[0]) + ", " +
                        String.format("%02x", value[1]) + ", " +
                        String.format("%02x", value[2]) + ", " +
                        String.format("%02x", value[3]) + ", " +
                        String.format("%02x", value[4]));

                boolean isSousVide = false;
                boolean isPreheat = false;

                for (int i = 0; i < MAX_BURNER; i++) {
                    if (getBit(value[i], 6)) {
                        isSousVide = true;
                        isPreheat = getBit(value[i], 5);

                        break;
                    }
                }

                Log.d(TAG, "isSousVid : "+isSousVide + ", isPreheat : " +isPreheat);

                final boolean isFinalPreaheat = isPreheat;

                if (isSousVide) {
                    Fragment fragment = getFragmentManager().findFragmentById(R.id.frame_content);

                    if (fragment instanceof GetReadyFragment) {

                        nextStep(ParagonSteps.STEP_SOUSVIDE_CIRCLE);

                    } else if (fragment instanceof SousvideStatusFragment) {

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Fragment fragment = getFragmentManager().findFragmentById(R.id.frame_content);

                                        ((SousvideStatusFragment) fragment).updateCookStatus(isFinalPreaheat);

                                    }
                                });
                            }
                        }).start();

                    } else {

                    }


                } else {
                    //do nothging.
                }


                break;
        }
    }

    boolean getBit(int value, int bit) {
        return (value & (1 << bit)) != 0;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paragon_main);

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        // Use this check to determine whether BLE is supported on the device. Then
        // you can selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Log.d(TAG, "BLE is not supported - Stop activity!");

            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        } else {
            // Initializes Bluetooth adapter.
            final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            bluetoothAdapter = bluetoothManager.getAdapter();

            // Checks if Bluetooth is supported on the device.
            if (bluetoothAdapter == null) {
                Log.d(TAG, "Bluetooth is not supported - Stop activity!");

                Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                // Do nothing
            }

        }

        // Initialize ble manager
        BleManager.getInstance().initBleManager(this);

        // Add ble event listener
        BleManager.getInstance().addListener(bleListener);

        //TODO: remove below code after test.
//        nextStep(ParagonSteps.STEP_COOKING_MODE);


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        FragmentManager fm = getFragmentManager();

        if (fm.getBackStackEntryCount() > 1) {
            fm.popBackStack();
        } else {
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
        } else {
            Log.d(TAG, "Bluetooth adapter is already enabled. Start scanning.");
            BleManager.getInstance().startScan();
        }

    }

    /**
     * @param step
     */
    public void nextStep(ParagonSteps step) {
        Fragment fragment = null;

        switch (step) {
            case STEP_COOKING_MODE:
                fragment = new SelectModeFragment();
                break;

            case STEP_SOUSVIDE_SETTINGS:
                fragment = new SettingsFragment();
                break;

            case STEP_SOUSVIDE_GETREADY:
                fragment = new GetReadyFragment();
                break;

            case STEP_SOUSVIDE_CIRCLE:
                fragment = new SousvideStatusFragment();
                break;

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

            case STEP_SOUSVIDE_READY_COOK:
                fragment = new ReadyToCookFragment();
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
        } else {

        }


    }

    public enum ParagonSteps {
        STEP_COOKING_MODE,
        STEP_SOUSVIDE_SETTINGS,
        STEP_SOUSVIDE_GETREADY,
        STEP_SOUSVIDE_CIRCLE,
        STEP_COOKING_METHOD_1,
        STEP_COOKING_METHOD_2,
        STEP_SOUSVIDE_BEEF,
        STEP_SOUSVIDE_READY_PREHEAT,
        STEP_SOUSVIDE_PREHEATING,
        STEP_SOUSVIDE_READY_COOK,
        STEP_SOUSVIDE_COOKING,
    }


}
