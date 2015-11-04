package com.firstbuild.androidapp.paragon.dataModel;

import java.util.ArrayList;

/**
 * Created by Hollis on 10/28/15.
 */
public class RecipeInfo {

    public static final int TYPE_SOUSVIDE = 1;
    public static final int TYPE_MULTI_STAGE = 2;

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

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void addStage(StageInfo stage){
        stages.add(stage);
    }

    public int numStage(){
        return stages.size();
    }

    public void deleteStage(int index){
        stages.remove(index);
    }

    public StageInfo getStage(int index){
        return stages.get(index);
    }

    public void setStageList(ArrayList<StageInfo> stageList){
        this.stages = (ArrayList)stageList.clone();
    }

    public ArrayList<StageInfo> getStageList(){
        return stages;
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
