package com.xgame.push;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Sets;
import com.xgame.R;
import com.xgame.app.XgameApplication;
import com.xgame.common.util.LogUtil;
import com.xgame.push.event.AnotherGameEvent;
import com.xgame.push.event.BWBattleMatchResultEvent;
import com.xgame.push.event.BWBonusEvent;
import com.xgame.push.event.BaseEvent;
import com.xgame.push.event.FriendPassEvent;
import com.xgame.push.event.FriendVerifyEvent;
import com.xgame.push.event.InvitationEvent;
import com.xgame.push.event.LeaveRoomEvent;
import com.xgame.push.event.MatchResultEvent;
import com.xgame.ui.activity.MainActivity;
import com.xiaomi.mipush.sdk.ErrorCode;
import com.xiaomi.mipush.sdk.MiPushClient;
import com.xiaomi.mipush.sdk.MiPushCommandMessage;
import com.xiaomi.mipush.sdk.MiPushMessage;
import com.xiaomi.mipush.sdk.PushMessageReceiver;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by jiangjianhe on 1/27/18.
 */
public class XGameMessageReceiver extends PushMessageReceiver {

    private Set<String> mInvitationSet = Sets.newHashSet(PushConstants.TYPE_GAME_INVITATION,
            PushConstants.TYPE_GAME_INVITATION_CANCEL, PushConstants.TYPE_GAME_INVITATION_REJECT,
            PushConstants.TYPE_GAME_INVITATION_SUCCESS);

    @Override
    public void onReceivePassThroughMessage(Context context, MiPushMessage message) {
        LogUtil.d(PushManager.TAG, "onReceivePassThroughMessage message: " + message);
        try {
            if (XgameApplication.getApplication().isInForground()) {
                String payload = message.getContent();
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(payload);
                String content = jsonNode.path("content").asText();
                String type = jsonNode.path("type").asText();
                BaseEvent event = generateEvent(type, content);
                if (event != null) {
                    EventBus.getDefault().post(event);
                }
            }

//            if (XgameApplication.getApplication().isInForground()) {
//                String content = jsonNode.path("content").asText();
//                String type = jsonNode.path("type").asText();
//                BaseEvent event = generateEvent(type, content);
//                if (event != null) {
//                    EventBus.getDefault().post(event);
//                }
//            }
//            else {
//                String title = jsonNode.path("title").asText();
//                if (Strings.isNullOrEmpty(title)) {
//                    title = context.getString(R.string.app_name);
//                }
//                String desc = jsonNode.path("desc").asText();
//                if (Strings.isNullOrEmpty(desc)) {
//                    return;
//                }
//                String intentUri = jsonNode.path("intent").asText();
//                Intent resultIntent = null;
//                if (Strings.isNullOrEmpty(intentUri)) {
//                    resultIntent = new Intent(context, MainActivity.class);
//                } else {
//                    resultIntent = Intent.parseUri(intentUri, Intent.URI_INTENT_SCHEME);
//                }
//                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//                NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Activity.NOTIFICATION_SERVICE);
//                Notification.Builder builder = new Notification.Builder(context);
//                builder.setContentTitle(title)
//                        .setContentText(desc)
//                        .setSmallIcon(R.drawable.ic_launcher)
//                        .setContentIntent(pendingIntent)
//                        .setAutoCancel(true)
//                        .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
//                Notification notification = builder.build();
//                mNotificationManager.notify(0, notification);
//            }
        } catch (Exception e) {
            Log.e(PushManager.TAG, "onReceivePassThroughMessage error: " + Throwables.getStackTraceAsString(e));
        }
    }

    @Override
    public void onNotificationMessageClicked(Context context, MiPushMessage message) {
        LogUtil.d(PushManager.TAG, "onNotificationMessageClicked message: " + message);
    }

    @Override
    public void onNotificationMessageArrived(Context context, MiPushMessage message) {
        LogUtil.d(PushManager.TAG, "onNotificationMessageArrived message: " + message);
        try {
            if (XgameApplication.getApplication().isInForground()) {
                String payload = message.getContent();
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(payload);
                String content = jsonNode.path("content").asText();
                String type = jsonNode.path("type").asText();
                BaseEvent event = generateEvent(type, content);
                if (event != null) {
                    EventBus.getDefault().post(event);
                }
            }
        } catch (Exception e) {
            Log.e(PushManager.TAG, "onNotificationMessageArrived error: " + Throwables.getStackTraceAsString(e));
        }
    }

    @Override
    public void onCommandResult(Context context, MiPushCommandMessage message) {
        LogUtil.d(PushManager.TAG, "onCommandResult message: " + message);
    }

    @Override
    public void onReceiveRegisterResult(Context context, MiPushCommandMessage message) {
        String command = message.getCommand();
        LogUtil.d(PushManager.TAG, "onReceiveRegisterResult message: " + message.toString());
        List<String> arguments = message.getCommandArguments();
        if (MiPushClient.COMMAND_REGISTER.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                String regId = ((arguments != null && arguments.size() > 0) ? arguments.get(0) : null);
                LogUtil.d(PushManager.TAG, "regId: " + regId);
                PushManager.setRegId(regId);
            }
        }
    }

    private BaseEvent generateEvent(String type, String content) {
        if (mInvitationSet.contains(type)) {
            return new InvitationEvent(type, content);
        } else if (PushConstants.TYPE_LEAVE_ROOM.equals(type)) {
            return new LeaveRoomEvent(type, content);
        } else if (PushConstants.TYPE_ANOTHER_GAME.equals(type)) {
            return new AnotherGameEvent(type, content);
        } else if (PushConstants.FRIEND_STATE_VERIFY.equals(type)) {
            return new FriendVerifyEvent(type, content);
        } else if (PushConstants.FRIEND_STATE_PASS.equals(type)) {
            return new FriendPassEvent(type, content);
        } else if (PushConstants.TYPE_BW_MATCH_SUCCESS.equals(type)) {
            return new BWBattleMatchResultEvent(type, content);
        } else if (PushConstants.TYPE_MATCH_SUCCESS.equals(type)) {
            return new MatchResultEvent(type, content);
        } else if (PushConstants.TYPE_BW_BONUS.equals(type)) {
            return new BWBonusEvent(type, content);
        }
        return null;
    }
}
