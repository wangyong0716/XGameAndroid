package com.xgame.battle.model;

import com.google.gson.annotations.SerializedName;
import com.xgame.base.api.DataProtocol;

/**
 * Created by wangyong on 18-2-1.
 */

public class ServerInviteResult implements DataProtocol {
    private String sessionId;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}
