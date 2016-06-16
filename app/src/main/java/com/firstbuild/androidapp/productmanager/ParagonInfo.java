package com.firstbuild.androidapp.productmanager;

import android.util.Log;
import android.view.View;

import com.firstbuild.androidapp.OpalValues;
import com.firstbuild.androidapp.ParagonValues;
import com.firstbuild.androidapp.R;
import com.firstbuild.androidapp.dashboard.DashboardActivity;
import com.firstbuild.androidapp.paragon.datamodel.RecipeInfo;
import com.firstbuild.androidapp.paragon.datamodel.StageInfo;
import com.firstbuild.commonframework.blemanager.BleManager;

import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * Created by hans on 16. 6. 13..
 */
public class ParagonInfo extends ProductInfo{

    private static final String TAG = ParagonInfo.class.getSimpleName();


    public static final int NO_BATTERY_INFO = -1;

    // Initial data must get from dashboard.
    private int erdBatteryLevel = NO_BATTERY_INFO;
    private byte erdBurnerStatus = INITIAL_VALUE;
    private byte erdProbeConnectionStatue = INITIAL_VALUE;
    private byte erdCurrentCookMode = INITIAL_VALUE;
    private RecipeInfo erdRecipeConfig = null;
    private byte erdCookState = INITIAL_VALUE;
    private int erdElapsedTime = INITIAL_ELAPSED_TIME;

    private float erdCurrentTemp;
    private byte erdCookStage;
    private byte erdPowerLevel = 0;
    private byte versionMajor = 0;
    private byte versionMinor = 0;
    private short versionBuild = 0;


    public ParagonInfo(int type, String address, String nickname) {
        super(type, address, nickname);
    }

    public ParagonInfo(ProductInfo product) {
        super(product);
    }

    public ParagonInfo(JSONObject jsonObject) {
        super(jsonObject);

    }

    public void initMustData(){
        erdBatteryLevel = NO_BATTERY_INFO;
        erdBurnerStatus = INITIAL_VALUE;
        erdProbeConnectionStatue = INITIAL_VALUE;
        erdCurrentCookMode = INITIAL_VALUE;
        erdRecipeConfig = null;
        erdCookState = INITIAL_VALUE;

        NUM_MUST_INIT_DATA = 7;

        isAllMustDataReceived = false;
    }

    @Override
    public ArrayList<String> getMustHaveNotificationUUIDList() {

        if(mustHaveNotificationUUIDList.size() == 0) {
            // initialize must-have-notification uuid list
            mustHaveNotificationUUIDList.add(ParagonValues.CHARACTERISTIC_COOK_CONFIGURATION);
            mustHaveNotificationUUIDList.add(ParagonValues.CHARACTERISTIC_BURNER_STATUS);
            mustHaveNotificationUUIDList.add(ParagonValues.CHARACTERISTIC_BATTERY_LEVEL);
            mustHaveNotificationUUIDList.add(ParagonValues.CHARACTERISTIC_PROBE_CONNECTION_STATE);
            mustHaveNotificationUUIDList.add(ParagonValues.CHARACTERISTIC_COOK_MODE);
            mustHaveNotificationUUIDList.add(ParagonValues.CHARACTERISTIC_CURRENT_COOK_STATE);
            mustHaveNotificationUUIDList.add(ParagonValues.CHARACTERISTIC_CURRENT_TEMPERATURE);
            mustHaveNotificationUUIDList.add(ParagonValues.CHARACTERISTIC_ELAPSED_TIME);
            mustHaveNotificationUUIDList.add(ParagonValues.CHARACTERISTIC_CURRENT_POWER_LEVEL);
        }

        return mustHaveNotificationUUIDList;
    }

    @Override
    public ArrayList<String> getMustHaveUUIDList() {
        if(mustHaveUUIDList.size() == 0) {
            // initialize must-have uuid list
            mustHaveUUIDList.add(ParagonValues.CHARACTERISTIC_PROBE_CONNECTION_STATE);
            mustHaveUUIDList.add(ParagonValues.CHARACTERISTIC_BATTERY_LEVEL);
            mustHaveUUIDList.add(ParagonValues.CHARACTERISTIC_BURNER_STATUS);
            mustHaveUUIDList.add(ParagonValues.CHARACTERISTIC_COOK_MODE);
            mustHaveUUIDList.add(ParagonValues.CHARACTERISTIC_COOK_CONFIGURATION);
            mustHaveUUIDList.add(ParagonValues.CHARACTERISTIC_CURRENT_COOK_STATE);
            mustHaveUUIDList.add(ParagonValues.CHARACTERISTIC_ELAPSED_TIME);
        }

        return mustHaveUUIDList;
    }

    public int getMustDataStatus(){
        int numGetData = 0;

        if(erdBatteryLevel != NO_BATTERY_INFO){
            numGetData++;
        }

        if(erdBurnerStatus != INITIAL_VALUE){
            numGetData++;
        }

        if(erdProbeConnectionStatue != INITIAL_VALUE){
            numGetData++;
        }

        if(erdCurrentCookMode != INITIAL_VALUE){
            numGetData++;
        }

        if(erdRecipeConfig != null ){
            numGetData++;
        }

        if(erdCookState != INITIAL_VALUE){
            numGetData++;
        }

        if(erdElapsedTime != INITIAL_ELAPSED_TIME){
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

            case ParagonValues.CHARACTERISTIC_BATTERY_LEVEL:
                setErdBatteryLevel(byteBuffer.get());
                Log.d(TAG, "CHARACTERISTIC_BATTERY_LEVEL :" + String.format("%02x", value[0]));
                break;

            case ParagonValues.CHARACTERISTIC_ELAPSED_TIME:
                Log.d(TAG, "CHARACTERISTIC_ELAPSED_TIME :" + String.format("%02x%02x", value[0], value[1]));
                setErdElapsedTime(byteBuffer.getShort());
                break;

            case ParagonValues.CHARACTERISTIC_BURNER_STATUS:
                Log.d(TAG, "CHARACTERISTIC_BURNER_STATUS :" + String.format("%02x", value[0]));
                setErdBurnerStatus(byteBuffer.get());
                break;

            case ParagonValues.CHARACTERISTIC_PROBE_CONNECTION_STATE:
                Log.d(TAG, "CHARACTERISTIC_PROBE_CONNECTION_STATE :" + String.format("%02x", value[0]));
                setErdProbeConnectionStatue(byteBuffer.get());
                break;

            case ParagonValues.CHARACTERISTIC_COOK_MODE:
                Log.d(TAG, "CHARACTERISTIC_COOK_MODE :" + String.format("%02x", value[0]));
                setErdCurrentCookMode(byteBuffer.get());
                break;

            case ParagonValues.CHARACTERISTIC_COOK_CONFIGURATION:
                Log.d(TAG, "CHARACTERISTIC_COOK_CONFIGURATION :");

                String data = "";
                for (int i = 0; i < value.length; i++) {
                    data += String.format("%02x", value[i]);
                }
                Log.d(TAG, "CONFIGURATION Data :" + data);

                RecipeInfo newRecipe = new RecipeInfo(value);
                setErdRecipeConfig(newRecipe);
                break;

            case ParagonValues.CHARACTERISTIC_CURRENT_COOK_STATE:
                Log.d(TAG, "CHARACTERISTIC_CURRENT_COOK_STATE :" + String.format("%02x", value[0]));
                setErdCookState(byteBuffer.get());
                break;

            case ParagonValues.CHARACTERISTIC_CURRENT_TEMPERATURE:
                Log.d(TAG, "CHARACTERISTIC_CURRENT_TEMPERATURE :" + String.format("%02x%02x", value[0], value[1]));
                setErdCurrentTemp(byteBuffer.getShort());
                break;


            case ParagonValues.CHARACTERISTIC_CURRENT_COOK_STAGE:
                Log.d(TAG, "CHARACTERISTIC_CURRENT_COOK_STAGE :" + String.format("%02x", value[0]));
                setErdCookStage(byteBuffer.get());
                break;


            case ParagonValues.CHARACTERISTIC_OTA_VERSION:
                Log.d(TAG, "CHARACTERISTIC_OTA_VERSION :" + String.format("%02x%02x%02x%02x%02x%02x", value[0], value[1], value[2], value[3], value[4], value[5]));
                setErdVersion(value[2], value[3], (short) value[4]);
                break;

            case ParagonValues.CHARACTERISTIC_CURRENT_POWER_LEVEL:
                Log.d(TAG, "CHARACTERISTIC_CURRENT_POWER_LEVEL :" + String.format("%02x", value[0]));
                setErdPowerLevel(byteBuffer.get());
                break;

        }

    }

    @Override
    public void updateDashboardItemUI(DashboardActivity.ProductListAdapter.ViewHolder holderDashboard) {

        holderDashboard.imageLogo.setImageResource(R.drawable.ic_paragon_logo);
        holderDashboard.imageMark.setImageResource(R.drawable.ic_paragon_mark);

        if (isProbeConnected()) {

            holderDashboard.imageBattery.setVisibility(View.VISIBLE);

            int level = getErdBatteryLevel();

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
            holderDashboard.textBattery.setText(batteryLevel);
        }
        else {
            holderDashboard.textBattery.setText("probe\noffline");
            holderDashboard.imageBattery.setVisibility(View.GONE);
        }

        if (getErdBurnerStatus() == ParagonValues.BURNER_STATE_START) {
            holderDashboard.textCooking.setText(R.string.product_state_cooking);
        }
        else {
            holderDashboard.textCooking.setText("");
        }
    }

    public void disconnected() {

        super.disconnected();

        this.erdProbeConnectionStatue = ParagonValues.PROBE_NOT_CONNECT;
        this.erdBatteryLevel = NO_BATTERY_INFO;
        this.erdCurrentTemp = 0.0f;
        this.erdElapsedTime = 0;
        this.erdRecipeConfig = null;
        this.erdBurnerStatus = INITIAL_VALUE;
        this.erdCookState = INITIAL_VALUE;
        this.erdCookStage = INITIAL_VALUE;
        this.erdCurrentCookMode = INITIAL_VALUE;
        this.erdElapsedTime = INITIAL_ELAPSED_TIME;

    }

    public float getErdCurrentTemp() {
        return erdCurrentTemp;
    }

    public void setErdCurrentTemp(short erdCurrentTemp) {
        this.erdCurrentTemp = (erdCurrentTemp / 100.0f);
    }

    public int getErdElapsedTime() {
        return erdElapsedTime;
    }

    public void setErdElapsedTime(int erdRemainingTime) {
        this.erdElapsedTime = erdRemainingTime;
    }

    public RecipeInfo getErdRecipeConfig() {
        return erdRecipeConfig;
    }

    public void setErdRecipeConfig(RecipeInfo erdRecipeConfig) {
        this.erdRecipeConfig = erdRecipeConfig;
    }

    public byte getErdBurnerStatus() {
        return erdBurnerStatus;
    }

    public void setErdBurnerStatus(byte erdBurnerStatus) {
        this.erdBurnerStatus = erdBurnerStatus;
    }

    public byte getErdCookState() {
        return erdCookState;
    }

    public void setErdCookState(byte erdCookState) {
        this.erdCookState = erdCookState;
    }

    public byte getErdCookStage() {
        return erdCookStage;
    }

    public void setErdCookStage(byte erdCookStage) {
        this.erdCookStage = erdCookStage;
    }

    public byte getErdCurrentCookMode() {
        return erdCurrentCookMode;
    }

    public void setErdCurrentCookMode(byte erdCurrentCookMode) {
        this.erdCurrentCookMode = erdCurrentCookMode;
    }

    public int getErdBatteryLevel() {
        return erdBatteryLevel;
    }

    public void setErdBatteryLevel(int erdBatteryLevel) {
        this.erdBatteryLevel = erdBatteryLevel;
    }

    public byte getErdProbeConnectionStatue() {
        return erdProbeConnectionStatue;
    }

    public void setErdProbeConnectionStatue(byte erdProbeConnectionStatue) {
        this.erdProbeConnectionStatue = erdProbeConnectionStatue;
    }

    public boolean isProbeConnected() {
        return (this.erdProbeConnectionStatue == ParagonValues.PROBE_CONNECT);
    }

    public void setErdPowerLevel(byte erdPowerLevel) {
        this.erdPowerLevel = erdPowerLevel;
    }

    public byte getErdPowerLevel() {
        return erdPowerLevel;
    }

    public void setErdVersion(byte major, byte minor, short build) {
        this.versionMajor = major;
        this.versionMinor = minor;
        this.versionBuild = build;
    }

    public byte getVersionMajor() {
        return versionMajor;
    }

    public byte getVersionMinor() {
        return versionMinor;
    }

    public short getVersionBuild() {
        return versionBuild;
    }

    public boolean isReceivedVersion() {
        if(versionMajor == 0 && versionMinor == 0 && versionBuild == 0){
            return false;
        }
        else{
            return true;
        }
    }

    public void createRecipeConfigForSousVide() {
        this.erdRecipeConfig = new RecipeInfo("", "", "", "");
        this.erdRecipeConfig.setType(RecipeInfo.TYPE_SOUSVIDE);
        this.erdRecipeConfig.addStage(new StageInfo());
    }

    public void sendRecipeConfig() {
        ByteBuffer valueBuffer = ByteBuffer.allocate(40);

        int numStage = this.erdRecipeConfig.numStage();

        for (int i = 0; i < numStage; i++) {
            StageInfo stage = this.erdRecipeConfig.getStage(i);

            valueBuffer.put(8 * i, (byte) stage.getSpeed());
            valueBuffer.putShort(1 + 8 * i, (short) (stage.getTime()));
            valueBuffer.putShort(3 + 8 * i, (short) (stage.getMaxTime()));
            valueBuffer.putShort(5 + 8 * i, (short) (stage.getTemp() * 100));
            valueBuffer.put(7 + 8 * i, (byte) (stage.isAutoTransition() ? 0x01 : 0x02));
        }

//        for (int i = 0; i < 40; i++) {
//            Log.d(TAG, "RecipeManager.sendCurrentStages:" + String.format("0x%02x", valueBuffer.array()[i]));
//        }

        BleManager.getInstance().writeCharacteristics(bluetoothDevice, ParagonValues.CHARACTERISTIC_COOK_CONFIGURATION, valueBuffer.array());
    }

}
