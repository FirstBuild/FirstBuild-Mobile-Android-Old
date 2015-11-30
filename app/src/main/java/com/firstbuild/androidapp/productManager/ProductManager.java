package com.firstbuild.androidapp.productManager;

import android.content.Context;
import android.content.SharedPreferences;

import com.firstbuild.androidapp.FirstBuildApplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ProductManager {
    public static final String PREFS_NAME = "FIRSTBUILD_APP";
    public static final String FAVORITES = "Favorite";
    private static ProductManager ourInstance = new ProductManager();
    private ArrayList<ProductInfo> products = new ArrayList<ProductInfo>();

    private ProductManager() {

        // read from file.
        try {
            read();
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

//    private void read() {
//
//        //TODO: currently added for test. It should come from internal storage.
//        products.add(new ProductInfo(1, "1111", "MyParagon"));
////        products.add(new ProductInfo(0, "2222", "MyChillHub"));
//
//    }

    public static ProductManager getInstance() {
        return ourInstance;
    }

    /**
     * Get the number of product.
     *
     * @return Number of product.
     */
    public int getSize() {
        return products.size();
    }

    public ProductInfo getProduct(int index) {
        return products.get(index);
    }

    /**
     * Add Paragon to the list
     *
     * @param deviceAddress Address.
     */
    public void addProduct(String deviceAddress) {
        //TODO: Check if the nick name is duplicated;
        addProduct(new ProductInfo(ProductInfo.PRODUCT_TYPE_PARAGON, "Paragon", deviceAddress));
    }

    /**
     * Once add a product app store its information into internal storage.
     *
     * @param productInfo
     */
    public void addProduct(ProductInfo productInfo) {
        products.add(productInfo);

        write();
    }


    /**
     * Remove product in the list.
     *
     * @param index index of the list.
     */
    public void removeProdct(int index) {
        products.remove(index);
    }


    /**
     * Load registerd Paragon from SharedPreference.
     *
     * @throws JSONException
     */
    public void read() throws JSONException {

        SharedPreferences settings = FirstBuildApplication.getContext().getSharedPreferences(
                PREFS_NAME, Context.MODE_PRIVATE);

        if (settings.contains(FAVORITES)) {
            String jsonString = settings.getString(FAVORITES, null);

            JSONArray arrayProductObject = new JSONArray(jsonString);

            for (int i = 0; i < arrayProductObject.length(); i++) {
                JSONObject productObject = arrayProductObject.getJSONObject(i);

                ProductInfo productInfo = new ProductInfo(productObject);

                products.add(productInfo);
            }
        }
        else {
            //do nothing
        }

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

    public ArrayList<ProductInfo> getProducts() {
        return products;
    }
}
