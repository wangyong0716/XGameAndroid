package com.xgame.invite.model;

import com.xgame.base.api.DataProtocol;

/**
 * Created by Albert
 * on 18-1-28.
 */

public class GameInfo implements DataProtocol {

    private String id;
    private String name;
    private String icon;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    @Override
    public String toString() {
        return "GameInfo{" +
                "name='" + name + '\'' +
                ", icon='" + icon + '\'' +
                '}';
    }
}
