package com.firstbuild.androidapp.productManager;

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
}
