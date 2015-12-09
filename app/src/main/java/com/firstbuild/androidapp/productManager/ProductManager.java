package com.firstbuild.androidapp.productManager;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.firstbuild.androidapp.FirstBuildApplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ProductManager {
    public static final String PREFS_NAME = "FIRSTBUILD_APP";
    public static final String FAVORITES = "Favorite";
    private static ProductManager ourInstance = new ProductManager();
    private String TAG = ProductManager.class.getSimpleName();
    private ArrayList<ProductInfo> products = new ArrayList<ProductInfo>();
    private int selectedIndex = -1;

    private ProductManager() {
    }

    public static ProductManager getInstance() {
        return ourInstance;
    }

    public ProductInfo getProduct(int index) {
        return products.get(index);
    }

    /**
     * Add Paragon to the list
     *
     * @param deviceAddress Address.
     */
    public void add(String deviceAddress) {
        //TODO: Check if the nick name is duplicated;
        add(new ProductInfo(ProductInfo.PRODUCT_TYPE_PARAGON, "Paragon", deviceAddress));
    }

    /**
     * Once add a product app store its information into internal storage.
     *
     * @param productInfo
     */
    public void add(ProductInfo productInfo) {
        products.add(productInfo);

        write();
    }

    /**
     * Save the list of Paragon to SharedPreference.
     */
    public void write() {
        SharedPreferences settings;
        SharedPreferences.Editor editor;

        settings = FirstBuildApplication.getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = settings.edit();

        JSONArray arrayProduct = new JSONArray();

        for (ProductInfo productInfo : products) {
            arrayProduct.put(productInfo.toJson());
        }

        String jsonString = arrayProduct.toString();

        editor.putString(FAVORITES, jsonString);
        editor.commit();
    }

    /**
     * Remove product in the list.
     *
     * @param index index of the list.
     */
    public void remove(int index) {
        products.remove(index);

        write();
    }

    /**
     * Load registerd Paragon from SharedPreference.
     *
     * @throws JSONException
     */
    public void read() {

        SharedPreferences settings = FirstBuildApplication.getContext().getSharedPreferences(
                PREFS_NAME, Context.MODE_PRIVATE);

        if (settings.contains(FAVORITES)) {
            String jsonString = settings.getString(FAVORITES, null);

            try {

                JSONArray arrayProductObject = null;
                arrayProductObject = new JSONArray(jsonString);
                for (int i = 0; i < arrayProductObject.length(); i++) {
                    JSONObject productObject = arrayProductObject.getJSONObject(i);

                    ProductInfo productInfo = new ProductInfo(productObject);

                    products.add(productInfo);
                }

            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else {
            //do nothing
        }


        Log.d(TAG, "read DONE");

    }

    public ProductInfo getCurrent() {
        ProductInfo productInfo = null;

        if (0 <= selectedIndex && selectedIndex < getSize()) {
            productInfo = products.get(selectedIndex);
        }

        return productInfo;
    }

    /**
     * Get the number of product.
     *
     * @return Number of product.
     */
    public int getSize() {
        return products.size();
    }

    public void setCurrent(int index) {
        selectedIndex = index;
    }

    public ArrayList<ProductInfo> getProducts() {
        return products;
    }
}
