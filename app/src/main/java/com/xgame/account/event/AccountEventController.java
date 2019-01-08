package com.xgame.account.event;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by wuyanzhi on 2018/1/25.
 */

public class AccountEventController {
    /**
     * 初始化账号完成事件
     */
    public static class InitAccountFinishEvent {
        public InitAccountFinishEvent() {
        }
    }

    public static void onActionInitAccountFinish() {
        InitAccountFinishEvent event = new InitAccountFinishEvent();
        EventBus.getDefault().post(event);
    }

    public static class UserChangeEvent {
        public static final UserChangeEvent LOGOUT = new UserChangeEvent();
    }

    /**
     * 注销
     */
    public static class LogoutEvent {
        public static final int EVENT_TYPE_NONE = 0;
        public static final int EVENT_TYPE_NORMAL_LOGOFF = 1;
        private int eventType = EVENT_TYPE_NONE;

        public LogoutEvent(int type) {
            this.eventType = type;
        }

        public int getEventType() {
            return eventType;
        }

        public void setEventType(int type) {
            this.eventType = type;
        }
    }


    /**
     * 登录事件
     */
    public static class LoginEvent<T> {
        public static final int EVENT_TYPE_NONE = 0;
        public static final int EVENT_TYPE_THIRD_APP_LOGIN_CANCEL = 1;
        public static final int EVENT_TYPE_THIRD_APP_LOGIN_SUCCESS = 2;
        public static final int EVENT_TYPE_THIRD_APP_LOGIN_FAILED = 3;
        public static final int EVENT_TYPE_THIRD_APP_LOGIN_EXCEPTION = 4;

        public static final int EVENT_TYPE_LOGIN_CANCEL = 5;
        public static final int EVENT_TYPE_LOGIN_SUCCESS = 6;
        public static final int EVENT_TYPE_LOGIN_FAILED = 7;
        public static final int EVENT_TYPE_LOGIN_EXCEPTION = 8;
        public static final int EVENT_TYPE_GET_VERIFICATION_SUCCESS = 9;
        public static final int EVENT_TYPE_GET_VERIFICATION_FAILED = 10;
        private int eventType = EVENT_TYPE_NONE;
        private int loginType; // SocialConstants.LOGIN
        private String msg;

        public LoginEvent(int resultType, int loginType, String msg) {
            this.eventType = resultType;
            this.loginType = loginType;
            this.msg = msg;
        }

        public int getEventType() {
            return eventType;
        }

        public void setEventType(int type) {
            this.eventType = type;
        }

        public int getLoginType() {
            return loginType;
        }

        public void setLoginType(int type) {
            this.loginType = type;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }
    }

    public static class UploadEvent<T> {
        public static final int EVENT_TYPE_NONE = 0;

        public static final int EVENT_TYPE_UPLOAD_CANCEL = 1;
        public static final int EVENT_TYPE_UPLOAD_SUCCESS = 2;
        public static final int EVENT_TYPE_UPLOAD_FAILED = 3;

        public static final int UPLOAD_TYPE_INFO = 1;
        public static final int UPLOAD_TYPE_AVATAR = 2;

        private int eventType = EVENT_TYPE_NONE;
        private int uploadType = 0;
        private T data;

        public UploadEvent(int type, int uploadType, T data) {
            this.eventType = type;
            this.uploadType = uploadType;
            this.data= data;
        }

        public int getUploadType() {
            return uploadType;
        }

        public void setUploadType(int uploadType) {
            this.uploadType = uploadType;
        }

        public int getEventType() {
            return eventType;
        }

        public void setEventType(int type) {
            this.eventType = type;
        }

        public T getData() {
            return data;
        }

        public void setData(T data) {
            this.data = data;
        }
    }

    // 通知用户登录
    public static void onActionLogin(int resultType, int loginType, String data) {
        LoginEvent event = new LoginEvent(resultType, loginType, data);
        EventBus.getDefault().post(event);
    }

    // 通知用户信息修改
    public static void onActionUpload(int resultType, int uploadType, Object data) {
        UploadEvent event = new UploadEvent(resultType, uploadType, data);
        EventBus.getDefault().post(event);
    }


    // 通知用户信息更新
    public static void onUserChangeEvent() {
        UserChangeEvent event = new UserChangeEvent();
        EventBus.getDefault().post(event);
    }


    public static void onActionLogOff(int type) {
        LogoutEvent event = new LogoutEvent(type);
        EventBus.getDefault().post(event);
    }
}
