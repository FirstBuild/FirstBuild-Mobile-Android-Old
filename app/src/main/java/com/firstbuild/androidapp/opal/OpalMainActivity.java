package com.firstbuild.androidapp.opal;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.firstbuild.androidapp.FirstBuildApplication;
import com.firstbuild.androidapp.OpalValues;
import com.firstbuild.androidapp.R;
import com.firstbuild.androidapp.paragon.OtaManager;
import com.firstbuild.androidapp.productmanager.OpalInfo;
import com.firstbuild.androidapp.productmanager.ProductInfo;
import com.firstbuild.androidapp.productmanager.ProductManager;
import com.firstbuild.commonframework.blemanager.BleListener;
import com.firstbuild.commonframework.blemanager.BleManager;
import com.firstbuild.commonframework.blemanager.BleValues;
import com.firstbuild.tools.IntentTools;
import com.firstbuild.tools.MathTools;
import com.firstbuild.viewutil.OTAConfirmDialogFragment;
import com.firstbuild.viewutil.ProgressDialogFragment;

import java.util.HashMap;
import java.util.List;

/**
 * Created by hans on 16. 6. 16..
 */
public class OpalMainActivity extends AppCompatActivity implements OTAConfirmDialogFragment.OTAUpdateStartDelegate {

    public interface OTAResultDelegate {
        void onOTASuccessful();
        void onOTAFailed();

        void onOTAProgressChanged(int progress);
        void onOTAProgressMax(int max);

        void onOpalBinaryInstallPrepare();
        void onOpalBinaryInstallProgress(int progress);

    }

    private static final int REQUEST_ENABLE_BT = 1234;

    private static final String TAG = OpalMainActivity.class.getSimpleName();

    public static final String TAG_MAIN_FRAGMENT = "tag_main_fragment";


    public static final String TAG_BLE_OTA_UPDATE_CONFIRM_DIALOG = "tag_ble_ota_update_confirm_dialog";
    public static final String TAG_OPAL_OTA_UPDATE_CONFIRM_DIALOG = "tag_opal_ota_update_confirm_dialog";
    public static final String TAG_SCHEDULE_FRAGMENT = "tag_schedule_fragment";

    public static final String TAG_OTA_UPDATE_NOT_AVAILABLE_DIALOG = "tag_ota_update_not_available_dialog";
    public static final String TAG_OTA_FAILURE_DIALOG = "tag_ota_failure_dialog";
    public static final String TAG_OTA_SUCCESS_DIALOG = "tag_ota_success_dialog";
    public static final String TAG_OTA_PROGRESS_DIALOG = "tag_ota_progress_dialog";

    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    private TextView opalFirmwareTv;
    private TextView bleFirmwareTv;

    private OpalInfo opalInfo;
    private int currentNavItemId;

    private OTAResultDelegate otaResultDelegate = new OTAResultDelegate() {
        @Override
        public void onOTASuccessful() {
            showUpdateSuccessDialog(true);

            // Read BLE version again
            BleManager.getInstance().readCharacteristics(opalInfo.bluetoothDevice, OpalValues.OPAL_OTA_BT_VERSION_CHAR_UUID);
        }

        @Override
        public void onOTAFailed() {
            showUpdateFailureDialog();
        }

        @Override
        public void onOTAProgressChanged(int progress) {

            ProgressDialogFragment pd = (ProgressDialogFragment)getSupportFragmentManager().findFragmentByTag(TAG_OTA_PROGRESS_DIALOG);
            if(pd != null) {
                pd.setProgress(progress);
            }
        }

        @Override
        public void onOTAProgressMax(int max) {
            ProgressDialogFragment pd = (ProgressDialogFragment)getSupportFragmentManager().findFragmentByTag(TAG_OTA_PROGRESS_DIALOG);
            if(pd != null) {
                // Initialize progress and max
                pd.setProgress(0);
                pd.setMax(max);
            }
        }

        @Override
        public void onOpalBinaryInstallPrepare() {

            ProgressDialogFragment pd = (ProgressDialogFragment)getSupportFragmentManager().findFragmentByTag(TAG_OTA_PROGRESS_DIALOG);
            if(pd != null) {
                // Initialize progress and max
                pd.setProgress(0);
                pd.setMax(100);

                pd.setTitle(getString(R.string.popup_ota_install_progress_title));
            }
        }

        @Override
        public void onOpalBinaryInstallProgress(int progress) {

            ProgressDialogFragment pd = (ProgressDialogFragment)getSupportFragmentManager().findFragmentByTag(TAG_OTA_PROGRESS_DIALOG);

            if(pd != null) {
                pd.setProgress(progress);

                // Opal Firmware Install finishes
                if(progress == 100) {
                    showUpdateSuccessDialog(false);

                    // Read Opal version again
                    BleManager.getInstance().readCharacteristics(opalInfo.bluetoothDevice, OpalValues.OPAL_FIRMWARE_VERSION_CHAR_UUID);
                }
            }
        }
    };

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
                Log.d(TAG, "[onConnectionStateChanged] address: " + address + ", status: " + status);

                if (status == BluetoothProfile.STATE_CONNECTED) {
                    // TODO: needs testing for corner case handling

                }
                else if (status == BluetoothProfile.STATE_DISCONNECTED) {
                    // TODO: needs testing for corner case handling

                    checkBleAvailability();

                    // if OTA is in progress, cancel the progress UI and show update failure UI
                    checkOtaProgressOnDisconnected();
                }
            }
        }

        @Override
        public void onServicesDiscovered(String address, List<BluetoothGattService> bleGattServices) {
            super.onServicesDiscovered(address, bleGattServices);

            Log.d(TAG, "[onServicesDiscovered] address: " + address);
        }

        @Override
        public void onCharacteristicRead(String address, final String uuid, final byte[] value, int status) {
            super.onCharacteristicRead(address, uuid, value, status);

            ProductInfo productInfo = ProductManager.getInstance().getCurrent();

            if (address.equals(productInfo.address)) {
                Log.d(TAG, "[HANS][onCharacteristicRead] address: " + address + ", uuid: " + uuid + " value : " + MathTools.byteArrayToHex(value));

                runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            // If version information is read, update version UI in navigation view if possible
                            if(uuid.equalsIgnoreCase(OpalValues.OPAL_FIRMWARE_VERSION_CHAR_UUID) ||
                                    uuid.equalsIgnoreCase(OpalValues.OPAL_OTA_BT_VERSION_CHAR_UUID)) {
                                onUpdateVersion();
                            }

                            OpalMainFragment mainFragment = (OpalMainFragment)getSupportFragmentManager().findFragmentByTag(TAG_MAIN_FRAGMENT);
                            if(mainFragment != null) {
                                mainFragment.onOpalDataChanged(uuid, value);
                            }
                            else {
                                // Do nothing
                            }
                        }
                    });
            }
        }

        @Override
        public void onCharacteristicWrite(String address, final String uuid, final byte[] value, final int status) {
            super.onCharacteristicWrite(address, uuid, value, status);

            final ProductInfo productInfo = ProductManager.getInstance().getCurrent();

            if (address.equals(productInfo.address)) {

                Log.d(TAG, "[HANS][onCharacteristicWrite] uuid: " + uuid + ", value: " + MathTools.byteArrayToHex(value) + ", status : " + status);

                OtaManager.getInstance().onHandleWriteResponse(uuid, status);

                if(status == BluetoothGatt.GATT_SUCCESS) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            // if schedule write is successful, then update the product info
                            if(uuid.equalsIgnoreCase(OpalValues.OPAL_SET_SCHEDULE_UUID)) {
                                productInfo.updateErd(uuid, value);
                            }

                            OpalMainFragment mainFragment = (OpalMainFragment)getSupportFragmentManager().findFragmentByTag(TAG_MAIN_FRAGMENT);
                            if(mainFragment != null) {
                                mainFragment.onOpalDataChanged(uuid, value);
                            }
                        }
                    });
                }
                else {
                    // Write failure
                }
            }

        }

        @Override
        public void onCharacteristicChanged(String address, final String uuid, final byte[] value) {
            super.onCharacteristicChanged(address, uuid, value);

            ProductInfo productInfo = ProductManager.getInstance().getCurrent();
            if (address.equals(productInfo.address)) {

                Log.d(TAG, "[HANS][onCharacteristicChanged] : uuid: " + uuid + ", value: " + MathTools.byteArrayToHex(value));

                OtaManager.getInstance().onHandleNotification(uuid, value);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        OpalMainFragment mainFragment = (OpalMainFragment)getSupportFragmentManager().findFragmentByTag(TAG_MAIN_FRAGMENT);
                        if(mainFragment != null) {
                            mainFragment.onOpalDataChanged(uuid, value);
                        }
                        else {
                            // Do nothing
                        }
                    }
                });
            }
        }

        @Override
        public void onDescriptorWrite(String address, String uuid, byte[] value, int status) {
            super.onDescriptorWrite(address, uuid, value, status);

            ProductInfo productInfo = ProductManager.getInstance().getCurrent();

            if (address.equals(productInfo.address)) {
                Log.d(TAG, "[HANS][onDescriptorWrite] : uuid: " + uuid + ", value: " + MathTools.byteArrayToHex(value));
            }
        }
    };

    private void checkOtaProgressOnDisconnected() {

        FragmentManager fm = getSupportFragmentManager();

        if(fm != null &&
                fm.findFragmentByTag(TAG_OTA_PROGRESS_DIALOG) != null) {
            showUpdateFailureDialog();
        }
        else {
            // Do nothing
        }
    }

    private void onUpdateVersion() {
        if(currentNavItemId == R.id.nav_item_about) {

            Log.d(TAG, "[HANS][onUpdateVersion] : Updating version UI");

            opalFirmwareTv.setText(opalInfo.getFirmWareVersion());
            bleFirmwareTv.setText(opalInfo.getBTVersion());

        }
        else {
            Log.d(TAG, "[HANS][onUpdateVersion] : About UI is not visible so skip updating it");
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_opal_main);

        setupToolBar();

        setupDrawerLayout();

        setupNavagationView();

        getSupportFragmentManager().
                beginTransaction().
                replace(R.id.frame_content, new OpalMainFragment(), TAG_MAIN_FRAGMENT).
                commit();

        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {

            @Override
            public void onBackStackChanged() {

                if( getSupportFragmentManager().findFragmentByTag(TAG_SCHEDULE_FRAGMENT) != null ) {
                    setToolBarTitle(getString(R.string.schedule_edit_schedule_title));
                    actionBarDrawerToggle.setDrawerIndicatorEnabled(false);
                }else {
                    setToolBarTitle(getString(R.string.product_name_opal));
                    actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
                }
            }
        });

        opalInfo = (OpalInfo) ProductManager.getInstance().getCurrent();

        checkBleAvailability();

    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(TAG, "onResume() IN");

        checkBleAvailability();

        // Add ble event listener
        BleManager.getInstance().addListener(bleListener);

        // Read the version info
        BleManager.getInstance().readCharacteristics(opalInfo.bluetoothDevice, OpalValues.OPAL_OTA_BT_VERSION_CHAR_UUID);
        BleManager.getInstance().readCharacteristics(opalInfo .bluetoothDevice, OpalValues.OPAL_FIRMWARE_VERSION_CHAR_UUID);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "onDestroy() IN");

        // remove ble listener
        BleManager.getInstance().removeListener(bleListener);
    }


    private void setupNavagationView() {
        // navigation_view
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                //Checking if the item is in checked state or not, if not make it in checked state
                item.setChecked(true);
                currentNavItemId = item.getItemId();

                switch(item.getItemId()) {
                    case R.id.nav_item_my_product:

                        IntentTools.goToDashboard(OpalMainActivity.this, OpalMainActivity.class.getSimpleName());
                        drawerLayout.closeDrawers();

                        break;

                    case R.id.nav_item_help:

                        refreshHelpUI();

                        break;

                    case R.id.nav_item_faq:
                        IntentTools.openBrowser(getBaseContext(), IntentTools.OPAL_FAQ_URL);
                        break;

                    case R.id.nav_item_contact_expert:
                        IntentTools.composeEmail(getBaseContext(),
                                new String[] {IntentTools.OPAL_CONTACT_AN_EXPERT_EMAIL_ADDRESS},
                                IntentTools.OPAL_CONTACT_AN_EXPERT_EAMIL_SUBJECT);
                        break;

                    case R.id.nav_item_contact_warranty:
                        IntentTools.composeEmail(getBaseContext(),
                                new String[] {IntentTools.OPAL_CONTACT_WARRANTY_EMAIL_ADDRESS},
                                IntentTools.OPAL_CONTACT_WARRANTY_EMAIL_SUBJECT);
                        break;

                    case R.id.nav_item_feedback:
                        IntentTools.composeEmail(getBaseContext(),
                                new String[] {IntentTools.OPAL_FEEDBACK_EMAIL_ADDRESS},
                                IntentTools.OPAL_FEEDBACK_EMAIL_SUBJECT);

                        break;
                    case R.id.nav_item_about:

                        refreshAboutUI();

                        break;
                    case R.id.nav_item_update:

                        // BLE update first
                        if(opalInfo.isBLEModuleUpgradeRequired()) {
                            showUpdateConfirmDialog(TAG_BLE_OTA_UPDATE_CONFIRM_DIALOG);
                        } else if(opalInfo.isOpalFirmwareUpgradeRequired()) {
                            showUpdateConfirmDialog(TAG_OPAL_OTA_UPDATE_CONFIRM_DIALOG);
                        } else {
                            showUpdateNotAvailableDialog();
                        }
                        break;

                    default:
                        Log.d(TAG, "onNavigationItemSelected : unknown menu id : " + item.getItemId());
                        break;
                }

                return true;
            }
        });
    }

    private void refreshHelpUI() {
        navigationView.getMenu().clear();
        navigationView.inflateMenu(R.menu.menu_navigation_help_sub);
        navigationView.getMenu().getItem(0)
                .getActionView().findViewById(R.id.back_btn_container)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        navigationView.getMenu().clear();
                        navigationView.inflateMenu(R.menu.menu_navigation_drawer);
                    }
                });

        // Dirty fix for moving back button arrow to the left a bit.
        // I have no designer to ask to modify image at the moment.
        navigationView.getMenu().
                getItem(0).getActionView().
                setTranslationX(getResources().
                        getDimensionPixelSize(R.dimen.navigation_back_button_left_translation) * -1);
    }

    private void refreshAboutUI() {
        navigationView.getMenu().clear();

        // since Navigation Menu item has limited height so utilize header view
        navigationView.inflateHeaderView(R.layout.navigation_menu_item_back_button_header_about);
        navigationView.inflateHeaderView(R.layout.navigation_menu_item_about);

        navigationView.getHeaderView(0).findViewById(R.id.back_btn_container)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                        public void onClick(View v) {
                        View back_btn_header = navigationView.getHeaderView(0);
                        View headerContents = navigationView.getHeaderView(1);
                        navigationView.removeHeaderView(back_btn_header);
                        navigationView.removeHeaderView(headerContents);

                        navigationView.inflateMenu(R.menu.menu_navigation_drawer);
                    }
                });


        // Update about contents
        View aboutContentsView = navigationView.getHeaderView(1);

        opalFirmwareTv = ((TextView)aboutContentsView.findViewById(R.id.opal_about_firmware_version));
        bleFirmwareTv = ((TextView)aboutContentsView.findViewById(R.id.opal_about_bt_version));


        ((TextView)aboutContentsView.findViewById(R.id.opal_about_app_version)).setText(FirstBuildApplication.getInstance().getAppVersion());
        opalFirmwareTv.setText(opalInfo.getFirmWareVersion());
        bleFirmwareTv.setText(opalInfo.getBTVersion());

        aboutContentsView.findViewById(R.id.opal_about_app_source_code).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                IntentTools.openBrowser(getBaseContext(), IntentTools.OPAL_APP_SOURCE_CODE_URL);
            }
        });

        aboutContentsView.findViewById(R.id.opal_learn_more).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                IntentTools.openBrowser(getBaseContext(), IntentTools.OPAL_APP_LEARN_MORE_URL);
            }
        });
    }

    private void showUpdateConfirmDialog(String tag) {
        FragmentManager fm = getSupportFragmentManager();

        if(fm != null &&
                fm.findFragmentByTag(tag) != null) {
            // skip showing dialog as it is already shown
            return;
        }

        OTAConfirmDialogFragment dialogFragment = OTAConfirmDialogFragment.getInstance(
                getString(tag.equals(TAG_BLE_OTA_UPDATE_CONFIRM_DIALOG) ? R.string.popup_bluetooth_update_available_title : R.string.popup_opal_update_available_title),
                getString(tag.equals(TAG_BLE_OTA_UPDATE_CONFIRM_DIALOG) ? R.string.popup_bluetooth_update_available_confirm_body : R.string.popup_opal_update_available_confirm_body),
                getString(R.string.popup_bluetooth_update_available_positive_btn),
                getString(R.string.popup_bluetooth_update_available_negative_btn));

        dialogFragment.show(fm, tag);
    }

    private void showUpdateNotAvailableDialog() {
        FragmentManager fm = getSupportFragmentManager();

        if(fm != null &&
                fm.findFragmentByTag(TAG_OTA_UPDATE_NOT_AVAILABLE_DIALOG) != null) {
            // skip showing dialog as it is already shown
            return;
        }

        OTAConfirmDialogFragment dialogFragment = OTAConfirmDialogFragment.getInstance(
                getString(R.string.popup_bluetooth_update_unavailable_title),
                getString(R.string.popup_bluetooth_update_unavailable_body),
                getString(android.R.string.ok),
                null);

        dialogFragment.show(fm, TAG_OTA_UPDATE_NOT_AVAILABLE_DIALOG);
    }

    private void showUpdateFailureDialog() {
        FragmentManager fm = getSupportFragmentManager();

        dismissProgressDialog(fm);

        if(fm != null &&
                fm.findFragmentByTag(TAG_OTA_FAILURE_DIALOG) != null) {
            // skip showing dialog as it is already shown
            return;
        }

        OTAConfirmDialogFragment dialogFragment = OTAConfirmDialogFragment.getInstance(
                getString(R.string.popup_firmware_update_fail_title),
                getString(R.string.popup_firmware_update_fail_body),
                getString(R.string.popup_bluetooth_update_available_negative_btn),
                null);

        dialogFragment.show(fm, TAG_OTA_FAILURE_DIALOG);
    }

    private void dismissProgressDialog(FragmentManager fm) {

        if(fm != null &&
                fm.findFragmentByTag(TAG_OTA_PROGRESS_DIALOG) != null) {
            ((DialogFragment)fm.findFragmentByTag(TAG_OTA_PROGRESS_DIALOG)).dismiss();
        }
    }

    private void showUpdateSuccessDialog(boolean isBLESuccess) {
        FragmentManager fm = getSupportFragmentManager();

        dismissProgressDialog(fm);

        if(fm != null &&
                fm.findFragmentByTag(TAG_OTA_SUCCESS_DIALOG) != null) {
            // skip showing dialog as it is already shown
            return;
        }

        String bodyContents = getString(R.string.popup_firmware_update_success_body);

        // Show guidance string to let user to check the OPAL device firmware update again after BLE update is finished successfully
        if(isBLESuccess == true) {
            bodyContents = getString(R.string.popup_firmware_update_success_body_with_next_step_guide);
        }
        else {
            // Do nothing
        }

        OTAConfirmDialogFragment dialogFragment = OTAConfirmDialogFragment.getInstance(
                null,
                bodyContents,
                getString(android.R.string.ok),
                null);

        dialogFragment.show(fm, TAG_OTA_SUCCESS_DIALOG);
    }

    private void showProgressDialog() {
        FragmentManager fm = getSupportFragmentManager();

        if(fm != null &&
                fm.findFragmentByTag(TAG_OTA_PROGRESS_DIALOG) != null) {
            // skip showing dialog as it is already shown
            return;
        }

        final ProgressDialogFragment dialogFragment = ProgressDialogFragment.getInstance(
                getString(R.string.popup_ota_progress_title),
                getString(R.string.popup_ota_progress_body),
                null,
                null);

        dialogFragment.show(fm, TAG_OTA_PROGRESS_DIALOG);
    }

    private void setupDrawerLayout() {
        // drawer view
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.openDrawer,
                R.string.closeDrawer){

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };

        //Setting the actionbarToggle to drawer layout
        drawerLayout.addDrawerListener(actionBarDrawerToggle);

        //calling sync state is necessary or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();

        // Make sure to call below after setting drawer layout otherwise it will throw exception that right drawer is not available
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                    drawerLayout.closeDrawer(GravityCompat.END);
                } else {
                    drawerLayout.openDrawer(GravityCompat.END);
                }
            }
        });
    }

    private void setupToolBar() {
        toolbar = (Toolbar) findViewById(R.id.app_bar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        setToolBarTitle(getString(R.string.product_name_opal));
    }

    private void setToolBarTitle(String title) {
        ((TextView) toolbar.findViewById(R.id.toolbar_title)).
                setText(title.toUpperCase());
    }

    private void checkBleAvailability() {
        // Check bluetooth adapter. If the adapter is disabled, enable it
        boolean result = BleManager.getInstance().isBluetoothEnabled();

        if (!result) {
            Log.d(TAG, "Bluetooth adapter is disabled. Enable bluetooth adapter.");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        else {
            Log.d(TAG, "Bluetooth adapter is already enabled. Start connect");
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        actionBarDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        actionBarDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onOTAStart() {

        if(opalInfo.isBLEModuleUpgradeRequired()) {
            OtaManager.getInstance().startBleOtaProcess(opalInfo.bluetoothDevice, otaResultDelegate);
        } else if(opalInfo.isOpalFirmwareUpgradeRequired()) {
            OtaManager.getInstance().startOpalOtaProcess(opalInfo.bluetoothDevice, otaResultDelegate);
        } else {

        }

        showProgressDialog();
    }

    public void onTimeSlotClicked(View v) {
        FragmentManager fm = getSupportFragmentManager();

        if(fm != null &
                fm.findFragmentByTag(TAG_SCHEDULE_FRAGMENT) == null) {
            return;
        }

        OpalScheduleFragment scheduleFragment = (OpalScheduleFragment)fm.findFragmentByTag(TAG_SCHEDULE_FRAGMENT);
        scheduleFragment.onHandleTimeSlotClicked(v);

    }

    public void onWeekdaysClicked(View v) {
        FragmentManager fm = getSupportFragmentManager();

        if(fm != null &
                fm.findFragmentByTag(TAG_SCHEDULE_FRAGMENT) == null) {
            return;
        }

        OpalScheduleFragment scheduleFragment = (OpalScheduleFragment)fm.findFragmentByTag(TAG_SCHEDULE_FRAGMENT);
        scheduleFragment.onHandleWeekdaysClicked(v);

    }
}

