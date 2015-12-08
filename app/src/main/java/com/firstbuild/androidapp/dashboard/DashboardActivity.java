package com.firstbuild.androidapp.dashboard;

import android.app.ActivityOptions;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firstbuild.androidapp.ParagonValues;
import com.firstbuild.androidapp.R;
import com.firstbuild.androidapp.addProduct.AddProductActivity;
import com.firstbuild.androidapp.paragon.ParagonMainActivity;
import com.firstbuild.androidapp.paragon.dataModel.RecipeManager;
import com.firstbuild.androidapp.productManager.ProductInfo;
import com.firstbuild.androidapp.productManager.ProductManager;
import com.firstbuild.androidapp.viewUtil.SwipeMenu;
import com.firstbuild.androidapp.viewUtil.SwipeMenuCreator;
import com.firstbuild.androidapp.viewUtil.SwipeMenuItem;
import com.firstbuild.androidapp.viewUtil.SwipeMenuListView;
import com.firstbuild.commonframework.bleManager.BleListener;
import com.firstbuild.commonframework.bleManager.BleManager;
import com.firstbuild.commonframework.bleManager.BleValues;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;

public class DashboardActivity extends ActionBarActivity {

    private String TAG = DashboardActivity.class.getSimpleName();
    private SwipeMenuListView listViewProduct;

    private ProductListAdapter adapterDashboard;
    // Bluetooth adapter handler
    private BluetoothAdapter bluetoothAdapter = null;
    private View layoutNoProduct;


    private BleListener bleListener = new BleListener() {
        @Override
        public void onScanDevices(HashMap<String, BluetoothDevice> bluetoothDevices) {
            super.onScanDevices(bluetoothDevices);
            Log.d(TAG, "onScanDevices IN");
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

            BleManager.getInstance().setCharacteristicNotification(ParagonValues.CHARACTERISTIC_PROBE_CONNECTION_STATE, true);
            BleManager.getInstance().setCharacteristicNotification(ParagonValues.CHARACTERISTIC_BATTERY_LEVEL, true);
            BleManager.getInstance().readCharacteristics(ParagonValues.CHARACTERISTIC_PROBE_CONNECTION_STATE);
            BleManager.getInstance().readCharacteristics(ParagonValues.CHARACTERISTIC_BATTERY_LEVEL);
            BleManager.getInstance().readCharacteristics(ParagonValues.CHARACTERISTIC_COOK_MODE);

        }

        @Override
        public void onCharacteristicRead(String address, String uuid, byte[] value) {
            super.onCharacteristicRead(address, uuid, value);

            Log.d(TAG, "[onCharacteristicRead] address: " + address + ", uuid: " + uuid);

            onReceivedData(uuid, value);
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

        }

        @Override
        public void onDescriptorWrite(String address, String uuid, byte[] value) {
            super.onDescriptorWrite(address, uuid, value);

            Log.d(TAG, "[onDescriptorWrite] address: " + address + ", uuid: " + uuid);
        }
    };


    @Override
    public void onBackPressed() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate IN");
        setContentView(R.layout.activity_dashboard);

        setTheme(R.style.AppTheme);

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        toolbar.setTitle("");
        ((TextView) toolbar.findViewById(R.id.toolbar_title)).setText("My Products");

        setSupportActionBar(toolbar);

        listViewProduct = (SwipeMenuListView) findViewById(R.id.listProduct);
        layoutNoProduct = findViewById(R.id.img_no_product);

        layoutNoProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.urlFistBuild))));
            }
        });

        adapterDashboard = new ProductListAdapter();
        listViewProduct.setAdapter(adapterDashboard);

        // step 1. create a MenuCreator
        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                SwipeMenuItem item;

                // create "delete" item
                item = new SwipeMenuItem(getApplicationContext());
                item.setBackground(R.color.colorParagonHighlight);
                item.setWidth(dp2px(90));
                item.setTitle("Delete");
                item.setTitleSize(18);
                item.setTitleColor(Color.WHITE);
                menu.addMenuItem(item);
            }
        };
        // set creator
        listViewProduct.setMenuCreator(creator);

        // step 2. listener item click event
        listViewProduct.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {

                switch (index) {
                    case 0:
                        // delete
                        Log.d(TAG, "onMenuItemClick 0");
                        ProductInfo product = ProductManager.getInstance().getProduct(0);

                        BleManager.getInstance().unpair(product.address);
                        ProductManager.getInstance().remove(0);

                        updateListView();

                        break;
                }
                return false;
            }
        });


        listViewProduct.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                itemClicked(position);
            }
        });


        findViewById(R.id.btnAddProduct).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashboardActivity.this, AddProductActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
//                startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(DashboardActivity.this).toBundle());
                startActivity(intent);
            }
        });


        // Use this check to determine whether BLE is supported on the device. Then
        // you can selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Log.d(TAG, "BLE is not supported - Stop activity!");

            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            BleManager.getInstance().removeListener(bleListener);
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
                BleManager.getInstance().removeListener(bleListener);
                finish();
            }
            else {
                // Do nothing
            }

        }

    }

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }

    @Override
    protected void onPause() {
        super.onPause();

        BleManager.getInstance().removeListener(bleListener);
    }

    @Override
    protected void onResume() {

        Log.d(TAG, "onResume");
        super.onResume();

//        BleManager.getInstance().disconnect();

        // Check bluetooth adapter. If the adapter is disabled, enable it
//        boolean result = BleManager.getInstance().isBluetoothEnabled();
//
//        if (!result) {
//            Log.d(TAG, "Bluetooth adapter is disabled. Enable bluetooth adapter.");
//            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivityForResult(enableBtIntent, BleValues.REQUEST_ENABLE_BT);
//        }
//        else {
//            Log.d(TAG, "Bluetooth adapter is already enabled. Start scanning.");
//            BleManager.getInstance().startScan();
//        }


        // Initialize ble manager
        BleManager.getInstance().initBleManager(this);

        // Add ble event listener
        BleManager.getInstance().addListener(bleListener);

        updateListView();

        connectProducts();



    }

    private void updateListView() {

        adapterDashboard.notifyDataSetChanged();
        listViewProduct.invalidateViews();


        if(adapterDashboard.getCount() == 0){
            layoutNoProduct.setVisibility(View.VISIBLE);
            listViewProduct.setVisibility(View.GONE);
        }
        else{
            layoutNoProduct.setVisibility(View.GONE);
            listViewProduct.setVisibility(View.VISIBLE);
        }

    }

    private void connectProducts() {

        if (ProductManager.getInstance().getSize() > 0) {
            ProductInfo product = ProductManager.getInstance().getProduct(0);
            boolean result = BleManager.getInstance().isBluetoothEnabled();

            if (!result) {
                Log.d(TAG, "Bluetooth adapter is disabled. Enable bluetooth adapter.");
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, BleValues.REQUEST_ENABLE_BT);
            }
            else {
                Log.d(TAG, "Bluetooth adapter is already enabled. Start connecting with " + product.address);
                BleManager.getInstance().connect(product.address);
            }

        }


    }


    private void onReceivedData(String uuid, byte[] value) {

        Log.d(TAG, "onReceivedData :" + uuid);

        if (value == null) {
            Log.d(TAG, "onReceivedData :value is null");
            return;
        }

        ByteBuffer byteBuffer = ByteBuffer.wrap(value);
        ProductInfo product = ProductManager.getInstance().getProduct(0);


        switch (uuid.toUpperCase()) {

            case ParagonValues.CHARACTERISTIC_BATTERY_LEVEL:
                Log.d(TAG, "CHARACTERISTIC_BATTERY_LEVEL :" + String.format("%02x", value[0]));

                product.batteryLevel = (int) byteBuffer.get();
                break;

            case ParagonValues.CHARACTERISTIC_COOK_MODE:
                Log.d(TAG, "CHARACTERISTIC_COOK_MODE :" + String.format("%02x", value[0]));

                if (byteBuffer.get() == ParagonValues.CURRENT_COOK_MODE_MULTISTEP) {
                    product.cookMode = "Cooking";
                }
                else {
                    product.cookMode = "";
                }
                break;


            case ParagonValues.CHARACTERISTIC_PROBE_CONNECTION_STATE:
                Log.d(TAG, "CHARACTERISTIC_PROBE_CONNECTION_STATE :" + String.format("%02x", value[0]));

                if(byteBuffer.get() == ParagonValues.PROBE_CONNECT){
                    product.isProbeConnect = true;
                }
                else {
                    product.isProbeConnect = false;
                }
                break;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapterDashboard.notifyDataSetChanged();
                        listViewProduct.invalidateViews();

                    }
                });
            }
        }).start();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
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


    private void addDeviceList(BluetoothDevice device) {

    }

    public void itemClicked(int position) {
        ProductInfo productInfo = adapterDashboard.getItem(position);

        if(productInfo.batteryLevel == ProductInfo.NO_BATTERY_INFO &&
                productInfo.cookMode.isEmpty()){
            return;
        }

        if (productInfo.type == ProductInfo.PRODUCT_TYPE_PARAGON) {
            Intent intent = new Intent(DashboardActivity.this, ParagonMainActivity.class);
            startActivity(intent);
        }
        else {
            Log.d(TAG, "itemClicked but error :" + productInfo.type);
        }

    }


    public class ProductListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return ProductManager.getInstance().getSize();
        }

        @Override
        public ProductInfo getItem(int position) {
            return ProductManager.getInstance().getProduct(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(getApplicationContext(), R.layout.adapter_product_card_view, null);
                new ViewHolder(convertView);
            }
            ViewHolder holderDashboard = (ViewHolder) convertView.getTag();
            ProductInfo currentProduct = getItem(position);


            if (currentProduct.type == ProductInfo.PRODUCT_TYPE_CILLHUB) {
                holderDashboard.imageLogo.setImageResource(R.drawable.ic_paragon_logo);
                holderDashboard.imageMark.setImageResource(R.drawable.ic_paragon_mark);
            }
            else if (currentProduct.type == ProductInfo.PRODUCT_TYPE_PARAGON) {
                holderDashboard.imageLogo.setImageResource(R.drawable.ic_paragon_logo);
                holderDashboard.imageMark.setImageResource(R.drawable.ic_paragon_mark);

            }
            else {
            }

            holderDashboard.textNickname.setText(currentProduct.nickname);

            if (currentProduct.cookMode.isEmpty()) {
                holderDashboard.textCooking.setVisibility(View.GONE);
            }
            else {
                holderDashboard.textCooking.setVisibility(View.VISIBLE);
                holderDashboard.textCooking.setText(currentProduct.cookMode);
            }

            String batteryLevel = currentProduct.batteryLevel + "%";
            holderDashboard.textBattery.setText(batteryLevel + "");


            if(currentProduct.isProbeConnect == false){
                holderDashboard.textProbe.setVisibility(View.VISIBLE);
                holderDashboard.layoutBatteryLevel.setVisibility(View.GONE);
            }
            else{

                holderDashboard.textProbe.setVisibility(View.GONE);
                holderDashboard.layoutBatteryLevel.setVisibility(View.VISIBLE);

                if (currentProduct.batteryLevel > 75) {
                    holderDashboard.imageBattery.setImageResource(R.drawable.ic_battery_100);
                }
                else if (currentProduct.batteryLevel > 25) {
                    holderDashboard.imageBattery.setImageResource(R.drawable.ic_battery_50);
                }
                else if (currentProduct.batteryLevel > 15) {
                    holderDashboard.imageBattery.setImageResource(R.drawable.ic_battery_25);
                }
                else {
                    holderDashboard.imageBattery.setImageResource(R.drawable.ic_battery_15);
                }

            }

            if(currentProduct.batteryLevel == ProductInfo.NO_BATTERY_INFO){
                holderDashboard.progressBar.setVisibility(View.VISIBLE);
                holderDashboard.layoutStatus.setVisibility(View.GONE);
            }
            else{
                holderDashboard.progressBar.setVisibility(View.GONE);
                holderDashboard.layoutStatus.setVisibility(View.VISIBLE);
            }

            return convertView;
        }


        class ViewHolder {
            private ImageView imageMark;
            private ImageView imageLogo;
            private TextView textNickname;
            private TextView textCooking;
            private TextView textBattery;
            private TextView textProbe;
            private ImageView imageBattery;
            private View progressBar;
            private View layoutStatus;
            private View layoutBatteryLevel;

            public ViewHolder(View view) {
                imageMark = (ImageView) view.findViewById(R.id.image_mark);
                imageLogo = (ImageView) view.findViewById(R.id.image_logo);
                textNickname = (TextView) view.findViewById(R.id.item_nickname);
                textCooking = (TextView) view.findViewById(R.id.text_cooking);
                textBattery = (TextView) view.findViewById(R.id.text_battery);
                imageBattery = (ImageView) view.findViewById(R.id.image_battery);
                progressBar = view.findViewById(R.id.progressBar);
                layoutStatus = view.findViewById(R.id.layout_status);
                textProbe = (TextView) view.findViewById(R.id.text_probe_connect);
                layoutBatteryLevel = view.findViewById(R.id.layout_battery_level);

                view.setTag(this);
            }
        }

    }


}
