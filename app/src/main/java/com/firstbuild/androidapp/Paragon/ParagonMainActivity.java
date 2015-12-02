package com.firstbuild.androidapp.paragon;

import android.app.Fragment;
import android.app.FragmentManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.firstbuild.androidapp.ParagonValues;
import com.firstbuild.androidapp.R;
import com.firstbuild.androidapp.paragon.dataModel.RecipeInfo;
import com.firstbuild.androidapp.paragon.dataModel.RecipeManager;
import com.firstbuild.androidapp.paragon.navigation.NavigationDrawerFragment;
import com.firstbuild.androidapp.paragon.settings.SettingsActivity;
import com.firstbuild.commonframework.bleManager.BleListener;
import com.firstbuild.commonframework.bleManager.BleManager;
import com.firstbuild.commonframework.bleManager.BleValues;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class ParagonMainActivity extends ActionBarActivity {
    public static final int INTERVAL_CHECKING_PARAGON_CONNECTION = 20000;
    static final int REQUEST_TAKE_PHOTO = 1;
    static final byte INITIAL_VALUE = 0x0f;
    Toolbar toolbar;
    private String TAG = ParagonMainActivity.class.getSimpleName();
    private String REQUEST_METHOD_READ = "READ";
    private String REQUEST_METHOD_NOTIFICATION = "NOTIFICATION";
    // Bluetooth adapter handler
    private BluetoothAdapter bluetoothAdapter = null;
    private ParagonSteps currentStep = ParagonSteps.STEP_NONE;
    private float currentTemp;
    private byte batteryLevel;
    private byte burnerStatus = INITIAL_VALUE;
    private byte cookMode = INITIAL_VALUE;
    private Queue requestQueue = new LinkedList();
    private boolean isCheckingCurrentStatus = false;
    // Thread handler for checking the connection with Paragon Master.
    private Handler handlerCheckingConnection;
    // Thread for update UI.
    private Runnable runnable;
    private int MAX_BURNER = 5;
    // Navigation drawer.
    private NavigationDrawerFragment drawerFragment;
    private TextView toolbarText;
    private ImageView toolbarImage;
    private String currentPhotoPath;
    private MaterialDialog dialogWaiting;
    private MaterialDialog dialogOtaProcessing;
    private MaterialDialog dialogOtaAsk;

    private BleListener bleListener = new BleListener() {
        @Override
        public void onScanDevices(HashMap<String, BluetoothDevice> bluetoothDevices) {
            super.onScanDevices(bluetoothDevices);

            Log.d(TAG, "onScanDevices IN");

            Log.d(TAG, "bluetoothDevices size: " + bluetoothDevices.size());
//            for (Map.Entry<String, BluetoothDevice> entry : bluetoothDevices.entrySet()) {
//
//                // Retrieves address and name
//                BluetoothDevice device = entry.getValue();
//                String address = device.getAddress();
//                String name = device.getName();
//
//                Log.d(TAG, "------------------------------------");
//                Log.d(TAG, "Device address: " + address);
//                Log.d(TAG, "Device name: " + name);
//
//                if (ParagonValues.TARGET_DEVICE_NAME.equals(name)) {
//                    Log.d(TAG, "device found: " + device.getName());
//
//                    dialogWaiting.setContent("Communicating...");
//
//                    // Connect to device
//                    BleManager.getInstance().connect(address);
//
//                    // Stop ble device scanning
//                    BleManager.getInstance().stopScan();
//
//                    nextStep(ParagonSteps.STEP_CHECK_CURRENT_STATUS);
//                    break;
//                }
//            }
            Log.d(TAG, "====================================");
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

//            // Get Initial values.
//            BleManager.getInstance().readCharacteristics(ParagonValues.CHARACTERISTIC_OTA_VERSION);
//            BleManager.getInstance().setCharacteristicNotification(ParagonValues.CHARACTERISTIC_OTA_COMMAND, true);
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

            if (uuid.toUpperCase().equals(ParagonValues.CHARACTERISTIC_OTA_DATA)) {
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
            }

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

    public MaterialDialog getDialogOtaProcessing() {
        return dialogOtaProcessing;
    }

    private void nextCharacteristicRead() {

        String nextRequest = (String) requestQueue.poll();

        if (nextRequest != null) {
            String method = nextRequest.split("/")[0];
            String characteristic = nextRequest.split("/")[1];

            if (method.equals(REQUEST_METHOD_READ)) {
                BleManager.getInstance().readCharacteristics(characteristic);
            }
            else if (method.equals(REQUEST_METHOD_NOTIFICATION)) {
                BleManager.getInstance().setCharacteristicNotification(characteristic, true);
            }
            else {

            }

        }
    }

    public float getCurrentTemp() {
        return currentTemp;
    }

    private void onReceivedData(String uuid, byte[] value) {

        Log.d(TAG, "onReceivedData :" + uuid);

        if (value == null) {
            Log.d(TAG, "onReceivedData :value is null");
            return;
        }

        ByteBuffer byteBuffer = ByteBuffer.wrap(value);


        switch (uuid.toUpperCase()) {
            case ParagonValues.CHARACTERISTIC_CURRENT_TEMPERATURE:
                currentTemp = (byteBuffer.getShort() / 100.0f);
                Log.d(TAG, "CHARACTERISTIC_CURRENT_TEMPERATURE :" + currentTemp);

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

            case ParagonValues.CHARACTERISTIC_REMAINING_TIME:
                Log.d(TAG, "CHARACTERISTIC_REMAINING_TIME :" + byteBuffer.getShort());
                String buffer = String.format("%02x%02x", value[0], value[1]);
                int remainingTime = Integer.parseInt(buffer, 16);
                onElapsedTime(remainingTime);

                break;


            case ParagonValues.CHARACTERISTIC_COOK_CONFIGURATION:
                Log.d(TAG, "CHARACTERISTIC_COOK_CONFIGURATION :");

                for (int i = 0; i < 39; i++) {
                    Log.d(TAG, "CHARACTERISTIC_COOK_CONFIGURATION :" + String.format("%02x", value[i]));
                }

                onCookConfiguration(value);
                break;


            case ParagonValues.CHARACTERISTIC_BURNER_STATUS:
                Log.d(TAG, "CHARACTERISTIC_BURNER_STATUS :" + String.format("%02x", value[0]));

                burnerStatus = value[0];
                checkInitialStatus();

                break;


            case ParagonValues.CHARACTERISTIC_CURRENT_COOK_STATE:
                Log.d(TAG, "CHARACTERISTIC_CURRENT_COOK_STATE :" + String.format("%02x", value[0]));
                onCookState(value[0]);

                break;


            case ParagonValues.CHARACTERISTIC_CURRENT_COOK_STAGE:
                Log.d(TAG, "CHARACTERISTIC_CURRENT_COOK_STAGE :" + String.format("%02x", value[0]));
                onCookStage(value[0]);
                break;


            case ParagonValues.CHARACTERISTIC_COOK_MODE:
                Log.d(TAG, "CHARACTERISTIC_COOK_MODE :" + String.format("%02x", value[0]));
                cookMode = value[0];
                checkInitialStatus();
                break;


            case ParagonValues.CHARACTERISTIC_OTA_VERSION:
                Log.d(TAG, "ParagonValues.CHARACTERISTIC_OTA_VERSION :" + String.format("%02x%02x%02x%02x%02x%02x", value[0], value[1], value[2], value[3], value[4], value[5]));
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
                Log.d(TAG, "ParagonValues.CHARACTERISTIC_OTA_COMMAND :" + String.format("%02x", value[0]));
                OtaManager.getInstance().getResponse(value[0]);
                break;


            case ParagonValues.CHARACTERISTIC_ERROR_STATE:
                Log.d(TAG, "ParagonValues.CHARACTERISTIC_ERROR_STATE :" + String.format("%02x", value[0]));
                break;


        }
    }

    private void checkInitialStatus() {
        if (currentStep == ParagonSteps.STEP_CHECK_CURRENT_STATUS) {
            if (burnerStatus != INITIAL_VALUE && cookMode != INITIAL_VALUE) {

                dialogWaiting.dismiss();

                if (burnerStatus == ParagonValues.BURNER_STATE_START &&
                        cookMode == ParagonValues.CURRENT_COOK_MODE_MULTISTEP) {
                    nextStep(ParagonSteps.STEP_COOK_STATUS);
                }
                else {
                    nextStep(ParagonSteps.STEP_COOKING_MODE);
                }
            }
            else {
                // show timer icon and popup "connecting..."
            }
        }
        else {
            // do nothing
        }
    }

    /**
     * @param value
     */
    private void onCookConfiguration(byte[] value) {
        RecipeInfo newRecipe = new RecipeInfo(value);

        if (newRecipe.numStage() > 0) {
            RecipeManager.getInstance().setCurrentRecipe(newRecipe);
            RecipeManager.getInstance().setCurrentStage(0);

        }
    }

    private void onCookStage(byte stage) {
        final int newStage = (int) stage;

        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Fragment fragment = getFragmentManager().findFragmentById(R.id.frame_content);

                        if (fragment instanceof MultiStageStatusFragment) {
                            ((MultiStageStatusFragment) fragment).updateCookStage(newStage);
                        }
                        else if (fragment instanceof SousvideStatusFragment) {
                            ((SousvideStatusFragment) fragment).updateCookStage(newStage);
                        }
                        else {
                            //do nothing.
                        }


                    }
                });
            }
        }).start();

    }

    private void onCookState(final byte state) {

        if(state == ParagonValues.COOK_STATE_OFF){
            nextStep(ParagonMainActivity.ParagonSteps.STEP_COOKING_MODE);
        }
        else{
            new Thread(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Fragment fragment = getFragmentManager().findFragmentById(R.id.frame_content);

                            if (fragment instanceof SousvideStatusFragment) {
                                ((SousvideStatusFragment) fragment).updateCookStatus(state);
                            }
                            else if (fragment instanceof MultiStageStatusFragment) {
                                ((MultiStageStatusFragment) fragment).updateCookStatus(state);
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

    }

    private void onElapsedTime(int elapsedTime) {
        final int elapsedTimeValue = elapsedTime;

        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Fragment fragment = getFragmentManager().findFragmentById(R.id.frame_content);

                        if (fragment instanceof SousvideStatusFragment) {
                            ((SousvideStatusFragment) fragment).updateUiElapsedTime(elapsedTimeValue);
                        }
                        else if (fragment instanceof MultiStageStatusFragment) {
                            ((MultiStageStatusFragment) fragment).updateUiElapsedTime(elapsedTimeValue);
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

        currentStep = step;

        switch (currentStep) {
            case STEP_CHECK_CURRENT_STATUS:
                isCheckingCurrentStatus = true;
                return;

            case STEP_COOKING_MODE:
                BleManager.getInstance().setCharacteristicNotification(ParagonValues.CHARACTERISTIC_CURRENT_TEMPERATURE, false);
                BleManager.getInstance().setCharacteristicNotification(ParagonValues.CHARACTERISTIC_REMAINING_TIME, false);

                getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

                fragment = new SelectModeFragment();
                break;

            case STEP_SOUSVIDE_SETTINGS:
                fragment = new SettingsFragment();
                break;

            case STEP_SOUSVIDE_GETREADY:
                BleManager.getInstance().readCharacteristics(ParagonValues.CHARACTERISTIC_CURRENT_TEMPERATURE);
                BleManager.getInstance().readCharacteristics(ParagonValues.CHARACTERISTIC_REMAINING_TIME);

                BleManager.getInstance().setCharacteristicNotification(ParagonValues.CHARACTERISTIC_CURRENT_TEMPERATURE, true);
                BleManager.getInstance().setCharacteristicNotification(ParagonValues.CHARACTERISTIC_REMAINING_TIME, true);

                fragment = new GetReadyFragment();
                break;

            case STEP_COOK_STATUS:
                BleManager.getInstance().readCharacteristics(ParagonValues.CHARACTERISTIC_CURRENT_TEMPERATURE);
                BleManager.getInstance().readCharacteristics(ParagonValues.CHARACTERISTIC_REMAINING_TIME);

                BleManager.getInstance().setCharacteristicNotification(ParagonValues.CHARACTERISTIC_CURRENT_TEMPERATURE, true);
                BleManager.getInstance().setCharacteristicNotification(ParagonValues.CHARACTERISTIC_REMAINING_TIME, true);

                if (RecipeManager.getInstance().getCurrentRecipe().numStage() == 1) {
                    fragment = new SousvideStatusFragment();
                }
                else {
                    fragment = new MultiStageStatusFragment();
                }
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
                fragment = new StageEditFragment();
                int index = RecipeManager.getInstance().getCurrentStageIndex();

                if (index == RecipeManager.INVALID_INDEX) {
                    setTitle("New Stage");
                }
                else {
                    setTitle("Stage " + (index + 1));
                }
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

        // Initialize ble manager
//        BleManager.getInstance().initBleManager(this);

        // Add ble event listener
        BleManager.getInstance().addListener(bleListener);


        isCheckingCurrentStatus = false;

        dialogWaiting = new MaterialDialog.Builder(ParagonMainActivity.this)
                .title("Please wait")
                .content("Communicating with Paragon...")
                .progress(true, 0)
                .cancelable(false).build();


        dialogOtaAsk = new MaterialDialog.Builder(ParagonMainActivity.this)
                .title("Update Available")
                .content("Are you want to update Pararagon now?")
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


    }

    private void checkParagonConnectionStatus() {
        if(dialogWaiting.isShowing()){
            dialogWaiting.dismiss();

            new MaterialDialog.Builder(ParagonMainActivity.this)
                    .title("Not connected with Paragon")
                    .content("Are you sure?")
                    .positiveText("Try again")
                    .negativeText("No")
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
//                            BleManager.getInstance().disconnect();
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
        //TODO: do normal process. need to check.
        BleManager.getInstance().readCharacteristics(ParagonValues.CHARACTERISTIC_COOK_CONFIGURATION);
        BleManager.getInstance().readCharacteristics(ParagonValues.CHARACTERISTIC_BATTERY_LEVEL);
        BleManager.getInstance().readCharacteristics(ParagonValues.CHARACTERISTIC_BURNER_STATUS);
        BleManager.getInstance().readCharacteristics(ParagonValues.CHARACTERISTIC_REMAINING_TIME);
        BleManager.getInstance().readCharacteristics(ParagonValues.CHARACTERISTIC_COOK_MODE);
        BleManager.getInstance().readCharacteristics(ParagonValues.CHARACTERISTIC_CURRENT_COOK_STATE);

        BleManager.getInstance().setCharacteristicNotification(ParagonValues.CHARACTERISTIC_CURRENT_COOK_STAGE, true);
        BleManager.getInstance().setCharacteristicNotification(ParagonValues.CHARACTERISTIC_BATTERY_LEVEL, true);
        BleManager.getInstance().setCharacteristicNotification(ParagonValues.CHARACTERISTIC_CURRENT_COOK_STATE, true);

        BleManager.getInstance().setCharacteristicNotification(ParagonValues.CHARACTERISTIC_CURRENT_COOK_STATE, true);
        BleManager.getInstance().setCharacteristicNotification(ParagonValues.CHARACTERISTIC_ERROR_STATE, true);

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
            emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"master@firstbuild.com"});
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
        BleManager.getInstance().disconnect();
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

        if (fm.getBackStackEntryCount() > 1) {
            fm.popBackStack();
        }
        else {
            BleManager.getInstance().removeListener(bleListener);
            finish();
        }
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
        }
        else {
            Log.d(TAG, "Bluetooth adapter is already enabled. Start scanning.");
            startCommunicateParagon();
        }

    }


    private void startCommunicateParagon() {

        handlerCheckingConnection.postDelayed(runnable, INTERVAL_CHECKING_PARAGON_CONNECTION);

        // Get Initial values.
        BleManager.getInstance().readCharacteristics(ParagonValues.CHARACTERISTIC_OTA_VERSION);
        BleManager.getInstance().setCharacteristicNotification(ParagonValues.CHARACTERISTIC_OTA_COMMAND, true);

        nextStep(ParagonSteps.STEP_CHECK_CURRENT_STATUS);

        dialogWaiting.setContent("Communicating...");
        dialogWaiting.show();

    }

    private void startSearchParagon() {

        handlerCheckingConnection.postDelayed(runnable, INTERVAL_CHECKING_PARAGON_CONNECTION);

        dialogWaiting.setContent("Searching Paragon...");
        dialogWaiting.show();

//        BleManager.getInstance().startScan();
    }

    /**
     * Get recipe's title image from file.
     *
     * @param imageFileName File name of external storage.
     */
    public void loadImageFromFile(String imageFileName) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
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
