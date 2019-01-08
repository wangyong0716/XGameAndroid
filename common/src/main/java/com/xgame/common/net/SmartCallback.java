package com.xgame.common.net;

public interface SmartCallback<T> {
    public boolean validResponseForCache(T t);
}