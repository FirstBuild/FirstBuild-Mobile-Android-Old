package com.firstbuild.androidapp.paragon.datamodel;

import java.util.ArrayList;

/**
 * Created by Hollis on 1/12/16.
 */
public class BuiltInRecipeInfo {
    public static final int TYPE_FOOD = 1;
    public static final int TYPE_SETTING = 2;

    public String name;
    public int type;

    public ArrayList<BuiltInRecipeInfo> child = null;
    public BuiltInRecipeInfo parent = null;

    public BuiltInRecipeInfo(String name) {
        this.name = name;
        this.type = TYPE_FOOD;
    }
}

