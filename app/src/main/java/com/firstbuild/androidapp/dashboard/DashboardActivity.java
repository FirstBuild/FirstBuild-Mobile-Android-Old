/**
 * @file DashboardActivity.java
 * @brief Activity for Dashboard showing products.
 * @author Hollis Kim (wowhos@gmail.com)
 * @date Oct/1/2015
 * Copyright (c) 2014 General Electric Corporation - Confidential - All rights reserved.
 */
package com.firstbuild.androidapp.dashboard;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.firstbuild.androidapp.ParagonValues;
import com.firstbuild.androidapp.R;
import com.firstbuild.androidapp.addproduct.AddProductActivity;
import com.firstbuild.androidapp.paragon.ParagonMainActivity;
import com.firstbuild.androidapp.productmanager.ProductInfo;
import com.firstbuild.androidapp.productmanager.ProductManager;
import com.firstbuild.androidapp.viewutil.SwipeMenu;
import com.firstbuild.androidapp.viewutil.SwipeMenuCreator;
import com.firstbuild.androidapp.viewutil.SwipeMenuItem;
import com.firstbuild.androidapp.viewutil.SwipeMenuListView;
import com.firstbuild.commonframework.blemanager.BleListener;
import com.firstbuild.commonframework.blemanager.BleManager;
import com.firstbuild.commonframework.blemanager.BleValues;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    static final int REQUEST_ENABLE_BT = 1234;
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

            ProductInfo productInfo = ProductManager.getInstance().getProductByAddress(address);

            if (productInfo != null && status == BluetoothProfile.STATE_DISCONNECTED) {
                productInfo.disconnected();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapterDashboard.notifyDataSetChanged();
                        listViewProduct.invalidateViews();
                    }
                });

//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                adapterDashboard.notifyDataSetChanged();
//                                listViewProduct.invalidateViews();
//                            }
//                        });
//                    }
//                }).start();


                checkBleTurnOn();
            }
        }

        @Override
        public void onServicesDiscovered(String address, List<BluetoothGattService> bleGattServices) {
            super.onServicesDiscovered(address, bleGattServices);

            Log.d(TAG, "[onServicesDiscovered] address: " + address);

            ProductInfo productInfo = ProductManager.getInstance().getProductByAddress(address);

            if (productInfo != null) {
                productInfo.connected();
                productInfo.initMustData();

                requestMustHaveData(productInfo);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapterDashboard.notifyDataSetChanged();
                        listViewProduct.invalidateViews();
                    }
                });

//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                adapterDashboard.notifyDataSetChanged();
//                                listViewProduct.invalidateViews();
//                            }
//                        });
//                    }
//                }).start();
            }
        }

        @Override
        public void onCharacteristicRead(String address, String uuid, byte[] value) {
            super.onCharacteristicRead(address, uuid, value);

            Log.d(TAG, "[onCharacteristicRead] address: " + address + ", uuid: " + uuid);

            onReceivedData(address, uuid, value);
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

            onReceivedData(address, uuid, value);

        }

        @Override
        public void onDescriptorWrite(String address, String uuid, byte[] value) {
            super.onDescriptorWrite(address, uuid, value);

            Log.d(TAG, "[onDescriptorWrite] address: " + address + ", uuid: " + uuid);
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == -1) {
                // Success
                Log.d(TAG, "Bluetooth adapter is enabled. Start scanning.");
            }
            else if (resultCode == 0) {
                Log.d(TAG, "Bluetooth adapter is still disabled");
            }
            else {
                // Else
            }
        }
        else {

        }
    }

    @Override
    public void onBackPressed() {
        //TODO: Consider later if app exit when press back button.
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

        // Initialize ble manager
        BleManager.getInstance().initBleManager(this);

        // Add ble event listener
        BleManager.getInstance().addListener(bleListener);

        // Check if Bluetooth turned off.
        if (checkBleTurnOn()) {
            requestUpdateProducts();
        }

    }

    /**
     * Check if bluetooto turned off.
     *
     * @return true if turned on.
     */
    private boolean checkBleTurnOn() {
        // Check bluetooth adapter. If the adapter is disabled, enable it
        boolean result = BleManager.getInstance().isBluetoothEnabled();

        if (!result) {
            Log.d(TAG, "Bluetooth adapter is disabled. Enable bluetooth adapter.");

            int size = ProductManager.getInstance().getSize();

            for (int i = 0; i < size; i++) {
                ProductInfo productInfo = ProductManager.getInstance().getProduct(i);
                productInfo.disconnected();
            }

            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        else {
            Log.d(TAG, "Bluetooth adapter is already enabled. Start connect");
        }

        return result;
    }

    /**
     * Request initial data to registed product.
     */
    private void requestUpdateProducts() {

        int size = ProductManager.getInstance().getSize();

        for (int i = 0; i < size; i++) {
            ProductInfo productInfo = ProductManager.getInstance().getProduct(i);
            if(productInfo.bluetoothDevice == null) {
                productInfo.bluetoothDevice = BleManager.getInstance().connect(productInfo.address);
            }
        }

        for (int i = 0; i < size; i++) {
            ProductInfo productInfo = ProductManager.getInstance().getProduct(i);
            requestMustHaveData(productInfo);
        }


        updateListView();
    }

    /**
     * Must get datas.
     *
     * @param productInfo Object of ProductInfo.
     */
    private void requestMustHaveData(ProductInfo productInfo) {
        if (productInfo.bluetoothDevice != null) {

            // read must have characteristics
            for(String uuid : productInfo.getMustHaveUUIDList()) {
                BleManager.getInstance().readCharacteristics(productInfo.bluetoothDevice, uuid);
            }

            // set must-have-notification characteristics
            for(String uuid : productInfo.getMustHaveNotificationUUIDList()) {
                BleManager.getInstance().setCharacteristicNotification(productInfo.bluetoothDevice, uuid, true);
            }
        }

    }

    /**
     * Update List view. if no item for list show robot image.
     */
    private void updateListView() {

        adapterDashboard.notifyDataSetChanged();
        listViewProduct.invalidateViews();


        if (adapterDashboard.getCount() == 0) {
            layoutNoProduct.setVisibility(View.VISIBLE);
            listViewProduct.setVisibility(View.GONE);
        }
        else {
            layoutNoProduct.setVisibility(View.GONE);
            listViewProduct.setVisibility(View.VISIBLE);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate IN");
        setContentView(R.layout.activity_dashboard);

        setTheme(R.style.AppTheme);

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        toolbar.setTitle("");
        ((TextView) toolbar.findViewById(R.id.toolbar_title)).setText(R.string.header_title_dashboard);

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
            public boolean onMenuItemClick(final int position, SwipeMenu menu, int index) {

                if (index == 0) {
                    // delete
                    Log.d(TAG, "onMenuItemClick 0");


                    new MaterialDialog.Builder(DashboardActivity.this)
                            .title("Delete product")
                            .content("Are you sure?")
                            .positiveText("Yes")
                            .negativeText("no")
                            .cancelable(false)
                            .callback(new MaterialDialog.ButtonCallback() {
                                @Override
                                public void onPositive(MaterialDialog dialog) {
                                    ProductInfo product = ProductManager.getInstance().getProduct(position);

                                    BleManager.getInstance().disconnect(product.bluetoothDevice);
                                    ProductManager.getInstance().remove(position);

                                    updateListView();
                                }

                                @Override
                                public void onNegative(MaterialDialog dialog) {
                                }

                                @Override
                                public void onNeutral(MaterialDialog dialog) {
                                }
                            })
                            .show();

                }
                return false;
            }
        });


        listViewProduct.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onItemClicked(position);
            }
        });


        findViewById(R.id.btnAddProduct).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(DashboardActivity.this, AddProductActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
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

    /**
     * Convert dp to pixel.
     *
     * @param dp dp value.
     * @return pixel value.
     */
    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }


    /**
     * call when press item on listview.
     *
     * @param position 0 index of list item.
     */
    public void onItemClicked(int position) {

//        ProductManager.getInstance().setCurrent(position);
        ProductInfo productInfo = adapterDashboard.getItem(position);

        if (productInfo.isConnected() && productInfo.isAllMustDataReceived()) {

            ProductManager.getInstance().setCurrent(position);

            Intent intent = new Intent(DashboardActivity.this, ParagonMainActivity.class);
            startActivity(intent);
        }
        else {
            Log.d(TAG, "onItemClicked but error :" + productInfo.type);
        }

    }


    /**
     * Called when data comming from Pragon Master.
     *
     * @param address Address of BLE.
     * @param uuid    UUID
     * @param value   value get from BLE.
     */
    private void onReceivedData(String address, String uuid, byte[] value) {

        Log.d(TAG, "onReceivedData :" + uuid);

        if (value == null) {
            Log.d(TAG, "onReceivedData :value is null");
            return;
        }

        ByteBuffer byteBuffer = ByteBuffer.wrap(value);
        ProductInfo product = ProductManager.getInstance().getProductByAddress(address);

        if (product == null) {
            Log.d(TAG, "Not found product by Address [" + address);
            return;
        }

        product.connected();

        switch (uuid.toUpperCase()) {

            case ParagonValues.CHARACTERISTIC_BATTERY_LEVEL:
                break;

            case ParagonValues.CHARACTERISTIC_ELAPSED_TIME:
                break;

            case ParagonValues.CHARACTERISTIC_BURNER_STATUS:
                break;

            case ParagonValues.CHARACTERISTIC_PROBE_CONNECTION_STATE:
                break;

            case ParagonValues.CHARACTERISTIC_COOK_MODE:
                break;

            case ParagonValues.CHARACTERISTIC_COOK_CONFIGURATION:
                break;

            case ParagonValues.CHARACTERISTIC_CURRENT_COOK_STATE:
                break;

            case ParagonValues.CHARACTERISTIC_CURRENT_TEMPERATURE:
                //Skip refresh ui.
                return;

        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapterDashboard.notifyDataSetChanged();

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
            Log.d(TAG, "update View");

            if (convertView == null) {
                convertView = View.inflate(getApplicationContext(), R.layout.adapter_product_card_view, null);
                convertView.setTag(new ViewHolder(convertView));
            }
            ViewHolder holderDashboard = (ViewHolder) convertView.getTag();
            ProductInfo currentProduct = getItem(position);

            holderDashboard.textNickname.setText(currentProduct.nickname);

            int numMustData = currentProduct.getMustDataStatus();
            Log.d(TAG, "numMustData :" + numMustData);

            if (currentProduct.isConnected()) {

                if (currentProduct.isAllMustDataReceived()) {
                    holderDashboard.layoutProgress.setVisibility(View.GONE);
                    holderDashboard.layoutStatus.setVisibility(View.VISIBLE);
                }
                else {
                    holderDashboard.progressBar.setIndeterminate(false);
                    holderDashboard.progressBar.setMax(currentProduct.getNumMustInitData());
                    holderDashboard.progressBar.setProgress(numMustData);

                    holderDashboard.layoutProgress.setVisibility(View.VISIBLE);
                    holderDashboard.layoutStatus.setVisibility(View.GONE);
                }
            }
            else {
                holderDashboard.progressBar.setIndeterminate(true);
                holderDashboard.layoutProgress.setVisibility(View.VISIBLE);
                holderDashboard.layoutStatus.setVisibility(View.GONE);
            }

            // Let each productInfo instance handle product specific UI update
            currentProduct.updateDashboardItemUI(holderDashboard);

            return convertView;
        }


        public class ViewHolder {
            public ImageView imageMark;
            public ImageView imageLogo;
            public TextView textNickname;
            public TextView textCooking;
            public TextView textBattery;
            public ImageView imageBattery;
            public View layoutProgress;
            public View layoutStatus;
            public ProgressBar progressBar;

            public ViewHolder(View view) {
                imageMark = (ImageView) view.findViewById(R.id.image_mark);
                imageLogo = (ImageView) view.findViewById(R.id.image_logo);
                textNickname = (TextView) view.findViewById(R.id.item_nickname);
                textCooking = (TextView) view.findViewById(R.id.text_cooking);
                textBattery = (TextView) view.findViewById(R.id.text_battery);
                imageBattery = (ImageView) view.findViewById(R.id.image_battery);
                layoutProgress = view.findViewById(R.id.layout_progressbar);
                layoutStatus = view.findViewById(R.id.layout_status);
                progressBar = (ProgressBar) view.findViewById(R.id.progressBar);

                view.setTag(this);
            }
        }

    }


}
