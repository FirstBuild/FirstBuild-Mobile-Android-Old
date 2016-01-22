package com.firstbuild.androidapp.productManager;

import com.firstbuild.commonframework.bleManager.BleDevice;

import org.json.JSONException;
import org.json.JSONObject;

public class ProductInfo {

    public static final int PRODUCT_TYPE_CILLHUB = 0;
    public static final int PRODUCT_TYPE_PARAGON = 1;
    public static final int NO_BATTERY_INFO = -1;

    //type can be Paragon and Chilhub so far.
    public int type = -1;
    public String address = "";
    public String nickname = "";

    //properties for UI.
    public boolean isProductConnected = false;
    public boolean isProbeConnected = false;
    public int batteryLevel = NO_BATTERY_INFO;
    public String cookMode = "";

    public BleDevice bleDevice = null;


    public ProductInfo(int type, String address, String nickname) {
        this.address = address;
        this.nickname = nickname;
        this.type = type;
        this.batteryLevel = -1;
        this.cookMode = "";
        this.isProbeConnected = false;
        this.isProductConnected = false;
    }

    public ProductInfo(ProductInfo product) {
        this.type = product.type;
        this.address = product.address;
        this.nickname = product.nickname;
        this.batteryLevel = -1;
        this.cookMode = "";
        this.isProbeConnected = false;
        this.isProductConnected = false;
    }

    public ProductInfo(JSONObject jsonObject) {

        try {
            this.type = jsonObject.getInt("type");
        } catch (JSONException e) {
            this.type = PRODUCT_TYPE_PARAGON;
        }


        try {
            this.nickname = jsonObject.getString("nickname");

        } catch (JSONException e) {
            this.nickname = "";
        }


        try {
            this.address = jsonObject.getString("address");

        } catch (JSONException e) {
            this.address = "";
        }

    }


    public JSONObject toJson(){

        JSONObject object  = new JSONObject();

        try {
            object.put("type", type);
            object.put("nickname", nickname);
            object.put("address", address);

        } catch (JSONException e) {
            //let content has ""
        }

        return object;
    }




}
