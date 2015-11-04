package com.firstbuild.androidapp.paragon.dataModel;

import com.firstbuild.androidapp.ParagonValues;
import com.firstbuild.commonframework.bleManager.BleManager;

import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * Created by Hollis on 11/2/15.
 */
public class RecipeManager {

    public static final int INVALID_INDEX = -1;

    private ArrayList<RecipeInfo> recipeInfos = new ArrayList<>();
    private int currentRecipeIndex = -1;
    private int currentStageIndex = -1;

    private boolean isCreated = false;

    private RecipeInfo currentRecipeInfo = null;
    private StageInfo currentStageInfo = null;

    private static RecipeManager ourInstance = new RecipeManager();

    public static RecipeManager getInstance() {
        return ourInstance;
    }

    private RecipeManager() {
    }

    public void add(RecipeInfo data) {
        recipeInfos.add(data);
    }

    public void remove(int index) {
        recipeInfos.remove(index);
    }

    public RecipeInfo get(int index) {
        return recipeInfos.get(index);
    }

    public int getSize() {
        return recipeInfos.size();
    }

    public RecipeInfo getCurrentRecipe() {
        return currentRecipeInfo;
    }

    public void setCurrentRecipe(int index) {
        currentRecipeIndex = index;
        RecipeInfo recipe = recipeInfos.get(index);

        currentRecipeInfo = new RecipeInfo(recipe.getImageFileName(), recipe.getName(),
                recipe.getIngredients(), recipe.getDirections());
        currentRecipeInfo.setStageList(recipe.getStageList());
    }

    public void restoreCurrentRecipe() {

        if(isCreated){
            //if created new one.
            recipeInfos.add(currentRecipeInfo);
            isCreated = false;
        }
        else{
            // or if get from recipe list.

            RecipeInfo recipe = recipeInfos.get(currentRecipeIndex);

            recipe.setImageFileName(currentRecipeInfo.getImageFileName());
            recipe.setName(currentRecipeInfo.getName());
            recipe.setIngredients(currentRecipeInfo.getIngredients());
            recipe.setDirections(currentRecipeInfo.getDirections());
            recipe.setStageList(currentRecipeInfo.getStageList());

            currentRecipeInfo = null;
            currentRecipeIndex = INVALID_INDEX;
        }

    }

    public void trashCurrentRecipe(){
        currentRecipeInfo = null;
        currentRecipeIndex = INVALID_INDEX;
        isCreated = false;
    }

    public StageInfo getCurrentStage() {
        return currentStageInfo;
    }

    public void setCurrentStage(int index) {
        currentStageIndex = index;
        StageInfo stage = getCurrentRecipe().getStage(index);

        currentStageInfo = new StageInfo(stage.getTime(), stage.getTemp(), stage.getSpeed(),
                stage.isAutoTransition(), stage.getDirection());
    }

    public void restoreCurrentStage() {
        StageInfo stage = getCurrentRecipe().getStage(currentStageIndex);

        stage.setTime(currentStageInfo.getTime());
        stage.setTemp(currentStageInfo.getTemp());
        stage.setSpeed(currentStageInfo.getSpeed());
        stage.setAutoTransition(currentStageInfo.isAutoTransition());
        stage.setDirection(currentStageInfo.getDirection());

        currentStageInfo = null;
        currentStageIndex = INVALID_INDEX;
    }


    public void ReadFromFile() {

        //TODO: Mocking up of reading from file.
        recipeInfos.clear();

        RecipeInfo recipe = new RecipeInfo(
                "a.png", "Hollis's world famous pot roast",
                "ingredient 1\ningredient 2\ningredient 3\n" +
                        "ingredient 3\n" +
                        "ingredient 3\n" +
                        "ingredient 3\n" +
                        "ingredient 3\n" +
                        "ingredient 3\n" +
                        "ingredient 3\n" +
                        "ingredient 3\n" +
                        "ingredient 3\n" +
                        "ingredient 3\n" +
                        "ingredient 3\n" +
                        "ingredient 3",
                "direction 1\ndirection 2"
        );
        recipe.addStage(new StageInfo(10, 120, 10, true, "direction A"));
        recipe.addStage(new StageInfo(11, 125, 5, false, "direction B"));
        recipe.addStage(new StageInfo(12, 130, 10, false, "direction C"));

        add(recipe);


        recipe = new RecipeInfo(
                "b.png", "Sous vide special ribeye",
                "ingredient 1\ningredient 2\ningredient 3",
                "direction 1\ndirection 2"
        );
        recipe.addStage(new StageInfo(10, 110, 10, true, "direction A"));

        add(recipe);

    }

    public void WriteToFile() {
        //TODO:

    }

    public int getCurrentStageIndex() {
        return currentStageIndex;
    }

    public void createRecipeMultiStage() {
        currentRecipeIndex = INVALID_INDEX;
        currentRecipeInfo = new RecipeInfo("", "", "", "");
        currentRecipeInfo.setType(RecipeInfo.TYPE_MULTI_STAGE);

        isCreated = true;
    }

    public void createRecipeSousVide() {
        currentRecipeIndex = INVALID_INDEX;
        currentRecipeInfo = new RecipeInfo("", "", "", "");
        currentRecipeInfo.setType(RecipeInfo.TYPE_SOUSVIDE);
        currentRecipeInfo.addStage(new StageInfo());

        currentStageInfo = currentRecipeInfo.getStage(0);
        currentStageIndex = 0;

        isCreated = true;
    }

    public void sendCurrentStages() {
        ByteBuffer valueBuffer = ByteBuffer.allocate(40);

        int numStage = getCurrentRecipe().numStage();

        for (int i = 0; i < numStage; i++) {
            StageInfo stage = getCurrentRecipe().getStage(i);

            valueBuffer.put(8 * i, (byte) stage.getSpeed());
            valueBuffer.putShort(1 + 8 * i, (short) (stage.getTime()));
            valueBuffer.putShort(3 + 8 * i, (short) (stage.getMaxTime()));
            valueBuffer.putShort(5 + 8 * i, (short) (stage.getTemp() * 100));
            valueBuffer.put(7 + 8 * i, (byte) (stage.isAutoTransition() ? 0x02 : 0x01));
        }

        BleManager.getInstance().writeCharateristics(ParagonValues.CHARACTERISTIC_COOK_CONFIGURATION, valueBuffer.array());

    }
}
