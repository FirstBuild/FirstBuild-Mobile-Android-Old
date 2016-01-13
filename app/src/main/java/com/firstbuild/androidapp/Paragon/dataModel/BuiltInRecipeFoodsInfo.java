package com.firstbuild.androidapp.paragon.dataModel;

import java.util.ArrayList;

public class BuiltInRecipeFoodsInfo extends BuiltInRecipeInfo{
    public ArrayList<BuiltInRecipeInfo> child;

    public BuiltInRecipeFoodsInfo(String name) {
        child = new ArrayList<BuiltInRecipeInfo>();
        this.type = BuiltInRecipeInfo.TYPE_FOOD;
        this.name = name;
    }
}
