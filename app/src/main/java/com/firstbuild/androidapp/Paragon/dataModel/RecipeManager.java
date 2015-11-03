package com.firstbuild.androidapp.paragon.dataModel;

import java.util.ArrayList;

/**
 * Created by Hollis on 11/2/15.
 */
public class RecipeManager {

    public static final int INVALID_INDEX = -1;

    private ArrayList<RecipeDataInfo> recipeDataInfos = new ArrayList<>();
    private int currentRecipe = -1;
    private int currentStage = -1;

    private static RecipeManager ourInstance = new RecipeManager();

    public static RecipeManager getInstance() {
        return ourInstance;
    }

    private RecipeManager() {
    }

    public void add(RecipeDataInfo data) {
        recipeDataInfos.add(data);
    }

    public void remove(int index) {
        recipeDataInfos.remove(index);
    }

    public RecipeDataInfo get(int index) {
        return recipeDataInfos.get(index);
    }

    public int getSize() {
        return recipeDataInfos.size();
    }

    public RecipeDataInfo getCurrentRecipe() {
        RecipeDataInfo recipeDataInfo;

        if (currentRecipe == INVALID_INDEX) {
            recipeDataInfo = null;
        }
        else {
            recipeDataInfo = recipeDataInfos.get(currentRecipe);
        }

        return recipeDataInfo;
    }

    public void setCurrentRecipe(int index) {
        currentRecipe = index;
    }

    public StageInfo getCurrentStage() {
        StageInfo stageInfo;

        if (currentRecipe == INVALID_INDEX || currentStage == INVALID_INDEX) {
            stageInfo = null;
        }
        else {
            stageInfo = recipeDataInfos.get(currentRecipe).getStage(currentStage);
        }

        return stageInfo;
    }

    public void setCurrentStage(int index) {
        currentStage = index;
    }


    public void ReadFromFile() {

        //TODO: Mocking up of reading from file.

        RecipeDataInfo recipe = new RecipeDataInfo(
                "a.png", "Hollis world famous pot roast",
                "ingredient 1\ningredient 2\ningredient 3",
                "direction 1\ndirection 2"
        );
        recipe.addStage(new StageInfo(30, 120, 10, true, "direction A"));

        add(recipe);


        recipe = new RecipeDataInfo(
                "b.png", "Sous vide special ribeye",
                "ingredient 1\ningredient 2\ningredient 3",
                "direction 1\ndirection 2"
        );
        recipe.addStage(new StageInfo(10, 110, 10, true, "direction A"));
        recipe.addStage(new StageInfo(20, 120, 30, false, "direction B"));
        recipe.addStage(new StageInfo(30, 130, 40, true, "direction C"));

        add(recipe);

    }

    public void WriteToFile() {
        //TODO:

    }

    public int getCurrentStageIndex() {
        return currentStage;
    }
}
