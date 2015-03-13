package com.firstbuild.androidapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends ActionBarActivity {
    private final String TAG = "MainActivity";
    private final int REQUEST_ENABLE_BT = 1000;

    private static Context context = null;
    private Button scanButton = null;
    private ListView listView = null;
    private BluetoothAdapter bluetoothAdapter = null;
    private ArrayList<BluetoothDevice> bleDevices = null;

    private DeviceListAdapter deviceListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        context = this;
        setContentView(R.layout.activity_main);

        scanButton = (Button) findViewById(R.id.scan_button);
        listView = (ListView) findViewById(R.id.ble_device_list);

        // Get Ble device adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Set List view to showing scan result
        deviceListAdapter = new DeviceListAdapter(this);

        // When a ble device scan button clicked
        scanButton.setOnClickListener(scanButtonClickListener);

        // When item is clicked from list view
        listView.setOnItemClickListener(itemClickListener);

        // Register intents for broadcast receiver
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        registerReceiver(broadcastReceiver, intentFilter);

        checkBluetoothOnOff();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
        unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    private void checkBluetoothOnOff(){
        if(bluetoothAdapter == null || !bluetoothAdapter.isEnabled()){
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "requestCode: " + requestCode + ", resultCode: " +  resultCode);
        switch(requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK) {
                    showToast("Bluetooth is ON");
                }
                else {
                    showToast("Bluetooth is OFF");
                }
                break;
            default:
                break;
        }
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private final View.OnClickListener scanButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View arg0) {
            Log.d(TAG, "Scan button clicked");
            bluetoothAdapter.cancelDiscovery();

            // Clear ble device list
            if(bleDevices != null) {
                bleDevices = null;
            }

            bluetoothAdapter.startDiscovery();
        }
    };

    // An item is clicked in the ble device list
    private final AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            bluetoothAdapter.cancelDiscovery();

            BluetoothDevice device = (BluetoothDevice) deviceListAdapter.getItem(position);
            Log.d(TAG, "Clicked item: " + device);

            Intent intent = new Intent(getBaseContext(), BleDeviceActivity.class);
            intent.putExtra("BLE_DEVICE", device);
            startActivity(intent);
        }
    };

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                if (state == BluetoothAdapter.STATE_ON) {
                    // TODO - do nothing so far.
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Log.d(TAG, "In BroadcastReceiver - Discovery started");

                if(bleDevices == null){
                    bleDevices = new ArrayList<BluetoothDevice>();
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.d(TAG, "In BroadcastReceiver - Discovery finished");

            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                Log.d(TAG, "In BroadcastReceiver - BluetoothDevice found!");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                boolean isDuplicated = isDuplicate(device);

                if(isDuplicated == false) {
                    bleDevices.add(device);
                    // Update items in the list view
                    deviceListAdapter.setData(bleDevices);
                    listView.setAdapter(deviceListAdapter);

                    for(BluetoothDevice item : bleDevices){
                        Log.d(TAG, item.getAddress() + " : " + item.getBondState());
                    }
                }
            }
        }

        private boolean isDuplicate(BluetoothDevice device){
            boolean isDuplicated = false;
            for(BluetoothDevice item : bleDevices){
                if(item == device){
                    isDuplicated = true;
                    break;
                }
            }

            return isDuplicated;
        }
    };
}