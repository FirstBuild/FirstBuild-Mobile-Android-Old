package com.firstbuild.androidapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
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

import com.firstbuild.ble.BluetoothLeManager;
import com.firstbuild.ble.BluetoothListener;

import java.util.ArrayList;

public class MainActivity extends ActionBarActivity {
    private final static String TAG = MainActivity.class.getSimpleName();
    private final int REQUEST_ENABLE_BT = 1000;

    private static Context context = null;
    private Button scanButton = null;
    private ListView listView = null;
    private BluetoothAdapter bluetoothAdapter = null;
    private ArrayList<BluetoothDevice> bleDevices = null;

    private DeviceListAdapter deviceListAdapter;
    private BluetoothLeManager bluetoothLeManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        context = this;
        setContentView(R.layout.activity_main);

        scanButton = (Button) findViewById(R.id.scan_button);
        listView = (ListView) findViewById(R.id.ble_device_list);

        // Prevent app from rotating
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Set List view to showing scan result
        deviceListAdapter = new DeviceListAdapter(this);

        // When a ble device scan button clicked
        scanButton.setOnClickListener(scanButtonClickListener);

        // When item is clicked from list view
        listView.setOnItemClickListener(itemClickListener);

        // Register Observer
        bluetoothLeManager = BluetoothLeManager.getSharedObject();
        bluetoothLeManager.addListener(onScanListener);

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
        bluetoothLeManager.close();
        super.onDestroy();
    }

    private void checkBluetoothOnOff(){
        if(!bluetoothLeManager.checkBluetoothOnOff()){
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
            bluetoothLeManager.stopScan(context);

            // Clear ble device list
            if(bleDevices != null) {
                bleDevices = null;
            }
            bluetoothLeManager.startScan(context);
        }
    };

    private final AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            bluetoothLeManager.stopScan(context);

            BluetoothDevice device = (BluetoothDevice) deviceListAdapter.getItem(position);
            Log.d(TAG, "Clicked item: " + device);

            Intent intent = new Intent(getBaseContext(), BleDeviceActivity.class);
            intent.putExtra("BLE_DEVICE", device);
            startActivity(intent);
        }
    };

    private final BluetoothListener onScanListener = new BluetoothListener(){
        public void onScan(String action){
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                Log.d(TAG, "In BroadcastReceiver - Status changed");
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Log.d(TAG, "In BroadcastReceiver - Discovery started");
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.d(TAG, "In BroadcastReceiver - Discovery finished");
            }
            else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                Log.d(TAG, "In BroadcastReceiver - BluetoothDevice found!");
                ArrayList<BluetoothDevice> bleDevices = bluetoothLeManager.getBluetoothDeviceList();

                deviceListAdapter.setData(bleDevices);
                listView.setAdapter(deviceListAdapter);

                for(BluetoothDevice bleDevice : bleDevices){
                    Log.d(TAG, "ble Device found: " + bleDevice);
                }
            }
        }
    };
}
