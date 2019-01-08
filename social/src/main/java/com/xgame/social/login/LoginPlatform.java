package com.xgame.social.login;

import android.support.annotation.IntDef;

import com.xgame.social.SocialConstants;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by shaohui on 2016/12/1.
 */

public class LoginPlatform {

    @Documented
    @IntDef({QQ, WX, WEIBO})
    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.PARAMETER)
    public @interface Platform {

    }

    public static final int QQ = SocialConstants.LOGIN.TYPE_QQ;

    public static final int WX = SocialConstants.LOGIN.TYPE_WECHAT;

    public static final int WEIBO = SocialConstants.LOGIN.TYPE_WEIBO;
}
