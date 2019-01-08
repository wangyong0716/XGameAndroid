package com.xgame.push.event;

/**
 * Created by jiangjh on 2018/2/2.
 */

public class InvitationEvent extends BaseEvent {

    public static final String PUSH_EVENT_INVITATION = "game_invitation";
    public static final String PUSH_EVENT_INVITATION_CANCEL = "game_invitation_cancel";
    public static final String PUSH_EVENT_INVITATION_REJECT = "game_invitation_reject";
    public static final String PUSH_EVENT_INVITATION_SUCCESS = "game_invitation_success";

    public InvitationEvent(String type, String content) {
        super(type, content);
    }
}
