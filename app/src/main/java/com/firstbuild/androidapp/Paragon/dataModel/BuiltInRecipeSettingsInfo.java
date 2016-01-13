package com.firstbuild.androidapp.paragon.dataModel;

import java.util.ArrayList;

public class BuiltInRecipeSettingsInfo extends BuiltInRecipeInfo {
    public int id;
    public ArrayList<String> doneness;
    public ArrayList<Float> minThickness;
    public ArrayList<Float> maxThickness;
    public ArrayList<RecipeSetting> recipeSettings;

    public BuiltInRecipeSettingsInfo(String name) {
        doneness = new ArrayList<String>();
        minThickness = new ArrayList<Float>();
        maxThickness = new ArrayList<Float>();
        recipeSettings = new ArrayList<RecipeSetting>();

        this.type = BuiltInRecipeInfo.TYPE_SETTING;
        this.name = name;
    }

    public void addRecipeSetting(int temp, float timeMin, float timeMax){
        recipeSettings.add(new RecipeSetting(temp, timeMin, timeMax));
    }

    class RecipeSetting {
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
