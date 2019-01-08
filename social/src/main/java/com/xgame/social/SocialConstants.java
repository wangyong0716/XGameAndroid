package com.xgame.social;

/**
 * Created by wuyanzhi on 2018/1/24.
 */

public class SocialConstants {

    public class LOGIN {
        public static final int TYPE_PHONE_NUM_VERIFICATION_CODE = -1;
        public static final int TYPE_PHONE_NUM_PASSOWRD = 0;
        public static final int TYPE_QQ = 1;
        public static final int TYPE_WEIBO = 2;
        public static final int TYPE_WECHAT = 3;
        public static final int TYPE_XIAOMI = 4;
        public static final int TYPE_LOGINRESULT = 2001;
        public static final int TYPE_DEFAULT = -2;
    }

    public interface QQ {
        public static final String APP_ID = "1106648335";
        public static final String SCOPE = "get_simple_userinfo,add_topic";
    }

    //微信
    public class WX {
        public static final String PACKAGE_NAME = "com.tencent.mm";
        public static final String BASE_URL_USER_INFO = "https://api.weixin.qq.com/";
        public static final String SECRET = "ae90df6fb6838a757e15eb02cb00b119";
        public static final String GRANT_TYPE = "authorization_code";

        public static final String APP_ID = "wxc1b90431f093fabe";
        public static final String SCOPE = "snsapi_userinfo";
        public static final String STATE = "xiangkan_wechat";

    }

    //微博
    public class WB {
        public static final String APP_KEY = "1530996005";
        public static final String REDIRECT_URL = "https://www.sina.com";
        //        public static final String REDIRECT_URL = "http://sns.whalecloud.com/sina2/callback";
        public static final String SCOPE = "email,direct_messages_read,direct_messages_write," +
                "friendships_groups_read,friendships_groups_write,statuses_to_me_read," +
                "follow_app_official_microblog," + "invitation_write";
    }
}
