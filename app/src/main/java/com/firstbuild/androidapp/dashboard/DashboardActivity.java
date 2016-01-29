package com.firstbuild.androidapp.dashboard;

import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.firstbuild.androidapp.ParagonValues;
import com.firstbuild.androidapp.R;
import com.firstbuild.androidapp.addProduct.AddProductActivity;
import com.firstbuild.androidapp.paragon.ParagonMainActivity;
import com.firstbuild.androidapp.paragon.RecipeEditFragment;
import com.firstbuild.androidapp.paragon.dataModel.RecipeInfo;
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

    static final int REQUEST_ENABLE_BT = 1234;
    private static final int INTERVAL_MAX_PRODUCT_UPDATE = 50;      // Max switching time to the switching to next product.
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
                BleManager.getInstance().readCharacteristics(productInfo.bluetoothDevice, ParagonValues.CHARACTERISTIC_PROBE_CONNECTION_STATE);
                BleManager.getInstance().readCharacteristics(productInfo.bluetoothDevice, ParagonValues.CHARACTERISTIC_BATTERY_LEVEL);
                BleManager.getInstance().readCharacteristics(productInfo.bluetoothDevice, ParagonValues.CHARACTERISTIC_BURNER_STATUS);
                BleManager.getInstance().readCharacteristics(productInfo.bluetoothDevice, ParagonValues.CHARACTERISTIC_COOK_MODE);
                BleManager.getInstance().readCharacteristics(productInfo.bluetoothDevice, ParagonValues.CHARACTERISTIC_COOK_CONFIGURATION);
                BleManager.getInstance().readCharacteristics(productInfo.bluetoothDevice, ParagonValues.CHARACTERISTIC_CURRENT_COOK_STATE);

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


                checkBleTurnOff();
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

                BleManager.getInstance().setCharacteristicNotification(productInfo.bluetoothDevice, ParagonValues.CHARACTERISTIC_BURNER_STATUS, true);
                BleManager.getInstance().setCharacteristicNotification(productInfo.bluetoothDevice, ParagonValues.CHARACTERISTIC_BATTERY_LEVEL, true);
                BleManager.getInstance().setCharacteristicNotification(productInfo.bluetoothDevice, ParagonValues.CHARACTERISTIC_PROBE_CONNECTION_STATE, true);
                BleManager.getInstance().readCharacteristics(productInfo.bluetoothDevice, ParagonValues.CHARACTERISTIC_PROBE_CONNECTION_STATE);
                BleManager.getInstance().readCharacteristics(productInfo.bluetoothDevice, ParagonValues.CHARACTERISTIC_BATTERY_LEVEL);
                BleManager.getInstance().readCharacteristics(productInfo.bluetoothDevice, ParagonValues.CHARACTERISTIC_BURNER_STATUS);

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
    private int currentConnectIndex = 0;

    @Override
    public void onBackPressed() {

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

        checkBleTurnOff();

        requestUpdateProducts();

    }


    private void requestUpdateProducts() {

        int size = ProductManager.getInstance().getSize();

        for (int i = 0; i < size; i++) {
            ProductInfo productInfo = ProductManager.getInstance().getProduct(i);
            productInfo.bluetoothDevice = BleManager.getInstance().connect(productInfo.address);
        }

        for (int i = 0; i < size; i++) {
            ProductInfo productInfo = ProductManager.getInstance().getProduct(i);
            if (productInfo.bluetoothDevice != null) {
                BleManager.getInstance().readCharacteristics(productInfo.bluetoothDevice, ParagonValues.CHARACTERISTIC_PROBE_CONNECTION_STATE);
                BleManager.getInstance().readCharacteristics(productInfo.bluetoothDevice, ParagonValues.CHARACTERISTIC_BATTERY_LEVEL);
                BleManager.getInstance().readCharacteristics(productInfo.bluetoothDevice, ParagonValues.CHARACTERISTIC_BURNER_STATUS);
                BleManager.getInstance().readCharacteristics(productInfo.bluetoothDevice, ParagonValues.CHARACTERISTIC_COOK_MODE);
                BleManager.getInstance().readCharacteristics(productInfo.bluetoothDevice, ParagonValues.CHARACTERISTIC_COOK_CONFIGURATION);
                BleManager.getInstance().readCharacteristics(productInfo.bluetoothDevice, ParagonValues.CHARACTERISTIC_CURRENT_COOK_STATE);

                BleManager.getInstance().setCharacteristicNotification(productInfo.bluetoothDevice, ParagonValues.CHARACTERISTIC_BURNER_STATUS, true);
                BleManager.getInstance().setCharacteristicNotification(productInfo.bluetoothDevice, ParagonValues.CHARACTERISTIC_BATTERY_LEVEL, true);
                BleManager.getInstance().setCharacteristicNotification(productInfo.bluetoothDevice, ParagonValues.CHARACTERISTIC_PROBE_CONNECTION_STATE, true);
                BleManager.getInstance().setCharacteristicNotification(productInfo.bluetoothDevice, ParagonValues.CHARACTERISTIC_COOK_MODE, true);
                BleManager.getInstance().setCharacteristicNotification(productInfo.bluetoothDevice, ParagonValues.CHARACTERISTIC_CURRENT_COOK_STATE, true);
                BleManager.getInstance().setCharacteristicNotification(productInfo.bluetoothDevice, ParagonValues.CHARACTERISTIC_CURRENT_TEMPERATURE, true);
                BleManager.getInstance().setCharacteristicNotification(productInfo.bluetoothDevice, ParagonValues.CHARACTERISTIC_ELAPSED_TIME, true);
                BleManager.getInstance().setCharacteristicNotification(productInfo.bluetoothDevice, ParagonValues.CHARACTERISTIC_CURRENT_POWER_LEVEL, true);
            }
        }


        updateListView();
    }

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
                itemClicked(position);
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


    private boolean checkBleTurnOff(){
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

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }

    public void itemClicked(int position) {

        ProductManager.getInstance().setCurrent(position);
        ProductInfo productInfo = adapterDashboard.getItem(position);

        if (productInfo.isConnected() && productInfo.isAllMustDataReceived()) {

            ProductManager.getInstance().setCurrent(position);

            Intent intent = new Intent(DashboardActivity.this, ParagonMainActivity.class);
            startActivity(intent);
        }
        else {
            Log.d(TAG, "itemClicked but error :" + productInfo.type);
        }

    }


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
                product.setErdBatteryLevel(byteBuffer.get());
                Log.d(TAG, "CHARACTERISTIC_BATTERY_LEVEL :" + String.format("%02x", value[0]));
                break;


            case ParagonValues.CHARACTERISTIC_BURNER_STATUS:
                Log.d(TAG, "CHARACTERISTIC_BURNER_STATUS :" + String.format("%02x", value[0]));
                product.setErdBurnerStatus(byteBuffer.get());
                break;

            case ParagonValues.CHARACTERISTIC_PROBE_CONNECTION_STATE:
                Log.d(TAG, "CHARACTERISTIC_PROBE_CONNECTION_STATE :" + String.format("%02x", value[0]));
                product.setErdProbeConnectionStatue(byteBuffer.get());
                break;

            case ParagonValues.CHARACTERISTIC_COOK_MODE:
                Log.d(TAG, "CHARACTERISTIC_COOK_MODE :" + String.format("%02x", value[0]));
                product.setErdCurrentCookMode(byteBuffer.get());
                break;

            case ParagonValues.CHARACTERISTIC_COOK_CONFIGURATION:
                Log.d(TAG, "CHARACTERISTIC_COOK_CONFIGURATION :");

                String data = "";
                for (int i = 0; i < 39; i++) {
                    data += String.format("%02x", value[i]);
                }
                Log.d(TAG, "CONFIGURATION Data :" + data);

                RecipeInfo newRecipe = new RecipeInfo(value);
                product.setErdRecipeConfig(newRecipe);
                break;

            case ParagonValues.CHARACTERISTIC_CURRENT_COOK_STATE:
                Log.d(TAG, "CHARACTERISTIC_CURRENT_COOK_STATE :" + String.format("%02x", value[0]));
                product.setErdCookState(byteBuffer.get());
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
            int numMustData = currentProduct.getMustDataStatus();
            Log.d(TAG, "numMustData :"+numMustData);

            if (currentProduct.isConnected()){

                if(currentProduct.isAllMustDataReceived()) {
                    holderDashboard.layoutProgress.setVisibility(View.GONE);
                    holderDashboard.layoutStatus.setVisibility(View.VISIBLE);
                }
                else{
                    holderDashboard.progressBar.setIndeterminate(false);
                    holderDashboard.progressBar.setMax(ProductInfo.NUM_MUST_INIT_DATA);
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


            if (currentProduct.isProbeConnected()) {

                holderDashboard.imageBattery.setVisibility(View.VISIBLE);

                int level = currentProduct.getErdBatteryLevel();

                if (level > 75) {
                    holderDashboard.imageBattery.setImageResource(R.drawable.ic_battery_100);
                }
                else if (level > 25) {
                    holderDashboard.imageBattery.setImageResource(R.drawable.ic_battery_50);
                }
                else if (level > 15) {
                    holderDashboard.imageBattery.setImageResource(R.drawable.ic_battery_25);
                }
                else {
                    holderDashboard.imageBattery.setImageResource(R.drawable.ic_battery_15);
                }

                String batteryLevel = level + "%";
                holderDashboard.textBattery.setText(batteryLevel + "");
            }
            else {
                holderDashboard.textBattery.setText("probe\noffline");
                holderDashboard.imageBattery.setVisibility(View.GONE);
            }

            if (currentProduct.getErdBurnerStatus() == ParagonValues.BURNER_STATE_START) {
                holderDashboard.textCooking.setText("Cooking");
            }
            else {
                holderDashboard.textCooking.setText("");
            }


            return convertView;
        }


        class ViewHolder {
            private ImageView imageMark;
            private ImageView imageLogo;
            private TextView textNickname;
            private TextView textCooking;
            private TextView textBattery;
            private ImageView imageBattery;
            private View layoutProgress;
            private View layoutStatus;
            private ProgressBar progressBar;

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
