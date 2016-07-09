package com.firstbuild.androidapp.productmanager;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import com.firstbuild.androidapp.dashboard.DashboardActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public abstract class ProductInfo {

    private static String TAG = ProductInfo.class.getSimpleName();

    public static final int PRODUCT_TYPE_CILLHUB = 0;
    public static final int PRODUCT_TYPE_PARAGON = 1;
    public static final int PRODUCT_TYPE_OPAL = 2;

    public static final byte INITIAL_VALUE = 0x0f;
    public static final int INITIAL_ELAPSED_TIME = 0xffff;

    public static final String KEY_PRODUCT_TYPE = "type";
    public static final String KEY_PRODUCT_NICKNAME = "nickname";
    public static final String KEY_PRODUCT_ADDRESS = "address";

    //product type
    public int type = -1;
    public String address = "";
    public String nickname = "";
    public BluetoothDevice bluetoothDevice = null;

    //properties get from device.
    protected boolean isConnected = false;

    protected boolean isAllMustDataReceived = false;

    protected int NUM_MUST_INIT_DATA = -1;

    protected ArrayList<String> mustHaveUUIDList = new ArrayList<>();
    protected ArrayList<String> mustHaveNotificationUUIDList = new ArrayList<>();

    public boolean isAllMustDataReceived() {
        return isAllMustDataReceived;
    }

    public abstract void initMustData();

    public abstract int getMustDataStatus();

    public abstract int getNumMustInitData();

    public abstract void updateErd(String uuid, byte[] value);

    public abstract void updateDashboardItemUI(DashboardActivity.ProductListAdapter.ViewHolder holderDashboard);

    public abstract ArrayList<String> getMustHaveUUIDList();

    public abstract ArrayList<String> getMustHaveNotificationUUIDList();

    public ProductInfo(int type, String address, String nickname) {
        this.address = address;
        this.nickname = nickname;
        this.type = type;
        this.isConnected = false;
    }

    public ProductInfo(ProductInfo product) {
        this.type = product.type;
        this.address = product.address;
        this.nickname = product.nickname;
        this.isConnected = false;
    }

    public ProductInfo(JSONObject jsonObject) {

        try {
            this.type = jsonObject.getInt(KEY_PRODUCT_TYPE);
        }
        catch (JSONException e) {
            Log.e(TAG, e.toString());
        }

        try {
            this.nickname = jsonObject.getString(KEY_PRODUCT_NICKNAME);

        }
        catch (JSONException e) {
            this.nickname = "";
        }


        try {
            this.address = jsonObject.getString(KEY_PRODUCT_ADDRESS);

        }
        catch (JSONException e) {
            this.address = "";
        }

    }


    public JSONObject toJson() {

        JSONObject object = new JSONObject();

        try {
            object.put(KEY_PRODUCT_TYPE, type);
            object.put(KEY_PRODUCT_NICKNAME, nickname);
            object.put(KEY_PRODUCT_ADDRESS, address);

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
        }
    }

    public boolean isConnected() {
        return isConnected;
    }

    public static ProductInfo buildProductInfo(JSONObject productObject) throws JSONException {

        int type = productObject.getInt(ProductInfo.KEY_PRODUCT_TYPE);

        ProductInfo ret = null;

        switch(type) {
            case ProductInfo.PRODUCT_TYPE_OPAL:
                ret = new OpalInfo(productObject);
                break;
            case ProductInfo.PRODUCT_TYPE_PARAGON:
                ret = new ParagonInfo(productObject);
                break;
            default:
                break;
        }

        return ret;

    }
}
