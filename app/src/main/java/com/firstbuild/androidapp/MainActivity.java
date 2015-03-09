package com.firstbuild.androidapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;


public class MainActivity extends ActionBarActivity {
    private final String TAG = "MainActivity";
    private static Context context = null;
    private Button scanButton = null;
    private ListView listView = null;
    private BluetoothAdapter bluetoothAdapter = null;
    private ArrayList<BluetoothDevice> bleDeviceList = null;
    //    private ArrayAdapter<String> arrayAdapter = null;
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

        // When a ble device scan button clicked.
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Log.d(TAG, "Scan button clicked");
                bluetoothAdapter.startDiscovery();
                Handler handler = new Handler();

                handler.postDelayed(new Runnable() {
                    public void run() {
                        Log.d(TAG, "cancelDiscovery");
                        bluetoothAdapter.cancelDiscovery();
                    }
                }, 7000);
            }
        });

        // Register intents for broadcast receiver
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        registerReceiver(broadcastReceiver, intentFilter);
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

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                if (state == BluetoothAdapter.STATE_ON) {

                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Log.d(TAG, "In BroadcastReceiver - Discovery started");

                if(bleDeviceList == null) {
                    bleDeviceList = new ArrayList<BluetoothDevice>();
//                    bleDeviceList = new HashMap<String, BluetoothDevice>();
                }

//                mProgressDlg.show();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.d(TAG, "In BroadcastReceiver - Discovery finished");

//                deviceListAdapter = new DeviceListAdapter(this);
                listView.setAdapter(deviceListAdapter);
                deviceListAdapter.setData(bleDeviceList);

            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                Log.d(TAG, "In BroadcastReceiver - BluetoothDevice found!");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                bleDeviceList.add(device);

                for(BluetoothDevice item : bleDeviceList){
                    Log.d(TAG, "Device: " + item);
                }

//                showToast("Found device " + device.getName());
            }
        }
    };
}


//                bleDeviceList.put(device.getAddress(), device);
//
//                for (HashMap.Entry<String, BluetoothDevice> entry : bleDeviceList.entrySet()) {
//                    Log.d(TAG, "Device" + entry.getKey()+" : " + entry.getValue());
//                }