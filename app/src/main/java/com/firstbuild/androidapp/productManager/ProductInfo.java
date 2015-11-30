package com.firstbuild.androidapp.productManager;

import org.json.JSONException;
import org.json.JSONObject;

public class ProductInfo {

    public static final int PRODUCT_TYPE_CILLHUB = 0;
    public static final int PRODUCT_TYPE_PARAGON = 1;

    //type can be Paragon and Chilhub so far.
    public int type;
    public String address;
    public String nickname;


    public ProductInfo(int type, String address, String nickname) {
        this.address = address;
        this.nickname = nickname;
        this.type = type;
    }

    public ProductInfo(ProductInfo product) {
        this.type = product.type;
        this.address = product.address;
        this.nickname = product.nickname;
    }

    public ProductInfo(JSONObject jsonObject) {

        try {
            this.nickname = jsonObject.getString("nickname");
            this.address = jsonObject.getString("address");

        } catch (JSONException e) {
            this.nickname = "";
            this.address = "";
        }

    }


    public JSONObject toJson(){

        JSONObject object  = new JSONObject();

        try {
            object.put("nickname", nickname);
            object.put("address", address);

        } catch (JSONException e) {
            //let content has ""
        }

        return object;
    }




}
