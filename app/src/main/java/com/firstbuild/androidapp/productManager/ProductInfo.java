package com.firstbuild.androidapp.productManager;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import com.firstbuild.androidapp.ParagonValues;
import com.firstbuild.androidapp.paragon.dataModel.RecipeInfo;
import com.firstbuild.androidapp.paragon.dataModel.StageInfo;
import com.firstbuild.commonframework.bleManager.BleManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;

public class ProductInfo {

    public static final int PRODUCT_TYPE_CILLHUB = 0;
    public static final int PRODUCT_TYPE_PARAGON = 1;
    public static final int NO_BATTERY_INFO = -1;
    public static final int NUM_MUST_INIT_DATA = 6;

    public static final byte INITIAL_VALUE = 0x0f;
    public static final byte INITIAL_ELAPSED_TIME = (byte) 0xff;
    private static String TAG = ProductInfo.class.getSimpleName();
    //type can be Paragon and Chilhub so far.
    public int type = -1;
    public String address = "";
    public String nickname = "";
    public BluetoothDevice bluetoothDevice = null;
    //properties get from device.
    private boolean isConnected = false;

    // Initial data must get from dashboard.
    private int erdBatteryLevel = NO_BATTERY_INFO;
    private byte erdBurnerStatus = INITIAL_VALUE;
    private byte erdProbeConnectionStatue = INITIAL_VALUE;
    private byte erdCurrentCookMode = INITIAL_VALUE;
    private RecipeInfo erdRecipeConfig = null;
    private byte erdCookState = INITIAL_VALUE;

    private float erdCurrentTemp;
    private int erdElapsedTime = INITIAL_ELAPSED_TIME;
    private byte erdCookStage;
    private byte erdPowerLevel = 0;

    private boolean isAllMustDataReceived = false;


    public boolean isAllMustDataReceived() {
        return isAllMustDataReceived;
    }

    public void initMustData(){
        erdBatteryLevel = NO_BATTERY_INFO;
        erdBurnerStatus = INITIAL_VALUE;
        erdProbeConnectionStatue = INITIAL_VALUE;
        erdCurrentCookMode = INITIAL_VALUE;
        erdRecipeConfig = null;
        erdCookState = INITIAL_VALUE;

        isAllMustDataReceived = false;
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

        if(numGetData == NUM_MUST_INIT_DATA){
            isAllMustDataReceived = true;
        }
        else{
            isAllMustDataReceived = false;
        }

        return numGetData;
    }


    public ProductInfo(int type, String address, String nickname) {
        this.address = address;
        this.nickname = nickname;
        this.type = type;
        this.erdBatteryLevel = -1;
        this.isConnected = false;
    }

    public ProductInfo(ProductInfo product) {
        this.type = product.type;
        this.address = product.address;
        this.nickname = product.nickname;
        this.erdBatteryLevel = -1;
        this.isConnected = false;
    }

    public ProductInfo(JSONObject jsonObject) {

        try {
            this.type = jsonObject.getInt("type");
        }
        catch (JSONException e) {
            this.type = PRODUCT_TYPE_PARAGON;
        }


        try {
            this.nickname = jsonObject.getString("nickname");

        }
        catch (JSONException e) {
            this.nickname = "";
        }


        try {
            this.address = jsonObject.getString("address");

        }
        catch (JSONException e) {
            this.address = "";
        }

    }


    public JSONObject toJson() {

        JSONObject object = new JSONObject();

        try {
            object.put("type", type);
            object.put("nickname", nickname);
            object.put("address", address);

        }
        catch (JSONException e) {
            //let content has ""
        }

        return object;
    }


    public void connected() {
        if (this.isConnected == false) {
            this.isConnected = true;
        }
    }

    public void disconnected() {
        if (this.isConnected == true) {
            this.isConnected = false;
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

        for (int i = 0; i < 40; i++) {
            Log.d(TAG, "RecipeManager.sendCurrentStages:" + String.format("0x%02x", valueBuffer.array()[i]));
        }

        BleManager.getInstance().writeCharacteristics(bluetoothDevice, ParagonValues.CHARACTERISTIC_COOK_CONFIGURATION, valueBuffer.array());
    }


    public void createRecipeConfigForSousVide() {
        this.erdRecipeConfig = new RecipeInfo("", "", "", "");
        this.erdRecipeConfig.setType(RecipeInfo.TYPE_SOUSVIDE);
        this.erdRecipeConfig.addStage(new StageInfo());
    }



    public boolean isConnected() {
        return isConnected;
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
}
