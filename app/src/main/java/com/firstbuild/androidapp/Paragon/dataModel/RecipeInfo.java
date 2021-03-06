package com.firstbuild.androidapp.paragon.datamodel;

import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Hollis on 10/28/15.
 */
public class RecipeInfo {

    public static final int TYPE_SOUSVIDE = 1;
    public static final int TYPE_MULTI_STAGE = 2;
    private static String TAG = RecipeInfo.class.getSimpleName();
    private final int MAX_RECIPE = 5;
    private final int STAGE_CHUNK_SIZE = 8;
    private int type;
    private String imageFileName;
    private String name;
    private String ingredients;
    private String directions;
    private ArrayList<StageInfo> stages = new ArrayList<>();


    public RecipeInfo(String imageFileName, String name, String ingredients, String directions) {
        this.type = TYPE_MULTI_STAGE;
        this.imageFileName = imageFileName;
        this.name = name;
        this.ingredients = ingredients;
        this.directions = directions;
    }

    public RecipeInfo(byte[] value) {

        int size = value.length / STAGE_CHUNK_SIZE;
        for (int i = 0; i < size; i++) {
            byte powerLevel = value[i * STAGE_CHUNK_SIZE];
            short holdTime = (short) ((value[i * STAGE_CHUNK_SIZE + 1] & 0xff) << 8 | (value[i * STAGE_CHUNK_SIZE + 2] & 0xff));
            short maxHoldTime = (short) ((value[i * STAGE_CHUNK_SIZE + 3] & 0xff) << 8 | (value[i * STAGE_CHUNK_SIZE + 4] & 0xff));
            short targetTemp = (short) ((value[i * STAGE_CHUNK_SIZE + 5] & 0xff) << 8 | (value[i * STAGE_CHUNK_SIZE + 6] & 0xff));
            byte transitionType = value[i * STAGE_CHUNK_SIZE + 7];

            if (powerLevel == 0) {
                break;
            }
            else {
                StageInfo newStage = new StageInfo();
                newStage.setSpeed(powerLevel);
                newStage.setTime(holdTime);
                newStage.setMaxTime(maxHoldTime);
                newStage.setTemp(targetTemp / 100);
                newStage.setAutoTransition(transitionType == 0x01);

                addStage(newStage);
            }
        }
    }

    public void addStage(StageInfo stage) {
        stages.add(stage);
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int numStage() {
        return stages.size();
    }

    public void deleteStage(int index) {
        stages.remove(index);
    }

    public StageInfo getStage(int index) {
        StageInfo stageInfo = null;

        if (index < stages.size()) {
            stageInfo = stages.get(index);
        }
        else {
            Log.d(TAG, "Error on getStage");
            stageInfo = null;
        }

        return stageInfo;
    }

    public ArrayList<StageInfo> getStageList() {
        return stages;
    }

    public void setStageList(ArrayList<StageInfo> stageList) {
        this.stages = (ArrayList) stageList.clone();
    }

    public String getImageFileName() {
        return imageFileName;
    }

    public void setImageFileName(String imageFileName) {
        this.imageFileName = imageFileName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIngredients() {
        return ingredients;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    public String getDirections() {
        return directions;
    }

    public void setDirections(String directions) {
        this.directions = directions;
    }

}
