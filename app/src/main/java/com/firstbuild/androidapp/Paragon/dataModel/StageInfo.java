package com.firstbuild.androidapp.paragon.dataModel;

/**
 * Created by Hollis on 10/29/15.
 */
public class StageInfo {
    public static final int TYPE_ADD_ITEM = -1;
    public static final int TYPE_NORMAL = 0;

    private int type;
    private int time;
    private int maxTime;
    private int temp;
    private int speed;
    private boolean autoTransition;
    private String direction;

    public StageInfo(int time, int temp, int speed, boolean autoTransition, String direction) {
        this.type = TYPE_NORMAL;
        this.time = time;
        this.maxTime = 0;
        this.temp = temp;
        this.speed = speed;
        this.autoTransition = autoTransition;
        this.direction = direction;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getTemp() {
        return temp;
    }

    public void setTemp(int temp) {
        this.temp = temp;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public boolean isAutoTransition() {
        return autoTransition;
    }

    public void setAutoTransition(boolean autoTransition) {
        this.autoTransition = autoTransition;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public void setMaxTime(int maxTime) {
        this.maxTime = maxTime;
    }

    public int getMaxTime() {
        return maxTime;
    }
}
