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
import com.firstbuild.androidapp.paragon.OtaManager;
import com.firstbuild.androidapp.productmanager.OpalInfo;
import com.firstbuild.androidapp.productmanager.ProductInfo;
import com.firstbuild.androidapp.productmanager.ProductManager;
import com.firstbuild.commonframework.blemanager.BleListener;
import com.firstbuild.commonframework.blemanager.BleManager;
import com.firstbuild.commonframework.blemanager.BleValues;
import com.firstbuild.tools.MathTools;

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

        return view;
    }

    public void onOpalDataChanged(String uuid, byte[] value) {

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

            case OpalValues.OPAL_IMAGE_DATA_CHAR_UUID:
                // TODO : each image write response should be 0x00 but current response contains exactly what we've sent to the BLE
                // TODO : should check this with JungIn
                OtaManager.getInstance().getResponse((byte)0x00);
                break;

            default:
                Log.d(TAG, "onOpalDataChanged : Not Handled ! : uuid : " + uuid + " value : " + MathTools.byteArrayToHex(value));
                break;
        }

    }

    public boolean onOpalDataNotified(String uuid, byte[] value) {

        boolean notificationHandled = true;

        switch (uuid.toUpperCase()) {
            case OpalValues.OPAL_OTA_CONTROL_COMMAND_CHAR_UUID:
                Log.d(TAG, "onOpalDataNotified : uuid : OpalValues.OPAL_OTA_CONTROL_COMMAND_CHAR_UUID");
                OtaManager.getInstance().getResponse(value[0]);
                break;

            case OpalValues.OPAL_UPDATE_PROGRESS_UUID:
                Log.d(TAG, "onOpalDataNotified : uuid : OpalValues.OPAL_UPDATE_PROGRESS_UUID");
                break;

            default:
                Log.d(TAG, "onOpalDataNotified : Not Handled ! So passing to  onOpalDataChanged() : uuid : " + uuid + "    value : " + MathTools.byteArrayToHex(value));
                notificationHandled = false;
                break;
        }

        return notificationHandled;

    }


}
