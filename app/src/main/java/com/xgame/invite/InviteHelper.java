package com.xgame.invite;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.PhoneLookup;
import android.text.TextUtils;
import android.view.View;

import com.miui.zeus.utils.CollectionUtils;
import com.xgame.account.UserManager;
import com.xgame.account.model.User;
import com.xgame.app.XgameApplication;
import com.xgame.base.ServiceFactory;
import com.xgame.common.api.OnCallback;
import com.xgame.common.util.ExecutorHelper;
import com.xgame.common.util.MD5Util;
import com.xgame.common.util.SharePrefUtils;
import com.xgame.invite.model.InvitedUser;
import com.xgame.ui.activity.invite.util.ShareInvoker;
import com.xgame.util.StringUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Albert
 * on 18-1-29.
 */

public class InviteHelper {

    private ShareInvoker mShareInvoker;
    private InviteListener mInviteListener;

    /**
     * Prefs
     */
    private static final String PREF_NAME = "user_friends";
    private static final String KEY_CONTACTS = "contacts";

    private static String sContactsMatched = "";

    public InviteHelper() {

    }

    public void setup(Context context, InviteListener listener, ShareInvoker.ShareListener shareListener) {
        mInviteListener = listener;
        mShareInvoker = new ShareInvoker();
        mShareInvoker.setShareListener(shareListener);
        sContactsMatched = getPrefContacts();
    }

    private String appendContacts(String contactStr, String phone) {
        if (StringUtil.isEmpty(contactStr)) {
            contactStr = phone;
        } else {
            contactStr += ("," + phone);
        }
        return contactStr;
    }

    private boolean saveContactsToPref(String contactStr) {
        Context context = XgameApplication.getApplication();
        if (TextUtils.equals(sContactsMatched, contactStr)) {
            return false;
        } else {
            sContactsMatched = contactStr;
            SharePrefUtils.putString(context, getFriendPref(), KEY_CONTACTS, sContactsMatched);
            return !StringUtil.isEmpty(sContactsMatched);
        }
    }

    private String getPrefContacts() {
        Context context = XgameApplication.getApplication();
        return SharePrefUtils.getString(context, getFriendPref(), KEY_CONTACTS, "");
    }

    private void crossMatch(final List<InvitedUser> matched, final Map<String, InvitedUser> all) {
        ExecutorHelper.runInBackground(new Runnable() {
            @Override
            public void run() {
                String contactStr = "";
                if (!CollectionUtils.isEmpty(matched)) {
                    for (InvitedUser matchedUser : matched) {
                        InvitedUser founded = all.get(matchedUser.getPhone());
                        matchedUser.setContactName(founded.getContactName());
                        contactStr = appendContacts(contactStr, matchedUser.getPhone());
                    }
                }
                boolean updated = saveContactsToPref(contactStr);
                if (mInviteListener != null) {
                    mInviteListener.onMatchFinished(updated);
                }
            }
        });
    }

    public void startMatch(final Context context) {
        if (mInviteListener == null) {
            return;
        }
        ExecutorHelper.runInBackground(new Runnable() {
            @Override
            public void run() {
                final Map<String, InvitedUser> all = new HashMap<>();
                String contactStr = fetchContacts(context, all);
                if (StringUtil.isEmpty(contactStr)) {
                    mInviteListener.onMatchFailed(all);
                    return;
                }
                ServiceFactory.inviteService().matchUser(contactStr).enqueue(new OnCallback<List<InvitedUser>>() {
                    @Override
                    public void onResponse(List<InvitedUser> matched) {
                        mInviteListener.onMatched(matched, all);
                        crossMatch(matched, all);
                    }

                    @Override
                    public void onFailure(List<InvitedUser> result) {
                        mInviteListener.onMatchFailed(all);
                    }
                });
            }
        });
    }

    private String fetchContacts(Context context, Map<String, InvitedUser> contactsMap) {
        String[] columns = {PhoneLookup.DISPLAY_NAME, CommonDataKinds.Phone.NUMBER};
        Cursor cursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                columns, null, null, null);
        if (cursor == null) {
            return null;
        }
        StringBuilder csb = new StringBuilder();
        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToPosition(i);
            int numberFieldColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            String number = cursor.getString(numberFieldColumnIndex);
            if (StringUtil.isEmpty(number)) {
                continue;
            } else {
                number = number.replaceAll("\\s*", "");
            }
            if (isAccountPhone(number)) {
                continue;
            }
            if (csb.length() > 0) {
                csb.append(",");
            }
            csb.append(number);
            // Mapping contacts with phone.
            if (contactsMap != null) {
                int nameFieldColumnIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME);
                String name = cursor.getString(nameFieldColumnIndex);
                InvitedUser user = new InvitedUser();
                user.setContactName(name);
                user.setNickname(name);
                user.setPhone(number);
                contactsMap.put(number, user);
            }
        }
        cursor.close();
        return String.valueOf(csb);
    }

    public void setShareView(View shareView) {
        mShareInvoker.setShareView(shareView);
    }

    public void onShareInDialog() {
        mShareInvoker.shareInDialog();
    }

    public void onInviteThirdUser(View v) {
        mShareInvoker.onInviteThirdUser(v);
    }

    public void release() {

    }

    public static String getFriendPref() {
        return PREF_NAME + getUserId();
    }

    public static String getUserId() {
        return MD5Util.MD5_32(String.valueOf(UserManager.getInstance().getUserId()));
    }

    public static boolean isAccountUser(String userId) {
        return TextUtils.equals(String.valueOf(UserManager.getInstance().getUserId()), userId);
    }

    public static boolean isAccountPhone(String phone) {
        User user = UserManager.getInstance().getUser();
        return user != null && TextUtils.equals(user.getPhone(), phone);
    }

    public interface InviteListener {
        void onMatched(List<InvitedUser> matched, Map<String, InvitedUser> all);

        void onMatchFailed(Map<String, InvitedUser> all);

        void onMatchFinished(boolean updated);
    }
}
