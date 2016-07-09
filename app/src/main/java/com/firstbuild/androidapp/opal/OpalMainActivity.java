package com.firstbuild.androidapp.opal;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.content.pm.PackageManager;
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
        void onBLEOTASuccessful();
        void onBLEOTAFailed();

        void onBLEOTAProgressChanged(int progress);
        void onBLEOTAProgressMax(int max);
    }

    private static final int REQUEST_ENABLE_BT = 1234;

    private static final String TAG = OpalMainActivity.class.getSimpleName();

    public static final String TAG_MAIN_FRAGMENT = "tag_main_fragment";

    public static final String TAG_OTA_UPDATE_CONFIRM_DIALOG = "tag_ota_update_confirm_dialog";
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

    private String appVersionName;

    private OpalInfo opalInfo;
    private int currentNavItemId;

    private OTAResultDelegate otaResultDelegate = new OTAResultDelegate() {
        @Override
        public void onBLEOTASuccessful() {
            showUpdateSuccessDialog();

            // Read BLE version again
            BleManager.getInstance().readCharacteristics(opalInfo.bluetoothDevice, OpalValues.OPAL_OTA_BT_VERSION_CHAR_UUID);
        }

        @Override
        public void onBLEOTAFailed() {
            showUpdateFailureDialog();
        }

        @Override
        public void onBLEOTAProgressChanged(int progress) {

            ProgressDialogFragment pd = (ProgressDialogFragment)getSupportFragmentManager().findFragmentByTag(TAG_OTA_PROGRESS_DIALOG);
            if(pd != null) {
                pd.setProgress(progress);
            }
        }

        @Override
        public void onBLEOTAProgressMax(int max) {
            ProgressDialogFragment pd = (ProgressDialogFragment)getSupportFragmentManager().findFragmentByTag(TAG_OTA_PROGRESS_DIALOG);
            if(pd != null) {
                pd.setMax(max);
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
            }
        }

        @Override
        public void onServicesDiscovered(String address, List<BluetoothGattService> bleGattServices) {
            super.onServicesDiscovered(address, bleGattServices);

            Log.d(TAG, "[onServicesDiscovered] address: " + address);
        }

        @Override
        public void onCharacteristicRead(String address, final String uuid, final byte[] value) {
            super.onCharacteristicRead(address, uuid, value);

            ProductInfo productInfo = ProductManager.getInstance().getCurrent();

            if (address.equals(productInfo.address)) {
                Log.d(TAG, "[HANS][onCharacteristicRead] address: " + address + ", uuid: " + uuid + " value : " + MathTools.byteArrayToHex(value));

                if(uuid.equalsIgnoreCase(OpalValues.OPAL_FIRMWARE_VERSION_CHAR_UUID) ||
                        uuid.equalsIgnoreCase(OpalValues.OPAL_OTA_BT_VERSION_CHAR_UUID)) {
                    // VERSION is read, so update version UI if possible
                    onUpdateVersion();
                }

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
        public void onCharacteristicWrite(String address, final String uuid, final byte[] value) {
            super.onCharacteristicWrite(address, uuid, value);

            ProductInfo productInfo = ProductManager.getInstance().getCurrent();

            if (address.equals(productInfo.address)) {

                Log.d(TAG, "[HANS][onCharacteristicWrite] uuid: " + uuid + ", value: " + MathTools.byteArrayToHex(value));

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
        public void onCharacteristicChanged(String address, final String uuid, final byte[] value) {
            super.onCharacteristicChanged(address, uuid, value);

            ProductInfo productInfo = ProductManager.getInstance().getCurrent();
            if (address.equals(productInfo.address)) {

                Log.d(TAG, "[HANS][onCharacteristicChanged] : uuid: " + uuid + ", value: " + MathTools.byteArrayToHex(value));

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        OpalMainFragment mainFragment = (OpalMainFragment)getSupportFragmentManager().findFragmentByTag(TAG_MAIN_FRAGMENT);
                        if(mainFragment != null) {
                            boolean notificationHandled = mainFragment.onOpalDataNotified(uuid, value);

                            if(notificationHandled == false) {

                                mainFragment.onOpalDataChanged(uuid, value);
                            }
                            else {
                                // Do nothing
                            }
                        }
                        else {
                            // Do nothing
                        }
                    }
                });
            }
        }

        @Override
        public void onDescriptorWrite(String address, String uuid, byte[] value) {
            super.onDescriptorWrite(address, uuid, value);

            ProductInfo productInfo = ProductManager.getInstance().getCurrent();

            if (address.equals(productInfo.address)) {
                Log.d(TAG, "[HANS][onDescriptorWrite] : uuid: " + uuid + ", value: " + MathTools.byteArrayToHex(value));
            }
        }
    };

    private void onUpdateVersion() {
        if(currentNavItemId == R.id.nav_item_about) {

            Log.d(TAG, "[HANS][onUpdateVersion] : Updating version UI");

            // TODO: somehow UI update doesn't work
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

        try {
            appVersionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        opalInfo = (OpalInfo) ProductManager.getInstance().getCurrent();

        checkBleAvailability();

    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(TAG, "onResume() IN");

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
                        IntentTools.openBrowser(getBaseContext(), IntentTools.OPAL_INTRODUCTION_HOME_URL);
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
                        if(opalInfo.isBLEVersionReceived() &&
                                opalInfo.isBLEModuleUpgradeRequired()) {
                            showUpdateConfirmDialog();
                        }
                        else {
                            showUpdateNotAvailableDialog();;
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


        ((TextView)aboutContentsView.findViewById(R.id.opal_about_app_version)).setText(appVersionName);
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

    private void showUpdateConfirmDialog() {
        FragmentManager fm = getSupportFragmentManager();

        if(fm != null &&
                fm.findFragmentByTag(TAG_OTA_UPDATE_CONFIRM_DIALOG) != null) {
            // skip showing dialog as it is already shown
            return;
        }

        OTAConfirmDialogFragment dialogFragment = OTAConfirmDialogFragment.getInstance(
                getString(R.string.popup_bluetooth_update_available_title),
                getString(R.string.popup_bluetooth_update_available_confirm_body),
                getString(R.string.popup_bluetooth_update_available_positive_btn),
                getString(R.string.popup_bluetooth_update_available_negative_btn));

        dialogFragment.show(fm, TAG_OTA_UPDATE_CONFIRM_DIALOG);
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

    private void showUpdateSuccessDialog() {
        FragmentManager fm = getSupportFragmentManager();

        dismissProgressDialog(fm);


        if(fm != null &&
                fm.findFragmentByTag(TAG_OTA_SUCCESS_DIALOG) != null) {
            // skip showing dialog as it is already shown
            return;
        }

        OTAConfirmDialogFragment dialogFragment = OTAConfirmDialogFragment.getInstance(
                null,
                getString(R.string.popup_firmware_update_success_body),
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

        //calling sync state is necessay or else your hamburger icon wont show up
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
        // Make sure to set this first before calling toolbar API
        setSupportActionBar(toolbar);

        toolbar.setTitle("");
        ((TextView) toolbar.findViewById(R.id.toolbar_title)).
                setText(getString(R.string.product_name_opal).toUpperCase());
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

        OtaManager.getInstance().startBleOtaProcess(opalInfo.bluetoothDevice, otaResultDelegate);
        showProgressDialog();
    }
}

