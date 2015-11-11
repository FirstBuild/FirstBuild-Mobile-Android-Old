package com.firstbuild.androidapp.productManager;

import java.util.ArrayList;

public class ProductManager {

    private ArrayList<ProductInfo> products = new ArrayList<ProductInfo>();

    private static ProductManager ourInstance = new ProductManager();

    public static ProductManager getInstance() {
        return ourInstance;
    }

    private ProductManager() {
        // read from file.
        read();
    }

    private void read() {

        //TODO: currently added for test. It should come from internal storage.
        products.add(new ProductInfo(1, "1111", "MyParagon"));
//        products.add(new ProductInfo(0, "2222", "MyChillHub"));

    }

    /**
     * Store product information into intenal storage.
     */
    private void save(){

    }

    /**
     * Get the number of product.
     * @return Number of product.
     */
    public int getSize(){
        return products.size();
    }

    public ProductInfo getProduct(int index){
        return products.get(index);
    }

    /**
     * Once add a product app store its information into internal storage.
     * @param productInfo
     */
    public void addProduct(ProductInfo productInfo){
        products.add(productInfo);

        save();
    }

    public void deleteProduct(ProductInfo productInfo){

    }

    public ArrayList<ProductInfo> getProducts() {
        return products;
    }
}
