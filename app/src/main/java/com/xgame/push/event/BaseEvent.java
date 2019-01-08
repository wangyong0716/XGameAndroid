package com.xgame.push.event;

/**
 * Created by jiangjh on 2018/2/2.
 */

public abstract class BaseEvent {
    private String mType;
    private String mContent;

    public BaseEvent(String type, String content) {
        mType = type;
        mContent = content;
    }

    public String getType() {
        return mType;
    }

    public String getContent() {
        return mContent;
    }
}
