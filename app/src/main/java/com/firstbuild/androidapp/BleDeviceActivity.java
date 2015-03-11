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

import java.lang.reflect.Method;
import java.util.List;


@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BleDeviceActivity extends ActionBarActivity {
    private final String TAG = "BleDeviceActivity";
    private TextView deviceName = null;
    private TextView deviceAddress = null;
    private NumberPicker setTemperaturePicker = null;
    private Button connectButton = null;
    private static BluetoothDevice device = null;
    private static BluetoothGatt bleGatt = null;
    private static boolean bPaired = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_device);

        device = getIntent().getParcelableExtra("BLE_DEVICE");
        Log.d(TAG, "device address: " + device.getAddress() + ", name: " + device.getName());

        deviceName = (TextView) findViewById(R.id.ble_device_name);
        deviceAddress = (TextView) findViewById(R.id.ble_device_address);
        connectButton = (Button) findViewById(R.id.ble_connect_button);
        setTemperaturePicker = (NumberPicker) findViewById(R.id.set_temp_picker);

        deviceName.setText(device.getName());
        deviceAddress.setText(device.getAddress());

        connectButton.setOnClickListener(connectButtonClickListener);

        // Set temp picker
        setTemperaturePicker.setMinValue(50);
        setTemperaturePicker.setMaxValue(200);
        setTemperaturePicker.setValue(100);
        setTemperaturePicker.setWrapSelectorWheel(false);

        setTemperaturePicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {

            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                Log.d(TAG, "old Value: " + oldVal + ", new Value: " + newVal);
            }
        });


        registerReceiver(mPairReceiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
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
        unregisterReceiver(mPairReceiver);

        super.onDestroy();
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void pairDevice(BluetoothDevice device) {
        Log.d(TAG, "In pairing - device: " + device);

        try {
            showToast("Pairing...");
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void unpairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("removeBond", (Class[]) null);
            method.invoke(device, (Object[]) null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void connectDevice(Context context){
        Log.d(TAG, "IN connectToDevice - Device: " + device);
        if(bPaired == false){
            showToast("Connecting....");
            bleGatt = device.connectGatt(context, false, bleGattCallback);
            bPaired = true;
        }
    }

    private final View.OnClickListener connectButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View arg0) {
            Log.d(TAG, "Connect button clicked! - isPaired: " + bPaired);

//            pairDevice(device);
            connectDevice(getApplicationContext());
        }
    };

    private final BluetoothGattCallback bleGattCallback = new BluetoothGattCallback() {

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            // this will get called anytime you perform a read or write characteristic operation
        }

        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
            // this will get called when a device connects or disconnects
            Log.d(TAG, "connection status: " + status + ", newState: " + newState);
            if(BluetoothProfile.STATE_CONNECTED == newState){
                Log.d(TAG, "Connected to " + device.getAddress() + " and Request Service");
                gatt.discoverServices();
            }
        }

        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
            Log.d(TAG, "In onServicesDiscovered");
            // this will get called after the client initiates a BluetoothGatt.discoverServices() call
            List<BluetoothGattService> services = bleGatt.getServices();

            for (BluetoothGattService service : services) {

                Log.d(TAG, "service: " + service.getUuid().toString());
                Log.d(TAG, "Type: " + service.getType());

                Log.d(TAG, "--------------------------------");

                List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();

                for(BluetoothGattCharacteristic c : characteristics){
                    Log.d(TAG, "uuid: " + c.getUuid().toString());
                    Log.d(TAG, "permissions: " + c.getPermissions());
                    Log.d(TAG, "properties: " + c.getProperties());
                    Log.d(TAG, "writeType: " + c.getWriteType());
                }
                Log.d(TAG, "================================");
            }
        }
    };

    private final BroadcastReceiver mPairReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                final int prevState	= intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);

                if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
                    connectDevice(context);
                } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED){
                    bPaired = false;
                }

//                mAdapter.notifyDataSetChanged();
            }
        }
    };
}
