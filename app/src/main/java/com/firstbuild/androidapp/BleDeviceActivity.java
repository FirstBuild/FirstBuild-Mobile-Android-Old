package com.firstbuild.androidapp;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.firstbuild.ble.BluetoothLeManager;
import com.firstbuild.ble.BluetoothListener;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BleDeviceActivity extends ActionBarActivity {
    private final String TAG = BluetoothLeManager.class.getSimpleName();
    private TextView deviceName = null;
    private TextView deviceAddress = null;
    private TextView currentTemp = null;
    private NumberPicker setTemperaturePicker = null;
    private Button connectButton = null;
    private Button setButton = null;

    private BluetoothLeManager bluetoothLeManager = null;
    private static BluetoothDevice device = null;
    private static boolean bPaired = false;

    private List<BluetoothGattCharacteristic> characteristics = null;
    private Timer readTimer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_device);

        device = getIntent().getParcelableExtra("BLE_DEVICE");
        Log.d(TAG, "device address: " + device.getAddress() + ", name: " + device.getName());

        deviceName = (TextView) findViewById(R.id.ble_device_name);
        deviceAddress = (TextView) findViewById(R.id.ble_device_address);
        connectButton = (Button) findViewById(R.id.ble_connect_button);
        setButton = (Button) findViewById(R.id.set_button);
        setTemperaturePicker = (NumberPicker) findViewById(R.id.set_temp_picker);
        currentTemp = (TextView) findViewById(R.id.current_temp_value);

        deviceName.setText(device.getName());
        deviceAddress.setText(device.getAddress());

        connectButton.setOnClickListener(connectButtonClickListener);
        setButton.setOnClickListener(setButtonClickListener);

        // Prevent app from rotating
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Set temp picker
        setTemperaturePicker.setMinValue(50);
        setTemperaturePicker.setMaxValue(200);
        setTemperaturePicker.setValue(120);
        setTemperaturePicker.setWrapSelectorWheel(false);

        setButton.setEnabled(false);

        setTemperaturePicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {

            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                Log.d(TAG, "old Value: " + oldVal + ", new Value: " + newVal);
            }
        });

        // Register Observer
        bluetoothLeManager = BluetoothLeManager.getSharedObject();
        bluetoothLeManager.addListener(onConnectListener);
        bluetoothLeManager.addListener(onServicesListener);
        bluetoothLeManager.addListener(onMessageListener);
        bluetoothLeManager.addListener(onPairingListener);
    }

    private void TimerMethod(){
        Log.d(TAG, "In TimerMethod");
        bluetoothLeManager.readData("f000ffc2-0451-4000-b000-000000000000");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_ble_device, menu);
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
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        bPaired = false;
        super.onDestroy();
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private final BluetoothListener onConnectListener = new BluetoothListener(){
        public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState){
            Log.d(TAG, "onConnectionListener");
            if(BluetoothProfile.STATE_CONNECTED == newState){
                Log.d(TAG, "Connected to " + device.getAddress() + " and Request Service");
                gatt.discoverServices();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        connectButton.setEnabled(false);
                        setButton.setEnabled(true);
                        setButton.setBackgroundColor(Color.parseColor("#673AB7"));
                        showToast("Connected");
                    }
                });

                readTimer = new Timer();
                readTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        TimerMethod();
                    }

                }, 0, 3000);
            }
        }
    };

    private final BluetoothListener onServicesListener = new BluetoothListener(){
        public void onServicesDiscovered(final BluetoothGatt gatt, final int status){
            Log.d(TAG, "In onServicesDiscovered");

            // this will get called after the client initiates a BluetoothGatt.discoverServices() call
            List<BluetoothGattService> services = bluetoothLeManager.getBluetootheServices();
            for (BluetoothGattService service : services) {
                Log.d(TAG, "service: " + service.getUuid().toString());
                Log.d(TAG, "Type: " + service.getType());

                Log.d(TAG, "--------------------------------");

                characteristics = service.getCharacteristics();

                for(BluetoothGattCharacteristic c : characteristics){
                    Log.d(TAG, "uuid: " + c.getUuid().toString());
                    Log.d(TAG, "permissions: " + c.getPermissions());
                    Log.d(TAG, "properties: " + c.getProperties());
                    Log.d(TAG, "writeType: " + c.getWriteType());
                    Log.d(TAG, "Value: " + c.getValue());
                }
                Log.d(TAG, "================================");
            }
        }
    };

    private final BluetoothListener onMessageListener = new BluetoothListener(){
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         final BluetoothGattCharacteristic characteristic,
                                         int status){
            Log.d(TAG, "In onReadData - status: " + status);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          final BluetoothGattCharacteristic characteristic,
                                          int status){
            Log.d(TAG, "In onWriteData - status: " + status);
        }
    };

    private final BluetoothListener onPairingListener = new BluetoothListener() {
        @Override
        public void onPairing(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                final int prevState	= intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);

                if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
                    bluetoothLeManager.connect(getApplicationContext(), device);
                } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED){
                    bPaired = false;
                }
            }
        }
    };

    private final View.OnClickListener connectButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View arg0) {
            Log.d(TAG, "Connect button clicked! - isPaired: " + bPaired);

//            bluetoothLeManager.pairing(getApplicationContext(), device);
            bluetoothLeManager.connect(getApplicationContext(), device);
        }
    };

    private final View.OnClickListener setButtonClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View arg0) {
            Integer value = setTemperaturePicker.getValue();

            byte[] sendValue = new byte[2];
            sendValue[0] = 0x00;
            sendValue[1] = value.byteValue();

            bluetoothLeManager.writeData(sendValue, "f000ffc2-0451-4000-b000-000000000000");
        }
    };
}


