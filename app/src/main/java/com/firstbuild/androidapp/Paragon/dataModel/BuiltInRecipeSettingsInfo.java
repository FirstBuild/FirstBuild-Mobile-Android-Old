package com.firstbuild.androidapp.paragon.dataModel;

import java.util.ArrayList;

public class BuiltInRecipeSettingsInfo extends BuiltInRecipeInfo {
    public int id;
    public ArrayList<String> doneness;
    public ArrayList<Float> thickness;
    public ArrayList<RecipeSetting> recipeSettings;

    public BuiltInRecipeSettingsInfo(String name) {
        super(name);
        this.doneness = new ArrayList<>();
        this.thickness = new ArrayList<>();
        this.recipeSettings = new ArrayList<>();

        this.type = BuiltInRecipeInfo.TYPE_SETTING;
    }


    public void addRecipeSetting(int temp, float timeMin, float timeMax) {
        recipeSettings.add(new RecipeSetting(temp, timeMin, timeMax));
    }


    public RecipeSetting getRecipeSetting(int doneness, int thickness) {
        int where = 0;

        if (this.thickness.isEmpty()) {
            where = doneness;
        }
        else {
            where = doneness * (this.thickness.size() - 1) + thickness;
        }

        return recipeSettings.get(where);
    }


    public class RecipeSetting {
        public int temp;
        public float timeMin;
        public float timeMax;

        public RecipeSetting(int temp, float timeMin, float timeMax) {
            this.temp = temp;
            this.timeMin = timeMin;
            this.timeMax = timeMax;
        }
    }

}
