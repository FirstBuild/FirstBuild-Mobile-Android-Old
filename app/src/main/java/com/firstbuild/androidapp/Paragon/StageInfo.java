package com.firstbuild.androidapp.paragon;

/**
 * Created by Hollis on 10/29/15.
 */
public class StageInfo {
    private int time;
    private int temp;
    private int speed;
    private boolean autoTransition;
    private String direction;

    public StageInfo(int time, int temp, int speed, boolean autoTransition, String direction) {
        this.time = time;
        this.temp = temp;
        this.speed = speed;
        this.autoTransition = autoTransition;
        this.direction = direction;
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
}
