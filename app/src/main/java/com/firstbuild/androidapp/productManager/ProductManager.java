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

        products.add(new ProductInfo(1, "1111", "MyParagon"));
        products.add(new ProductInfo(0, "2222", "MyChillHub"));

    }

    private void save(){

    }

    public int getSize(){
        return products.size();
    }

    public ProductInfo getProduct(int index){
        return products.get(index);
    }

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
