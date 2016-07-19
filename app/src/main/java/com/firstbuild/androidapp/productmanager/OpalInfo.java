package com.firstbuild.androidapp.productmanager;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.firstbuild.androidapp.OpalValues;
import com.firstbuild.androidapp.ParagonValues;
import com.firstbuild.androidapp.R;
import com.firstbuild.androidapp.dashboard.DashboardActivity;
import com.firstbuild.androidapp.paragon.datamodel.RecipeInfo;
import com.firstbuild.tools.MathTools;

import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * Created by hans on 16. 6. 10..
 */
public class OpalInfo extends ProductInfo{

    private static final String TAG = OpalInfo.class.getSimpleName();

    public static final int INITIAL_TIME_SYNC_VALUE = 0xffffffff;

    // TODO: hans 16. 6. 15. Asking which characteristic should be read and subscribed
    protected int NUM_MUST_INIT_DATA = 4;

    private byte operationStateValue;
    private byte operationModeValue;
    private byte lightModeValue;
    private byte cleanCycleValue;
    private byte isScheduleEnabledValue;
    private byte otaBinaryTypeValue; //  0x00: BLE image , 0x01: Opal image
    private byte opalUpdateProgressValue;
    private byte opalErrorValue;
    private byte temperatureValue;


    private byte firmwareVersionFirst = -1;
    private byte firmwareVersionSecond = -1;
    private byte firmwareVersionThird = -1;
    private byte firmwareVersionFourth = -1;

    private byte blueToothVersionFirst = -1;
    private byte blueToothVersionSecond = -1;
    private byte blueToothVersionThird = -1;
    private byte blueToothVersionFourth = -1;

    //private byte logDataIndexValue;
    //private byte[7][56] logDataValue;

    private short pumpCycleValue;

    private byte[] scheduleValue;
    private byte[] filterInstallValue;

    private int timeSyncValue = INITIAL_TIME_SYNC_VALUE;
    private int opalVersionValue;

    public OpalInfo(int type, String address, String nickname) {
        super(type, address, nickname);
    }

    public OpalInfo(ProductInfo product) {
        super(product);
    }

    public OpalInfo(JSONObject jsonObject) {
        super(jsonObject);
    }

    @Override
    public void initMustData() {

        operationStateValue = INITIAL_VALUE;
        operationModeValue = INITIAL_VALUE;
        lightModeValue = INITIAL_VALUE;
        isScheduleEnabledValue = INITIAL_VALUE;

        isAllMustDataReceived = false;
    }

    @Override
    public int getMustDataStatus() {

        int numGetData = 0;

        if(operationStateValue != INITIAL_VALUE){
            numGetData++;
        }

        if(operationModeValue != INITIAL_VALUE){
            numGetData++;
        }

        if(lightModeValue != INITIAL_VALUE){
            numGetData++;
        }

        if(isScheduleEnabledValue != INITIAL_VALUE){
            numGetData++;
        }

        if(numGetData == NUM_MUST_INIT_DATA){
            isAllMustDataReceived = true;
        }
        else{
            isAllMustDataReceived = false;
        }

        return numGetData;
    }

    @Override
    public int getNumMustInitData() {
        return NUM_MUST_INIT_DATA;
    }

    @Override
    public void updateErd(String uuid, byte[] value) {

        ByteBuffer byteBuffer = ByteBuffer.wrap(value);

        switch (uuid.toUpperCase()) {

            case OpalValues.OPAL_OP_STATE_UUID:
                setOperationStateValue(byteBuffer.get());
                Log.d(TAG, "OPAL_OP_STATE_UUID :" + String.format("%02x", value[0]));
                break;

            case OpalValues.OPAL_OP_MODE_UUID:
                Log.d(TAG, "OPAL_OP_MODE_UUID :" + String.format("%02x", value[0]));
                setOperationModeValue(byteBuffer.get());
                break;

            case OpalValues.OPAL_LIGHT_UUID:
                Log.d(TAG, "OPAL_LIGHT_UUID :" + String.format("%02x", value[0]));
                setLightModeValue(byteBuffer.get());
                break;

            case OpalValues.OPAL_ENABLE_DISABLE_SCHEDULE_UUID:
                Log.d(TAG, "OPAL_ENABLE_DISABLE_SCHEDULE_UUID :" + String.format("%02x", value[0]));
                setIsScheduleEnabledValue(byteBuffer.get());
                break;

            case OpalValues.OPAL_CLEAN_CYCLE_UUID:
                Log.d(TAG, "OPAL_CLEAN_CYCLE_UUID :" + String.format("%02x", value[0]));
                setCleanCycleValue(byteBuffer.get());
                break;

            case OpalValues.OPAL_ERROR_CHAR_UUID:
                Log.d(TAG, "OPAL_ERROR_CHAR_UUID :" + String.format("%02x", value[0]));
                setOpalErrorValue(byteBuffer.get());
                break;

            case OpalValues.OPAL_TEMPERATURE_CHAR_UUID:
                Log.d(TAG, "OPAL_TEMPERATURE_CHAR_UUID :" + String.format("%02x", value[0]));
                setTemperatureValue(byteBuffer.get());
                break;

            case OpalValues.OPAL_FIRMWARE_VERSION_CHAR_UUID:
                String firmware = String.format("%02x%02x%02x%02x", value[0], value[1], value[2], value[3]);
                Log.d(TAG, "OPAL_FIRMWARE_VERSION_CHAR_UUID :" + firmware);
                setFirmWareVersion(value[0], value[1], value[2], value[3]);
                break;

            case OpalValues.OPAL_OTA_BT_VERSION_CHAR_UUID:
                // First 2 bytes are service ID , so ignore it !
                String btVersion = String.format("%02x%02x%02x%02x", value[2], value[3], value[4], value[5]);
                Log.d(TAG, "OPAL_OTA_BT_VERSION_CHAR_UUID :" + btVersion);
                setBTVersion(value[2], value[3], value[4], value[5]);
                break;

            case OpalValues.OPAL_SET_SCHEDULE_UUID:
                Log.d(TAG, "OPAL_SET_SCHEDULE_UUID :" + MathTools.byteArrayToHex(value));
                setScheduleValue(value);
                break;

            default:
                Log.d(TAG, "[NOT UPDATING in APP]UUID to update : " + uuid + "   value to update : " + MathTools.byteArrayToHex(byteBuffer.array()));
                break;
        }
    }

    @Override
    public void updateDashboardItemUI(DashboardActivity.ProductListAdapter.ViewHolder holderDashboard) {

        holderDashboard.imageLogo.setImageResource(R.drawable.ic_opal_logo);
        holderDashboard.imageMark.setImageResource(R.drawable.ic_opal_mark);

        // set the Paragon related UI to GONE to make it disappear
        holderDashboard.layoutStatus.setVisibility(View.GONE);
    }

    @Override
    public ArrayList<String> getMustHaveNotificationUUIDList() {

        if(mustHaveNotificationUUIDList.size() == 0) {
            // initialize must-have-notification uuid list
            mustHaveNotificationUUIDList.add(OpalValues.OPAL_OP_STATE_UUID);
            mustHaveNotificationUUIDList.add(OpalValues.OPAL_OP_MODE_UUID);
            mustHaveNotificationUUIDList.add(OpalValues.OPAL_LIGHT_UUID);
//            mustHaveNotificationUUIDList.add(OpalValues.OPAL_CLEAN_CYCLE_UUID);
            mustHaveNotificationUUIDList.add(OpalValues.OPAL_UPDATE_PROGRESS_UUID);
            mustHaveNotificationUUIDList.add(OpalValues.OPAL_ERROR_CHAR_UUID);
            mustHaveNotificationUUIDList.add(OpalValues.OPAL_OTA_CONTROL_COMMAND_CHAR_UUID);
//            mustHaveNotificationUUIDList.add(OpalValues.OPAL_TEMPERATURE_CHAR_UUID);
        }

        return mustHaveNotificationUUIDList;
    }

    @Override
    public ArrayList<String> getMustHaveUUIDList() {
        if(mustHaveUUIDList.size() == 0) {
            // initialize must-have uuid list
            mustHaveUUIDList.add(OpalValues.OPAL_OP_STATE_UUID);
            mustHaveUUIDList.add(OpalValues.OPAL_OP_MODE_UUID);
            mustHaveUUIDList.add(OpalValues.OPAL_LIGHT_UUID);
            mustHaveUUIDList.add(OpalValues.OPAL_ENABLE_DISABLE_SCHEDULE_UUID);
            mustHaveUUIDList.add(OpalValues.OPAL_SET_SCHEDULE_UUID);
        }

        return mustHaveUUIDList;
    }

    public void disconnected() {

        super.disconnected();

        operationStateValue = INITIAL_VALUE;
        operationModeValue = INITIAL_VALUE;
        lightModeValue = INITIAL_VALUE;
        isScheduleEnabledValue = INITIAL_VALUE;

    }

    public boolean isMakingIceButtonVisible() {

        boolean ret = false;


        if(this.operationStateValue == OpalValues.OPAL_STATE_IDLE ||
                this.operationStateValue == OpalValues.OPAL_STATE_ICE_MAKING) {
            ret = true;
        }

        return ret;
    }

    public void setOperationStateValue(byte operationStateValue) {
        this.operationStateValue = operationStateValue;
    }

    public void setOperationModeValue(byte operationModeValue) {
        this.operationModeValue = operationModeValue;
    }

    public void setLightModeValue(byte lightModeValue) {
        this.lightModeValue = lightModeValue;
    }

    public void setCleanCycleValue(byte cleanCycleValue) {
        this.cleanCycleValue = cleanCycleValue;
    }

    public void setIsScheduleEnabledValue(byte isScheduleEnabledValue) {
        this.isScheduleEnabledValue = isScheduleEnabledValue;
    }

    public void setOtaBinaryTypeValue(byte otaBinaryTypeValue) {
        this.otaBinaryTypeValue = otaBinaryTypeValue;
    }

    public void setOpalUpdateProgressValue(byte opalUpdateProgressValue) {
        this.opalUpdateProgressValue = opalUpdateProgressValue;
    }

    public void setOpalErrorValue(byte opalErrorValue) {
        this.opalErrorValue = opalErrorValue;
    }

    public void setTemperatureValue(byte temperatureValue) {
        this.temperatureValue = temperatureValue;
    }

    public void setPumpCycleValue(short pumpCycleValue) {
        this.pumpCycleValue = pumpCycleValue;
    }

    public void setScheduleValue(byte[] scheduleValue) {
        this.scheduleValue = scheduleValue;
    }

    public void setFilterInstallValue(byte[] filterInstallValue) {
        this.filterInstallValue = filterInstallValue;
    }

    public void setTimeSyncValue(int timeSyncValue) {
        this.timeSyncValue = timeSyncValue;
    }

    public void setOpalVersionValue(int opalVersionValue) {
        this.opalVersionValue = opalVersionValue;
    }

    public byte getOperationStateValue() {
        return operationStateValue;
    }

    public byte getOperationModeValue() {
        return operationModeValue;
    }

    public byte getLightModeValue() {
        return lightModeValue;
    }

    public byte getCleanCycleValue() {
        return cleanCycleValue;
    }

    public byte getIsScheduleEnabledValue() {
        return isScheduleEnabledValue;
    }

    public byte getOtaBinaryTypeValue() {
        return otaBinaryTypeValue;
    }

    public byte getOpalUpdateProgressValue() {
        return opalUpdateProgressValue;
    }

    public byte getOpalErrorValue() {
        return opalErrorValue;
    }

    public byte getTemperatureValue() {
        return temperatureValue;
    }

    public short getPumpCycleValue() {
        return pumpCycleValue;
    }

    public byte[] getScheduleValue() {
        return scheduleValue;
    }

    public byte[] getFilterInstallValue() {
        return filterInstallValue;
    }

    public int getTimeSyncValue() {
        return timeSyncValue;
    }

    public int getOpalVersionValue() {
        return opalVersionValue;
    }


    public void setFirmWareVersion(byte first, byte second, byte third, byte fourth) {
        this.firmwareVersionFirst = first;
        this.firmwareVersionSecond = second;
        this.firmwareVersionThird = third;
        this.firmwareVersionFourth = fourth;
    }

    public String getFirmWareVersion() {

        return String.format("%02x%02x%02x%02x", firmwareVersionFirst, firmwareVersionSecond, firmwareVersionThird, firmwareVersionFourth );
    }

    public void setBTVersion(byte first, byte second, byte third, byte fourth) {
        this.blueToothVersionFirst = first;
        this.blueToothVersionSecond = second;
        this.blueToothVersionThird = third;
        this.blueToothVersionFourth = fourth;
    }

    public String getBTVersion() {
        return String.format("%02x%02x%02x%02x", blueToothVersionFirst, blueToothVersionSecond, blueToothVersionThird, blueToothVersionFourth);
    }

    /**
     * Compare the version number between opal device and image file.
     * @return return true when update is required, otherwise return false.
     */
    public boolean isOpalFirmwareUpgradeRequired() {

        if(isOpalFirmwareVersionReceived() == false) {
            return false;
        }

        byte[] latest = MathTools.hexToByteArray(OpalValues.LATEST_OPAL_FIRMWARE_VERSION);
        byte first = latest[0];
        byte second = latest[1];
        byte third = latest[2];
        byte fourth = latest[3];

        Log.d(TAG, "compareVersion : Image  version :" + first + "." + second + "." + third + "." + fourth );
        Log.d(TAG, "compareVersion : current Opal version   :" + this.firmwareVersionFirst + "." + this.firmwareVersionSecond + "." + this.firmwareVersionThird + "." + this.firmwareVersionFourth);

        boolean isNeedUpdate = false;

        if (first > this.firmwareVersionFirst ||
                second  > this.firmwareVersionSecond ||
                third > this.firmwareVersionThird ||
                fourth > this.firmwareVersionFourth ) {
            isNeedUpdate = true;
        }

        return isNeedUpdate;
    }

    /**
     * Compare the version number between BLE module and image file.
     *
     * @return return true when update is required, otherwise return false.
     */
    public boolean isBLEModuleUpgradeRequired() {

        // If required version is not received, let it return false
        if(isBLEVersionReceived() == false) {
            return false;
        }

        byte[] latest = MathTools.hexToByteArray(OpalValues.LATEST_OPAL_BLE_FIRMWARE_VERSION);
        byte first = latest[0];
        byte second = latest[1];
        byte third = latest[2];
        byte fourth = latest[3];

        Log.d(TAG, "compareVersion : Image  version :" + first + "." + second + "." + third + "." + fourth );
        Log.d(TAG, "compareVersion : current Opal BLE version   :" + this.blueToothVersionFirst + "." + this.blueToothVersionSecond + "." + this.blueToothVersionThird + "." + this.blueToothVersionFourth);

        boolean isNeedUpdate = false;

        if (first > this.blueToothVersionFirst ||
                second  > this.blueToothVersionSecond ||
                third > this.blueToothVersionThird ||
                fourth > this.blueToothVersionFourth ) {
            isNeedUpdate = true;
        }

        return isNeedUpdate;
    }

    private boolean isBLEVersionReceived() {
        boolean ret = false;

        if(blueToothVersionFirst != -1 &&
                blueToothVersionSecond != -1 &&
                blueToothVersionThird != -1 &&
                blueToothVersionFourth != -1) {

            ret = true;
        }

        return ret;
    }

    private boolean isOpalFirmwareVersionReceived() {
        boolean ret = false;

        if(firmwareVersionFirst != -1 &&
                firmwareVersionSecond != -1 &&
                firmwareVersionThird != -1 &&
                firmwareVersionFourth != -1) {

            ret = true;
        }

        return ret;
    }
}
