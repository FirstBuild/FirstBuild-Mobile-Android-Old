package com.firstbuild.androidapp.productManager;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.firstbuild.androidapp.FirstBuildApplication;
import com.firstbuild.androidapp.ParagonValues;
import com.firstbuild.androidapp.R;
import com.firstbuild.androidapp.paragon.MultiStageStatusFragment;
import com.firstbuild.androidapp.paragon.OtaManager;
import com.firstbuild.androidapp.paragon.SousvideStatusFragment;
import com.firstbuild.androidapp.paragon.dataModel.RecipeInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
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
        if (index >= products.size()) {
            return null;
        }

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



    public void updateErd(String address, String uuid, byte[] value) {
        Log.d(TAG, "updateErd Address :" + address);

        ProductInfo product = getProductByAddress(address);
        ByteBuffer byteBuffer = ByteBuffer.wrap(value);

        if (product == null) {
            return;
        }

        switch (uuid.toUpperCase()) {

            case ParagonValues.CHARACTERISTIC_BATTERY_LEVEL:
                product.setErdBatteryLevel(byteBuffer.get());
                Log.d(TAG, "CHARACTERISTIC_BATTERY_LEVEL :" + String.format("%02x", value[0]));
                break;

            case ParagonValues.CHARACTERISTIC_ELAPSED_TIME:
                Log.d(TAG, "CHARACTERISTIC_ELAPSED_TIME :" + String.format("%02x%02x", value[0], value[1]));
                product.setErdElapsedTime(byteBuffer.getShort());
                break;

            case ParagonValues.CHARACTERISTIC_BURNER_STATUS:
                Log.d(TAG, "CHARACTERISTIC_BURNER_STATUS :" + String.format("%02x", value[0]));
                product.setErdBurnerStatus(byteBuffer.get());
                break;

            case ParagonValues.CHARACTERISTIC_PROBE_CONNECTION_STATE:
                Log.d(TAG, "CHARACTERISTIC_PROBE_CONNECTION_STATE :" + String.format("%02x", value[0]));
                product.setErdProbeConnectionStatue(byteBuffer.get());
                break;

            case ParagonValues.CHARACTERISTIC_COOK_MODE:
                Log.d(TAG, "CHARACTERISTIC_COOK_MODE :" + String.format("%02x", value[0]));
                product.setErdCurrentCookMode(byteBuffer.get());
                break;

            case ParagonValues.CHARACTERISTIC_COOK_CONFIGURATION:
                Log.d(TAG, "CHARACTERISTIC_COOK_CONFIGURATION :");

                String data = "";
                for (int i = 0; i < value.length; i++) {
                    data += String.format("%02x", value[i]);
                }
                Log.d(TAG, "CONFIGURATION Data :" + data);

                RecipeInfo newRecipe = new RecipeInfo(value);
                product.setErdRecipeConfig(newRecipe);
                break;

            case ParagonValues.CHARACTERISTIC_CURRENT_COOK_STATE:
                Log.d(TAG, "CHARACTERISTIC_CURRENT_COOK_STATE :" + String.format("%02x", value[0]));
                product.setErdCookState(byteBuffer.get());
                break;

            case ParagonValues.CHARACTERISTIC_CURRENT_TEMPERATURE:
                Log.d(TAG, "CHARACTERISTIC_CURRENT_TEMPERATURE :" + String.format("%02x%02x", value[0], value[1]));
                product.setErdCurrentTemp(byteBuffer.getShort());
                break;


            case ParagonValues.CHARACTERISTIC_CURRENT_COOK_STAGE:
                Log.d(TAG, "CHARACTERISTIC_CURRENT_COOK_STAGE :" + String.format("%02x", value[0]));
                product.setErdCookStage(byteBuffer.get());
                break;


            case ParagonValues.CHARACTERISTIC_OTA_VERSION:
                Log.d(TAG, "CHARACTERISTIC_OTA_VERSION :" + String.format("%02x%02x%02x%02x%02x%02x", value[0], value[1], value[2], value[3], value[4], value[5]));
                product.setErdVersion(value[2], value[3], (short) value[4]);
                break;

            case ParagonValues.CHARACTERISTIC_CURRENT_POWER_LEVEL:
                Log.d(TAG, "CHARACTERISTIC_CURRENT_POWER_LEVEL :" + String.format("%02x", value[0]));
                product.setErdPowerLevel(byteBuffer.get());
                break;

        }
    }

    public ProductInfo getProductByAddress(String address) {

        for (ProductInfo productInfo : products) {
            if (productInfo.address.equals(address)) {
                return productInfo;
            }
        }

        return null;
    }
}
