package com.xgame.battle.model;

import com.xgame.base.api.DataProtocol;

/**
 * Created by zhanglianyu on 18-2-3.
 */

public class BWBattlePlayer implements DataProtocol {
    private long uId; // long, 用户id
    private String name; // string
    private String avatar; // string，头像url
    private int sex; // int, 2:女， 1:男, -1:未知
    private int age; // int
    private int robot;
    private int robotLevel;

    public long getuId() {
        return uId;
    }

    public void setuId(long uId) {
        this.uId = uId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getRobot() {
        return robot;
    }

    public void setRobot(int robot) {
        this.robot = robot;
    }

    public int getRobotLevel() {
        return robotLevel;
    }

    public void setRobotLevel(int robotLevel) {
        this.robotLevel = robotLevel;
    }

    @Override
    public String toString() {
        return "BWBattlePlayer{" +
                "uId=" + uId +
                ", name='" + name + '\'' +
                ", avatar='" + avatar + '\'' +
                ", sex=" + sex +
                ", age=" + age +
                ", robot=" + robot +
                ", robotLevel=" + robotLevel +
                '}';
    }
}
