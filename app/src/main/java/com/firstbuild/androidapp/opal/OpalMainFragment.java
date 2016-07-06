package com.firstbuild.androidapp.opal;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.firstbuild.androidapp.OpalValues;
import com.firstbuild.androidapp.R;
import com.firstbuild.androidapp.productmanager.OpalInfo;
import com.firstbuild.androidapp.productmanager.ProductInfo;
import com.firstbuild.androidapp.productmanager.ProductManager;
import com.firstbuild.commonframework.blemanager.BleListener;
import com.firstbuild.commonframework.blemanager.BleManager;
import com.firstbuild.commonframework.blemanager.BleValues;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;

/**
 * Created by hans on 16. 6. 20..
 */
public class OpalMainFragment extends Fragment {

    private String TAG = OpalMainFragment.class.getSimpleName();

    private TextView statusView;
    private Switch nightLightSwitch;
    private Switch scheduleSwitch;
    private Button makeIceBtn;
    private OpalInfo currentOpalInfo;

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
                Log.d(TAG, "[HANS][onCharacteristicRead] address: " + address + ", uuid: " + uuid);

                if(getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onOpalDataChanged(uuid, value);
                        }
                    });
                }
            }
        }

        @Override
        public void onCharacteristicWrite(String address, final String uuid, final byte[] value) {
            super.onCharacteristicWrite(address, uuid, value);

            ProductInfo productInfo = ProductManager.getInstance().getCurrent();

            if (address.equals(productInfo.address)) {

                Log.d(TAG, "[HANS][onCharacteristicWrite] uuid: " + uuid + ", value: " + String.format("%02x", value[0]));

                if(getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onOpalDataChanged(uuid, value);
                        }
                    });
                }
            }

        }

        @Override
        public void onCharacteristicChanged(String address, final String uuid, final byte[] value) {
            super.onCharacteristicChanged(address, uuid, value);

            ProductInfo productInfo = ProductManager.getInstance().getCurrent();
            if (address.equals(productInfo.address)) {
                Log.d(TAG, "[HANS][onCharacteristicChanged] address: " + address + ", uuid: " + uuid);

                if(getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onOpalDataChanged(uuid, value);
                        }
                    });
                }
            }
        }

        @Override
        public void onDescriptorWrite(String address, String uuid, byte[] value) {
            super.onDescriptorWrite(address, uuid, value);

            ProductInfo productInfo = ProductManager.getInstance().getCurrent();

            if (address.equals(productInfo.address)) {
                Log.d(TAG, "[HANS][onDescriptorWrite] address: " + address + ", uuid: " + uuid);
            }

        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_opal_main, container, false);

        statusView = (TextView)view.findViewById(R.id.opal_status_text);
        nightLightSwitch = (Switch)view.findViewById(R.id.night_light_switch);
        scheduleSwitch = (Switch)view.findViewById(R.id.schedule_mode_switch);
        makeIceBtn = (Button)view.findViewById(R.id.main_ui_start_stop_making_ice);

        currentOpalInfo = (OpalInfo)ProductManager.getInstance().getCurrent();

        // Setting current Status
        statusView.setText(OpalValues.getStatusText(getContext(), currentOpalInfo.getOperationStateValue()));

        // Setting current Night light mode
        nightLightSwitch.setChecked(currentOpalInfo.getLightModeValue() == OpalValues.OPAL_NIGHT_TIME_LIGHT ? true : false);

        // Setting current schedule en/disable mode
        scheduleSwitch.setChecked(currentOpalInfo.getIsScheduleEnabledValue() == OpalValues.OPAL_ENABLE_SCHEDULE ? true : false);

        // Configure visibilitiy of Make/Stop Ice button
        makeIceBtn.setVisibility(currentOpalInfo.isMakingIceButtonVisible() ? View.VISIBLE : View.GONE);

        // Configure Make/Stop Ice button's text
        if(currentOpalInfo.getOperationStateValue() == OpalValues.OPAL_MODE_ICE_MAKING) {
            makeIceBtn.setText(R.string.stop_making_ice);
        }else {
            makeIceBtn.setText(R.string.make_ice);
        }

        makeIceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ByteBuffer valueBuffer = ByteBuffer.allocate(1);

                if(makeIceBtn.getText().equals(getString(R.string.make_ice))) {

                    valueBuffer.put(OpalValues.OPAL_MODE_ICE_MAKING);
                }
                else {
                    valueBuffer.put(OpalValues.OPAL_MODE_OFF);
                }

                BleManager.getInstance().writeCharacteristics(currentOpalInfo.bluetoothDevice, OpalValues.OPAL_OP_MODE_UUID, valueBuffer.array());
            }
        });

        scheduleSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ByteBuffer valueBuffer = ByteBuffer.allocate(1);

                if(isChecked) {
                    valueBuffer.put(OpalValues.OPAL_ENABLE_SCHEDULE);
                }
                else {
                    valueBuffer.put(OpalValues.OPAL_DISABLE_SCHEDULE);
                }

                BleManager.getInstance().writeCharacteristics(currentOpalInfo.bluetoothDevice, OpalValues.OPAL_ENABLE_DISABLE_SCHEDULE_UUID, valueBuffer.array());
            }
        });

        nightLightSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ByteBuffer valueBuffer = ByteBuffer.allocate(1);

                if(isChecked) {
                    // turn on night light mode
                    valueBuffer.put(OpalValues.OPAL_NIGHT_TIME_LIGHT);
                }
                else {
                    // turn off night light mode
                    valueBuffer.put(OpalValues.OPAL_DAY_TIME_LIGHT);
                }

                BleManager.getInstance().writeCharacteristics(currentOpalInfo.bluetoothDevice, OpalValues.OPAL_LIGHT_UUID, valueBuffer.array());
            }
        });



        // Read the version info for OTA when creating view
        BleManager.getInstance().readCharacteristics(currentOpalInfo.bluetoothDevice, OpalValues.OPAL_FIRMWARE_VERSION_CHAR_UUID);
        BleManager.getInstance().readCharacteristics(currentOpalInfo.bluetoothDevice, OpalValues.OPAL_OTA_BT_VERSION_CHAR_UUID);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Add ble event listener
        BleManager.getInstance().addListener(bleListener);
    }

    @Override
    public void onPause() {
        super.onPause();

        // Add ble event listener
        BleManager.getInstance().removeListener(bleListener);
    }

    private void onOpalDataChanged(String uuid, byte[] value) {

        switch (uuid.toUpperCase()) {
            case OpalValues.OPAL_OP_STATE_UUID:
                statusView.setText(OpalValues.getStatusText(getContext(), currentOpalInfo.getOperationStateValue()));
                // Configure visibilitiy of Make/Stop Ice button
                makeIceBtn.setVisibility(currentOpalInfo.isMakingIceButtonVisible() ? View.VISIBLE : View.GONE);
                break;

            case OpalValues.OPAL_OP_MODE_UUID:
                if(currentOpalInfo.getOperationModeValue() == OpalValues.OPAL_MODE_OFF) {
                    makeIceBtn.setVisibility(View.VISIBLE);
                    makeIceBtn.setText(R.string.make_ice);
                }else if(currentOpalInfo.getOperationModeValue() == OpalValues.OPAL_MODE_ICE_MAKING){
                    makeIceBtn.setVisibility(View.VISIBLE);
                    makeIceBtn.setText(R.string.stop_making_ice);
                } else {
                    // Clean mode
                    makeIceBtn.setVisibility(View.GONE);
                }

                break;

            default:
                Log.d(TAG, "onOpalDataChanged : uuid : " + uuid + "    value : " + String.format("%02x", value[0]));
                break;
        }

    }

}
