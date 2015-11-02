package com.firstbuild.androidapp.paragon.dataModel;

import com.firstbuild.androidapp.paragon.StageAdapter;

import java.util.ArrayList;

/**
 * Created by Hollis on 10/28/15.
 */
public class RecipeDataInfo {
    private String imageFileName;
    private String name;
    private String ingredients;
    private String directions;

    private ArrayList<StageInfo> stages = new ArrayList<>();


    public RecipeDataInfo(String imageFileName, String name, String ingredients, String directions) {
        this.imageFileName = imageFileName;
        this.name = name;
        this.ingredients = ingredients;
        this.directions = directions;
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
