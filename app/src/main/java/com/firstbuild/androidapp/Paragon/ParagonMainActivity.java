package com.firstbuild.androidapp.paragon;

import android.app.Fragment;
import android.app.FragmentManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.firstbuild.androidapp.FirstBuildApplication;
import com.firstbuild.androidapp.ParagonValues;
import com.firstbuild.androidapp.R;
import com.firstbuild.androidapp.paragon.dataModel.BuiltInRecipeInfo;
import com.firstbuild.androidapp.paragon.dataModel.BuiltInRecipeSettingsInfo;
import com.firstbuild.androidapp.paragon.dataModel.RecipeInfo;
import com.firstbuild.androidapp.paragon.navigation.NavigationDrawerFragment;
import com.firstbuild.androidapp.paragon.settings.SettingsActivity;
import com.firstbuild.androidapp.productManager.ProductInfo;
import com.firstbuild.androidapp.productManager.ProductManager;
import com.firstbuild.commonframework.bleManager.BleListener;
import com.firstbuild.commonframework.bleManager.BleManager;
import com.firstbuild.commonframework.bleManager.BleValues;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class ParagonMainActivity extends ActionBarActivity {
    public static final int INTERVAL_CHECKING_PARAGON_CONNECTION = 1000;
    static final int REQUEST_TAKE_PHOTO = 1;
    static final byte INITIAL_VALUE = 0x0f;
    private final float MIN_THICKNESS = 0.25f;
    private final float MAX_THICKNESS = 4.0f;
    private final int INTERVAL_GOODTOGO = 300;
    private final String PREF_KEY_FOOD_WARNING = "FoodWarning";
    private final int MAX_WAITING_INIT_TIME = 30;
    public BuiltInRecipeInfo builtInRecipes = null;
    public BuiltInRecipeSettingsInfo selectedBuiltInRecipe = null;
    Toolbar toolbar;
    private String TAG = ParagonMainActivity.class.getSimpleName();
    // Bluetooth adapter handler
    private BluetoothAdapter bluetoothAdapter = null;
    private ParagonSteps currentStep = ParagonSteps.STEP_NONE;

    private boolean isCheckingCurrentStatus = false;
    // Thread handler for checking the connection with Paragon Master.
    private Handler handlerCheckingConnection;
    // Thread for update UI.
    private Runnable runnable;
    private Handler handlerCheckingGoodToGo = null;
    // Thread for update UI.
    private Runnable runnableGoodToGo;
    // Navigation drawer.
    private NavigationDrawerFragment drawerFragment;
    private TextView toolbarText;
    private ImageView toolbarImage;
    private String currentPhotoPath;
    private MaterialDialog dialogWaiting;
    private MaterialDialog dialogOtaProcessing;
    private MaterialDialog dialogOtaAsk;
    private MaterialDialog disconnectDialog = null;
    private MaterialDialog dialogGoodToGo;
    private MaterialDialog dialogFoodWarning;
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

            final ProductInfo productInfo = ProductManager.getInstance().getCurrent();

            if (address.equals(productInfo.address)) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (status == BluetoothProfile.STATE_CONNECTED) {

                                    if (disconnectDialog.isShowing()) {
                                        disconnectDialog.dismiss();

                                        new MaterialDialog.Builder(ParagonMainActivity.this)
                                                .content("Bluetooth Reconnected")
                                                .positiveText("Ok")
                                                .cancelable(true).show();
                                    }
                                    else {

                                    }
                                }
                                else if (status == BluetoothProfile.STATE_DISCONNECTED) {
                                    disconnectDialog.show();
                                    BleManager.getInstance().readCharacteristics(productInfo.bluetoothDevice, ParagonValues.CHARACTERISTIC_PROBE_CONNECTION_STATE);
                                    BleManager.getInstance().readCharacteristics(productInfo.bluetoothDevice, ParagonValues.CHARACTERISTIC_BATTERY_LEVEL);
                                    BleManager.getInstance().readCharacteristics(productInfo.bluetoothDevice, ParagonValues.CHARACTERISTIC_BURNER_STATUS);
                                    BleManager.getInstance().readCharacteristics(productInfo.bluetoothDevice, ParagonValues.CHARACTERISTIC_COOK_MODE);
                                    BleManager.getInstance().readCharacteristics(productInfo.bluetoothDevice, ParagonValues.CHARACTERISTIC_COOK_CONFIGURATION);
                                    BleManager.getInstance().readCharacteristics(productInfo.bluetoothDevice, ParagonValues.CHARACTERISTIC_CURRENT_COOK_STATE);

                                }
                                else {
                                    // do nothing
                                }

                            }
                        });
                    }
                }).start();
            }

        }

        @Override
        public void onServicesDiscovered(String address, List<BluetoothGattService> bleGattServices) {
            super.onServicesDiscovered(address, bleGattServices);

            Log.d(TAG, "[onServicesDiscovered] address: " + address);
        }

        @Override
        public void onCharacteristicRead(String address, String uuid, byte[] value) {
            super.onCharacteristicRead(address, uuid, value);

            Log.d(TAG, "[onCharacteristicRead] address: " + address + ", uuid: " + uuid);

            onReceivedData(uuid, value);
        }

        @Override
        public void onCharacteristicWrite(String address, String uuid, final byte[] value) {
            super.onCharacteristicWrite(address, uuid, value);

            final int ret = (int) value[0];

            Log.d(TAG, "[onCharacteristicWrite] uuid: " + uuid + ", value: " + String.format("%02x", value[0]));

            onWriteData(uuid, value);

        }

        @Override
        public void onCharacteristicChanged(String address, String uuid, byte[] value) {
            super.onCharacteristicChanged(address, uuid, value);

            Log.d(TAG, "[onCharacteristicChanged] address: " + address + ", uuid: " + uuid);

            onReceivedData(uuid, value);

//            nextCharacteristicRead();
        }

        @Override
        public void onDescriptorWrite(String address, String uuid, byte[] value) {
            super.onDescriptorWrite(address, uuid, value);

            Log.d(TAG, "[onDescriptorWrite] address: " + address + ", uuid: " + uuid);
        }
    };

    private int checkingCountDown;
    private static final int WRITE_STATE_NONE = 0;
    private static final int WRITE_STATE_WRITING = 1;
    private static final int WRITE_STATE_WRITE_DONE = 2;
    private int writeDataState = WRITE_STATE_NONE;


    public MaterialDialog getDialogOtaProcessing() {
        return dialogOtaProcessing;
    }


    private void onReceivedData(String uuid, byte[] value) {

        Log.d(TAG, "onReceivedData :" + uuid);

        if (value == null) {
            Log.d(TAG, "onReceivedData :value is null");
            return;
        }

        ByteBuffer byteBuffer = ByteBuffer.wrap(value);

        ProductInfo productInfo = ProductManager.getInstance().getCurrent();
        if (productInfo == null) {
            Log.d(TAG, "productInfo is null");
            return;
        }

        switch (uuid.toUpperCase()) {
            case ParagonValues.CHARACTERISTIC_CURRENT_TEMPERATURE:
                Log.d(TAG, "CHARACTERISTIC_CURRENT_TEMPERATURE :" + String.format("%02x%02x", value[0], value[1]));
                productInfo.setErdCurrentTemp(byteBuffer.getShort());

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Fragment fragment = getFragmentManager().findFragmentById(R.id.frame_content);

                                if (fragment instanceof SousvideStatusFragment) {
                                    ((SousvideStatusFragment) fragment).updateUiCurrentTemp();
                                }
                                else if (fragment instanceof MultiStageStatusFragment) {
                                    ((MultiStageStatusFragment) fragment).updateUiCurrentTemp();
                                }
                                else {
                                    //do nothing
                                }

                            }
                        });
                    }
                }).start();
                break;

            case ParagonValues.CHARACTERISTIC_BATTERY_LEVEL:
                Log.d(TAG, "CHARACTERISTIC_BATTERY_LEVEL :" + String.format("%02x", value[0]));
                productInfo.setErdBatteryLevel(byteBuffer.get());

                break;

            case ParagonValues.CHARACTERISTIC_ELAPSED_TIME:
                Log.d(TAG, "CHARACTERISTIC_ELAPSED_TIME :" + String.format("%02x%02x", value[0], value[1]));
                String buffer = String.format("%02x%02x", value[0], value[1]);
                productInfo.setErdElapsedTime(Integer.parseInt(buffer, 16));
                onElapsedTime();
                break;


            case ParagonValues.CHARACTERISTIC_COOK_CONFIGURATION:
                Log.d(TAG, "CHARACTERISTIC_COOK_CONFIGURATION :");

                String data = "";
                for (int i = 0; i < 39; i++) {
                    data += String.format("%02x", value[i]);
                }
                Log.d(TAG, "CONFIGURATION Data :" + data);

                RecipeInfo newRecipe = new RecipeInfo(value);
                productInfo.setErdRecipeConfig(newRecipe);
                break;


            case ParagonValues.CHARACTERISTIC_BURNER_STATUS:
                Log.d(TAG, "CHARACTERISTIC_BURNER_STATUS :" + String.format("%02x", value[0]));
                productInfo.setErdBurnerStatus(byteBuffer.get());
                break;


            case ParagonValues.CHARACTERISTIC_CURRENT_COOK_STATE:
                Log.d(TAG, "CHARACTERISTIC_CURRENT_COOK_STATE :" + String.format("%02x", value[0]));
                productInfo.setErdCookState(byteBuffer.get());
                onCookState();
                break;


            case ParagonValues.CHARACTERISTIC_CURRENT_COOK_STAGE:
                Log.d(TAG, "CHARACTERISTIC_CURRENT_COOK_STAGE :" + String.format("%02x", value[0]));
                productInfo.setErdCookStage(byteBuffer.get());
                onCookStage();
                break;


            case ParagonValues.CHARACTERISTIC_COOK_MODE:
                Log.d(TAG, "CHARACTERISTIC_COOK_MODE :" + String.format("%02x", value[0]));
                productInfo.setErdCurrentCookMode(byteBuffer.get());

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Fragment fragment = getFragmentManager().findFragmentById(R.id.frame_content);

                                if (fragment instanceof GetReadyFragment) {
                                    ((GetReadyFragment) fragment).onCookModeChanged();
                                }
                                else {
                                    //do nothing
                                }

                            }
                        });
                    }
                }).start();

                break;


            case ParagonValues.CHARACTERISTIC_OTA_VERSION:
                Log.d(TAG, "CHARACTERISTIC_OTA_VERSION :" + String.format("%02x%02x%02x%02x%02x%02x", value[0], value[1], value[2], value[3], value[4], value[5]));
                if (OtaManager.getInstance().compareVersion(value[2], value[3], (short) value[4])) {
                    if (dialogWaiting.isShowing()) {
                        dialogWaiting.dismiss();
                    }


                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    dialogOtaAsk.show();

                                }
                            });
                        }
                    }).start();

                }
                else {
                    Log.d(TAG, "No need to update");

                    requestInitialValues();

                }
                break;

            case ParagonValues.CHARACTERISTIC_OTA_COMMAND:
                Log.d(TAG, "CHARACTERISTIC_OTA_COMMAND :" + String.format("%02x", value[0]));
                OtaManager.getInstance().getResponse(value[0]);
                break;


            case ParagonValues.CHARACTERISTIC_ERROR_STATE:
                Log.d(TAG, "CHARACTERISTIC_ERROR_STATE :" + String.format("%02x", value[0]));
                break;

            case ParagonValues.CHARACTERISTIC_PROBE_CONNECTION_STATE:
                Log.d(TAG, "CHARACTERISTIC_PROBE_CONNECTION_STATE :" + String.format("%02x", value[0]));
                productInfo.setErdProbeConnectionStatue(byteBuffer.get());
                break;

            case ParagonValues.CHARACTERISTIC_CURRENT_POWER_LEVEL:
                Log.d(TAG, "CHARACTERISTIC_CURRENT_POWER_LEVEL :" + String.format("%02x", value[0]));
                productInfo.setErdPowerLevel(byteBuffer.get());
                onPowerLevelChanged();
                break;



        }
    }


    private void onWriteData(String uuid, byte[] value) {

        switch (uuid.toUpperCase()) {
            case ParagonValues.CHARACTERISTIC_OTA_DATA:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialogOtaProcessing.setMaxProgress(OtaManager.getInstance().getTransferCount());
                                dialogOtaProcessing.setProgress(OtaManager.getInstance().getTransferOffset());
                            }
                        });
                    }
                }).start();

                OtaManager.getInstance().responseWriteData();
                break;

            case ParagonValues.CHARACTERISTIC_COOK_CONFIGURATION:
                break;

        }

        writeDataState = WRITE_STATE_WRITE_DONE;
    }


    private void onPowerLevelChanged() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Fragment fragment = getFragmentManager().findFragmentById(R.id.frame_content);

                        if (fragment instanceof DirectStatusFragment) {
                            ((DirectStatusFragment) fragment).updateUiPowerLevel();
                        }
                        else {
                            //do nothing.
                        }


                    }
                });
            }
        }).start();
    }


    private void onCookStage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Fragment fragment = getFragmentManager().findFragmentById(R.id.frame_content);

                        if (fragment instanceof MultiStageStatusFragment) {
                            ((MultiStageStatusFragment) fragment).updateCookStage();
                        }
                        else if (fragment instanceof SousvideStatusFragment) {
                            ((SousvideStatusFragment) fragment).updateCookStage();
                        }
                        else {
                            //do nothing.
                        }


                    }
                });
            }
        }).start();

    }

    private void onCookState() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Fragment fragment = getFragmentManager().findFragmentById(R.id.frame_content);

                        if (fragment instanceof SousvideStatusFragment) {
                            ((SousvideStatusFragment) fragment).updateCookState();
                        }
                        else if (fragment instanceof MultiStageStatusFragment) {
                            ((MultiStageStatusFragment) fragment).updateCookState();
                        }
                        else if (fragment instanceof DirectStatusFragment) {
                            ((DirectStatusFragment) fragment).updateCookState();
                        }
                        else if (fragment instanceof GetReadyFragment) {
                            nextStep(ParagonSteps.STEP_COOK_STATUS);
                        }
                        else {
                            // do nothing.
                        }

                    }
                });
            }
        }).start();

    }

    private void onElapsedTime() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Fragment fragment = getFragmentManager().findFragmentById(R.id.frame_content);

                        if (fragment instanceof SousvideStatusFragment) {
                            ((SousvideStatusFragment) fragment).updateUiElapsedTime();
                        }
                        else if (fragment instanceof MultiStageStatusFragment) {
                            ((MultiStageStatusFragment) fragment).updateUiElapsedTime();
                        }
                        else {
                            // do nothing.
                        }

                    }
                });
            }
        }).start();
    }


    /**
     * @param step
     */
    public void nextStep(ParagonSteps step) {
        Fragment fragment = null;
        ProductInfo productInfo = ProductManager.getInstance().getCurrent();

        currentStep = step;

        switch (currentStep) {
            case STEP_CHECK_CURRENT_STATUS:
                isCheckingCurrentStatus = true;
                return;

            case STEP_COOKING_MODE:
                loadRecipesFromAsset();

                getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

                fragment = new SelectModeFragment();
                break;

            case STEP_SOUSVIDE_SETTINGS:
                fragment = new RecipeSettingsFragment();
                break;

            case STEP_SOUSVIDE_GETREADY:
                BleManager.getInstance().readCharacteristics(productInfo.bluetoothDevice,
                        ParagonValues.CHARACTERISTIC_CURRENT_TEMPERATURE);
                BleManager.getInstance().readCharacteristics(productInfo.bluetoothDevice,
                        ParagonValues.CHARACTERISTIC_ELAPSED_TIME);

                BleManager.getInstance().setCharacteristicNotification(productInfo.bluetoothDevice,
                        ParagonValues.CHARACTERISTIC_CURRENT_TEMPERATURE, true);
                BleManager.getInstance().setCharacteristicNotification(productInfo.bluetoothDevice,
                        ParagonValues.CHARACTERISTIC_ELAPSED_TIME, true);

                fragment = new GetReadyFragment();
                break;

            case STEP_COOK_STATUS:
                BleManager.getInstance().readCharacteristics(productInfo.bluetoothDevice,
                        ParagonValues.CHARACTERISTIC_CURRENT_TEMPERATURE);
                BleManager.getInstance().readCharacteristics(productInfo.bluetoothDevice,
                        ParagonValues.CHARACTERISTIC_ELAPSED_TIME);


                byte cookMode = productInfo.getErdCurrentCookMode();

                if(cookMode == ParagonValues.CURRENT_COOK_MODE_DIRECT){
                    BleManager.getInstance().readCharacteristics(productInfo.bluetoothDevice,
                            ParagonValues.CHARACTERISTIC_CURRENT_POWER_LEVEL);

                    fragment = new DirectStatusFragment();
                }
                else{
                    fragment = new SousvideStatusFragment();
                }

                //TODO: block this code until multi-stage.
//                if (recipeInfo.numStage() == 1) {
//                    fragment = new SousvideStatusFragment();
//                }
//                else {
//                    fragment = new MultiStageStatusFragment();
//                }
                break;

            case STEP_SOUSVIDE_COMPLETE:
                fragment = new CompleteFragment();
                break;

            case STEP_QUICK_START:
                fragment = new QuickStartFragment();
                break;

            case STEP_MY_RECIPES:
                fragment = new MyRecipesFragment();
                break;

            case STEP_EDIT_RECIPES:
                fragment = new RecipeEditFragment();
                setTitle("Edit");
                break;

            case STEP_EDIT_STAGE:
                //TODO: block this until multi-stage enabled.
//                fragment = new StageEditFragment();
//                int index = RecipeManager.getInstance().getCurrentStageIndex();
//
//                if (index == RecipeManager.INVALID_INDEX) {
//                    setTitle("New Stage");
//                }
//                else {
//                    setTitle("Stage " + (index + 1));
//                }
                break;

            case STEP_VIEW_RECIPE:
                fragment = new RecipeViewFragment();
                break;

            case STEP_VIEW_STAGE:
                fragment = new StageViewFragment();
                break;

            case STEP_ADD_RECIPE_MUTISTAGE:
                fragment = new RecipeEditFragment();
                setTitle("Multi-Stage");
                break;

            case STEP_ADD_RECIPE_SOUSVIDE:
                fragment = new SousVideEditFragment();
                setTitle("Sous vide");
                break;

            case STEP_ADD_SOUSVIDE_SETTING:
                fragment = new QuickStartFragment();
                setTitle("Sous Vide Settings");
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

    /**
     * Set title.
     *
     * @param title String to be title
     */
    protected void setTitle(String title) {
        if (title.equals("Paragon")) {
            toolbarText.setVisibility(View.GONE);
            toolbarImage.setVisibility(View.VISIBLE);
        }
        else {
            toolbarText.setVisibility(View.VISIBLE);
            toolbarImage.setVisibility(View.GONE);

            toolbarText.setText(title);
        }

    }

    boolean getBit(int value, int bit) {
        return (value & (1 << bit)) != 0;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d(TAG, "onCreate IN");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paragon_main);

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        toolbarText = (TextView) toolbar.findViewById(R.id.toolbar_title_text);
        toolbarImage = (ImageView) toolbar.findViewById(R.id.toolbar_title_image);


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

        OtaManager.getInstance().readImageFile(ParagonMainActivity.this);

        // Add ble event listener
        BleManager.getInstance().addListener(bleListener);


        isCheckingCurrentStatus = false;

        dialogWaiting = new MaterialDialog.Builder(ParagonMainActivity.this)
                .title("Please wait")
                .content("Communicating with Paragon...")
                .progress(true, 0)
                .cancelable(false).build();

        dialogGoodToGo = new MaterialDialog.Builder(ParagonMainActivity.this)
                .title("")
                .content("")
                .progress(false, 100)
                .negativeText("Cancel")
                .cancelable(false)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        handlerCheckingGoodToGo.removeCallbacks(runnableGoodToGo);
                    }

                    @Override
                    public void onNeutral(MaterialDialog dialog) {
                    }
                })
                .build();


        dialogOtaAsk = new MaterialDialog.Builder(ParagonMainActivity.this)
                .title("Update Available")
                .content("Are you sure you want to update Paragon now?")
                .positiveText("Yes")
                .negativeText("No")
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        OtaManager.getInstance().startProcess();
                        showOtaDialog();
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        dialogWaiting.show();
                        requestInitialValues();
                    }

                    @Override
                    public void onNeutral(MaterialDialog dialog) {
                    }
                })
                .cancelable(false).build();


        handlerCheckingConnection = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                checkParagonConnectionStatus();
            }
        };


        handlerCheckingGoodToGo = new Handler();
        runnableGoodToGo = new Runnable() {
            @Override
            public void run() {
                checkGoodToGoLoop();
            }
        };


        // Check bluetooth adapter. If the adapter is disabled, enable it
        boolean result = BleManager.getInstance().isBluetoothEnabled();

        if (!result) {
            Log.d(TAG, "Bluetooth adapter is disabled. Enable bluetooth adapter.");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, BleValues.REQUEST_ENABLE_BT);
        }
        else {
            Log.d(TAG, "Bluetooth adapter is already enabled. Start connect");
            startCommunicateParagon();
        }

        disconnectDialog = new MaterialDialog.Builder(ParagonMainActivity.this)
                .title("Bluetooth Disconnected")
                .content("Phone is out of range. Trying reconnect...")
                .progress(true, 0)
                .cancelable(false)
                .negativeText("Cancel")
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        BleManager.getInstance().removeListener(bleListener);
                        finish();
                    }

                    @Override
                    public void onNeutral(MaterialDialog dialog) {
                    }
                })
                .build();

    }

    private void checkParagonConnectionStatus() {
        Log.d(TAG, "checkParagonConnectionStatus");

        ProductInfo product = ProductManager.getInstance().getCurrent();

        Log.d(TAG, "checkParagonConnectionStatus buner :"+product.getErdBurnerStatus());
        Log.d(TAG, "checkParagonConnectionStatus cookmode :"+product.getErdCurrentCookMode());

        if (product.getErdBurnerStatus() != INITIAL_VALUE &&
                product.getErdCurrentCookMode() != INITIAL_VALUE &&
                product.getErdRecipeConfig() != null ) {

            Log.d(TAG, "checkParagonConnectionStatus ..1");

            dialogWaiting.dismiss();

            if (product.getErdBurnerStatus() == ParagonValues.BURNER_STATE_START &&
                    product.getErdCurrentCookMode() != ParagonValues.CURRENT_COOK_MODE_OFF ) {
                Log.d(TAG, "checkParagonConnectionStatus ..2");
                nextStep(ParagonSteps.STEP_COOK_STATUS);
            }
            else {
                Log.d(TAG, "checkParagonConnectionStatus ..3");
                nextStep(ParagonSteps.STEP_COOKING_MODE);
            }
        }
        else {
            Log.d(TAG, "checkParagonConnectionStatus ..4");
            checkingCountDown--;

            if (checkingCountDown == 0) {

                if (dialogWaiting.isShowing()) {
                    dialogWaiting.dismiss();

                    new MaterialDialog.Builder(ParagonMainActivity.this)
                            .title("Not connected with Paragon")
                            .content("Are you sure?")
                            .positiveText("Try again")
                            .negativeText("No")
                            .callback(new MaterialDialog.ButtonCallback() {
                                @Override
                                public void onPositive(MaterialDialog dialog) {
                                    startCommunicateParagon();
                                }

                                @Override
                                public void onNegative(MaterialDialog dialog) {
                                    BleManager.getInstance().removeListener(bleListener);
                                    finish();
                                }

                                @Override
                                public void onNeutral(MaterialDialog dialog) {
                                }
                            })
                            .cancelable(false).show();
                }
            }
            else {
                handlerCheckingConnection.postDelayed(runnable, INTERVAL_CHECKING_PARAGON_CONNECTION);
            }
        }


    }

    private void showOtaDialog() {
        dialogOtaProcessing = new MaterialDialog.Builder(this)
                .title(R.string.popup_ota_title)
                .content(R.string.popup_ota_content)
                .contentGravity(GravityEnum.CENTER)
                .progress(false, 100, true)
                .cancelable(false)
                .showListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {

                    }
                }).build();

        dialogOtaProcessing.show();
    }

    private void requestInitialValues() {
        ProductInfo productInfo = ProductManager.getInstance().getCurrent();

//        BleManager.getInstance().readCharacteristics(productInfo.bluetoothDevice, ParagonValues.CHARACTERISTIC_COOK_CONFIGURATION);
//        BleManager.getInstance().readCharacteristics(productInfo.bluetoothDevice, ParagonValues.CHARACTERISTIC_BURNER_STATUS);
//        BleManager.getInstance().readCharacteristics(productInfo.bluetoothDevice, ParagonValues.CHARACTERISTIC_COOK_MODE);
//        BleManager.getInstance().readCharacteristics(productInfo.bluetoothDevice, ParagonValues.CHARACTERISTIC_CURRENT_COOK_STATE);
//        BleManager.getInstance().readCharacteristics(productInfo.bluetoothDevice, ParagonValues.CHARACTERISTIC_ERROR_STATE);

        BleManager.getInstance().setCharacteristicNotification(productInfo.bluetoothDevice, ParagonValues.CHARACTERISTIC_ERROR_STATE, true);
        BleManager.getInstance().setCharacteristicNotification(productInfo.bluetoothDevice, ParagonValues.CHARACTERISTIC_CURRENT_COOK_STAGE, true);
        BleManager.getInstance().setCharacteristicNotification(productInfo.bluetoothDevice, ParagonValues.CHARACTERISTIC_CURRENT_COOK_STATE, true);
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

            Intent intent = new Intent(this, SettingsActivity.class);

            intent.putExtra("SelectedMenu", "MenuSettings");
            startActivity(intent);
            return true;
        }
        else if (id == R.id.action_about) {
            Intent intent = new Intent(this, SettingsActivity.class);

            intent.putExtra("SelectedMenu", "MenuAbout");
            startActivity(intent);
            return true;
        }
        else if (id == R.id.action_my_product) {
            BleManager.getInstance().removeListener(bleListener);
            finish();
            return true;
        }
        else if (id == R.id.action_help) {
            String url = getResources().getString(R.string.url_manual);
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            return true;
        }
        else if (id == R.id.action_feedback) {
            final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

            emailIntent.setType("plain/text");
            emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"paragon@firstbuild.com"});
            emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Feedback Paragon] ");
            emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Text");

            startActivity(Intent.createChooser(emailIntent, "Send mail..."));
        }
        else if (id == android.R.id.home) {
            onBackPressed();

            return true;
        }
        else {
            // do nothing.
        }


        return super.onOptionsItemSelected(item);
    }

    public void finishParagonMain() {
//        BleManager.getInstance().disconnect();
        BleManager.getInstance().removeListener(bleListener);
        finish();
    }

    /**
     * Call when OTA completed successfully.
     */
    public void succeedOta() {
        if (dialogOtaProcessing.isShowing()) {
            dialogOtaProcessing.dismiss();
        }
        else {
            // do nothing.
        }

        // Scan again.
//        BleManager.getInstance().disconnect();
        BleManager.getInstance().removeListener(bleListener);
        finish();
    }

    /**
     * CAll when OTA get failed.
     */
    public void failedOta() {

    }

    /**
     * Take picture and store in file.
     */
    protected void dispatchTakePictureIntent() {
        Log.d(TAG, "dispatchTakePictureIntent IN");

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            }
            catch (IOException ex) {
                // Error occurred while creating the File
                Log.d(TAG, "dispatchTakePictureIntent error : " + ex);
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    /**
     * Create image file for recipe.
     *
     * @return String dir + file name of image file.
     * @throws IOException
     */
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            options.inSampleSize = 4;
            Bitmap imageBitmap = BitmapFactory.decodeFile(currentPhotoPath, options);
            Fragment fragment = getFragmentManager().findFragmentById(R.id.frame_content);

            if (fragment instanceof RecipeEditFragment) {
                ((RecipeEditFragment) fragment).setRecipeImage(imageBitmap, currentPhotoPath);
            }
        }
    }

    @Override
    public void onBackPressed() {

        FragmentManager fm = getFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.frame_content);

        if (fragment instanceof SelectModeFragment) {
            ((SelectModeFragment) fragment).onBackPressed();
        }
        else if(fragment instanceof SousvideStatusFragment) {
            return;

        }
        else if (fm.getBackStackEntryCount() > 1) {
            fm.popBackStack();
        }
        else {
            finishParagonMain();
        }
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();

        if (disconnectDialog.isShowing()) {
            Log.d(TAG, "Stop reconnect");
        }

        handlerCheckingConnection.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();

        if (disconnectDialog.isShowing()) {
            Log.d(TAG, "Try to reconnect again");
//            BleManager.getInstance().connect(ProductManager.getInstance().getCurrent().address);
        }
        else {
            Log.d(TAG, "disconnectDialog is not Showing");
        }
    }


    private void startCommunicateParagon() {
        Log.d(TAG, "startCommunicateParagon IN");
        ProductInfo productInfo = ProductManager.getInstance().getCurrent();

        handlerCheckingConnection.postDelayed(runnable, INTERVAL_CHECKING_PARAGON_CONNECTION);

        // Get Initial values.
        BleManager.getInstance().setCharacteristicNotification(productInfo.bluetoothDevice, ParagonValues.CHARACTERISTIC_OTA_COMMAND, true);
        BleManager.getInstance().readCharacteristics(productInfo.bluetoothDevice, ParagonValues.CHARACTERISTIC_OTA_VERSION);

        nextStep(ParagonSteps.STEP_CHECK_CURRENT_STATUS);

        dialogWaiting.setContent("Communicating...");
        dialogWaiting.show();

        checkingCountDown = MAX_WAITING_INIT_TIME;
        Log.d(TAG, "startCommunicateParagon OUT");
    }


    /**
     * Get recipe's title image from file.
     *
     * @param imageFileName File name of external storage.
     */
    public void loadImageFromFile(String imageFileName) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inSampleSize = 4;
        Bitmap imageBitmap;
        try {
            imageBitmap = BitmapFactory.decodeFile(imageFileName, options);

        }
        catch (Exception e) {
            imageBitmap = null;
        }

        Fragment fragment = getFragmentManager().findFragmentById(R.id.frame_content);

        if (fragment instanceof RecipeViewFragment) {
            ((RecipeViewFragment) fragment).setRecipeImage(imageBitmap);
        }
    }

    /**
     * Read built-in receipes from asset file.
     */
    public void loadRecipesFromAsset() {
        String json = null;
        try {
            InputStream is = getAssets().open("recipes/builtin.JSON");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }

        try {
            builtInRecipes = new BuiltInRecipeInfo("root");
            builtInRecipes.child = new ArrayList<>();

            buildRecipeLinks(builtInRecipes, new JSONObject(json));
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

    }

    /**
     * Built recipe links
     *
     * @param parent     parent of BuiltInRecipeInfo.
     * @param rootObject JSON object.
     */
    public void buildRecipeLinks(BuiltInRecipeInfo parent, JSONObject rootObject) {

        try {
            JSONArray arrayJson = rootObject.getJSONArray("recipes");

            for (int j = 0; j < arrayJson.length(); j++) {
                JSONObject childObj = arrayJson.getJSONObject(j);
                String name = childObj.getString("name");

                if (childObj.has("recipes")) {
                    BuiltInRecipeInfo foodsInfo = new BuiltInRecipeInfo(name);
                    foodsInfo.child = new ArrayList<>();
                    parent.child.add(foodsInfo);
                    foodsInfo.parent = parent;

                    buildRecipeLinks(foodsInfo, childObj);
                }
                else {
                    BuiltInRecipeSettingsInfo settingsInfo = new BuiltInRecipeSettingsInfo(name);
                    settingsInfo.id = childObj.getInt("id");
                    settingsInfo.parent = parent;

                    JSONArray arrayDoneness = childObj.getJSONArray("doneness");
                    for (int i = 0; i < arrayDoneness.length(); i++) {
                        settingsInfo.doneness.add(arrayDoneness.getString(i));
                    }


                    if (childObj.has("thickness")) {
                        JSONArray arrayThickness = childObj.getJSONArray("thickness");

                        for (int i = 0; i < arrayThickness.length(); i++) {
                            settingsInfo.thickness.add((float) arrayThickness.getDouble(i));
                        }
                    }
                    else {
                        // do nothing.
                    }

                    JSONArray arrayTemp = childObj.getJSONArray("temp");
                    JSONArray arrayTimeMin = childObj.getJSONArray("time min");
                    JSONArray arrayTimeMax = childObj.getJSONArray("time max");


                    for (int i = 0; i < arrayTemp.length(); i++) {
                        int temp = arrayTemp.getInt(i);
                        float timeMin = (float) arrayTimeMin.getDouble(i);
                        float timeMax = (float) arrayTimeMax.getDouble(i);

                        settingsInfo.addRecipeSetting(temp, timeMin, timeMax);
                    }

                    parent.child.add(settingsInfo);
                }
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void checkGoodToGo() {
        Log.d(TAG, "checkGoodToGo");

        dialogGoodToGo.setProgress(0);
        checkGoodToGoLoop();

        writeDataState = WRITE_STATE_NONE;

    }

    private void checkGoodToGoLoop() {

        ProductInfo productInfo = ProductManager.getInstance().getCurrent();

        boolean isReady = false;
        boolean isAllRight = false;


        if (productInfo.getErdBurnerStatus() == ParagonValues.BURNER_STATE_START) {
            dialogGoodToGo.setTitle("Press Stop on Paragon");
            dialogGoodToGo.setContent("The Paragon is currently cooking. Please press Stop on the Paragon.");

            isReady = false;
        }
        else if (productInfo.isProbeConnected() == false) {
            dialogGoodToGo.setTitle("Connect Probe");
            dialogGoodToGo.setContent("Probe is not connected. Please connect the temperature probe by holding the button on the side of the probe FOR 3 SECONDS.");

            isReady = false;
        }
        else if (productInfo.getErdCurrentCookMode() != ParagonValues.CURRENT_COOK_MODE_RAPID &&
                productInfo.getErdCurrentCookMode() != ParagonValues.CURRENT_COOK_MODE_MULTISTEP) {
            dialogGoodToGo.setTitle("Select Rapid Precise");
            dialogGoodToGo.setContent("Please press Rapid Precise on the cooktop.");

            isReady = false;
        }
        else {
            isReady = true;
        }


        if(isReady){
            Log.d(TAG, "checkGoodToGoLoop isReady");

            switch(writeDataState){
                case WRITE_STATE_NONE:
                    Log.d(TAG, "checkGoodToGoLoop isReady Write None");
                    Fragment fragment = getFragmentManager().findFragmentById(R.id.frame_content);

                    if (fragment instanceof QuickStartFragment) {
                        ((QuickStartFragment) fragment).sendRecipeConfig();
                    }
                    else if (fragment instanceof RecipeSettingsFragment) {
                        ((RecipeSettingsFragment) fragment).sendRecipeConfig();
                    }
                    else {

                    }
                    writeDataState = WRITE_STATE_WRITING;
                    break;

                case WRITE_STATE_WRITING:
                    Log.d(TAG, "checkGoodToGoLoop isReady Write Writing");
                    break;

                case WRITE_STATE_WRITE_DONE:
                    Log.d(TAG, "checkGoodToGoLoop isReady Write Done");
                    isAllRight = true;
                    break;
            }
        }


        int progress = dialogGoodToGo.getCurrentProgress();

        if (isAllRight) {
            if (dialogGoodToGo.isShowing()) {
                dialogGoodToGo.dismiss();
            }

            Fragment fragment = getFragmentManager().findFragmentById(R.id.frame_content);

            if (fragment instanceof QuickStartFragment) {
                ((QuickStartFragment) fragment).goodToGo();
            }
            else if (fragment instanceof RecipeSettingsFragment) {
                ((RecipeSettingsFragment) fragment).goodToGo();
            }
            else {

            }

            // remove runnable handler.
            handlerCheckingGoodToGo.removeCallbacks(runnableGoodToGo);
            return;
        }

        if (progress >= dialogGoodToGo.getMaxProgress() ) {
            // if timer has expiered then mismiss popup.
            if (dialogGoodToGo.isShowing()) {
                dialogGoodToGo.dismiss();
            }

            // remove runnable handler.
            handlerCheckingGoodToGo.removeCallbacks(runnableGoodToGo);

        }
        else {
            progress++;
            dialogGoodToGo.setProgress(progress);
            if (!dialogGoodToGo.isShowing()) {
                dialogGoodToGo.show();
            }

            handlerCheckingGoodToGo.postDelayed(runnableGoodToGo, INTERVAL_GOODTOGO);
        }

    }


    public boolean isShowFoodWarning() {
        SharedPreferences settings = FirstBuildApplication.getContext().getSharedPreferences(
                ProductManager.PREFS_NAME, Context.MODE_PRIVATE);

        boolean isShowFoodWarning = settings.getBoolean(PREF_KEY_FOOD_WARNING, false);

        return isShowFoodWarning;
    }

    public void saveShowFoodWarning() {
        SharedPreferences settings = FirstBuildApplication.getContext().getSharedPreferences(
                ProductManager.PREFS_NAME, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(PREF_KEY_FOOD_WARNING, true);
        editor.commit();
    }


    public enum ParagonSteps {
        STEP_NONE,
        STEP_CHECK_CURRENT_STATUS,
        STEP_COOKING_MODE,
        STEP_SOUSVIDE_SETTINGS,
        STEP_SOUSVIDE_GETREADY,
        STEP_COOK_STATUS,
        STEP_SOUSVIDE_COMPLETE,
        STEP_QUICK_START,
        STEP_MY_RECIPES,
        STEP_EDIT_RECIPES,
        STEP_EDIT_STAGE,
        STEP_VIEW_RECIPE,
        STEP_VIEW_STAGE,
        STEP_ADD_RECIPE_MUTISTAGE,
        STEP_ADD_RECIPE_SOUSVIDE,
        STEP_ADD_SOUSVIDE_SETTING
    }

}
