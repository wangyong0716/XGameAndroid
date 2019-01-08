package com.xgame.ui.activity.home.transform;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import com.xgame.R;
import com.xgame.app.GlideApp;
import com.xgame.common.util.IntentParser;
import com.xgame.common.util.LogUtil;
import com.xgame.home.model.XGameItem;
import com.xgame.util.Analytics;

import static android.text.TextUtils.isEmpty;
import static android.webkit.URLUtil.isHttpUrl;
import static android.webkit.URLUtil.isHttpsUrl;
import static com.xgame.common.util.LaunchUtils.startActivity;
import static com.xgame.common.util.TaggedTextParser.setTaggedText;
import static com.xgame.common.util.ToastUtil.showToast;
import static com.xgame.ui.activity.CommonWebViewActivity.startWeb;
import static com.xgame.util.UrlUtils.getRequestUrl;

/**
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved.
 *
 * Created by jackwang
 * on 18-1-30.
 */


public class ViewHolderWrapper extends RecyclerView.ViewHolder {

    private static final String TAG = ViewHolderWrapper.class.getSimpleName();

    private static final String GIF = ".gif";
    //    public static final String ID = "id";

    private final ImageView mImage;

    private final TextView mTitle;

    private final TextView mSubTitle;

    private final TextView mStamp;

    private final TextView mRemind;

    ViewHolderWrapper(View itemView) {
        super(itemView);
        mImage = itemView.findViewById(R.id.image);
        mTitle = itemView.findViewById(R.id.title);
        mSubTitle = itemView.findViewById(R.id.sub_title);
        mStamp = itemView.findViewById(R.id.stamp);
        mRemind = itemView.findViewById(R.id.remind);
    }

    private static void setText(TextView tv, String text) {
        if (tv != null) {
            setTaggedText(tv, text);
        }
    }

    private static void doTrackEvent(Intent ext) {
        final String uri = ext.getDataString();
        if (uri == null) {
            return;
        }
        String gameId = IntentParser.getString(ext, XGameItem.EXTRA_GAME_ID);
        String gameName = IntentParser.getString(ext, XGameItem.EXTRA_GAME_NAME);
        if (uri.startsWith(XGameItem.REDIRECT_BATTLE_MATCH.toString())) {
            //真人对战首页各游戏点击
            Analytics.trackClickEvent(Analytics.Constans.SUBTYPE_CLICK, gameId,
                    gameName, Analytics.Constans.STOCK_TYPE_GAME,
                    Analytics.Constans.PAGE_BATTLE_HOME, Analytics.Constans.SECTION_GAME_LIST,
                    null);
        } else if (uri.startsWith(XGameItem.REDIRECT_BATTLE_COIN.toString())) {
            //金币场首页各游戏点击
            Analytics.trackClickEvent(Analytics.Constans.SUBTYPE_CLICK, gameId,
                    gameName, Analytics.Constans.STOCK_TYPE_GAME,
                    Analytics.Constans.PAGE_ARENA_HOME, Analytics.Constans.SECTION_GAME_LIST, null);
        }
    }

    void onBind(@NonNull ViewData vo, List payloads) {
        // TODO: 18-1-31 payloads
        onBind(vo);
    }

    void onBind(@NonNull final ViewData vo) {
        // TODO: 18-1-31 viewId <==> viewData relative process1
        final Context context = itemView.getContext();
        final Intent ext = vo.extension();
        final String title = vo.title(mTitle);
        if (ext != null) {
            itemView.setOnClickListener(new InnerClickListener(context, ext, title));
        } else {
            itemView.setOnClickListener(null);
        }
        String src;
        if (mImage != null && !isEmpty((src = vo.image(mImage)))) {
            if (src.endsWith(GIF)) {
                GlideApp.with(context).asGif().load(src).into(mImage);
            } else {
                GlideApp.with(context).load(src).into(mImage);
            }
        }
        setText(mTitle, title);
        setText(mSubTitle, vo.subTitle(mSubTitle));
        setText(mStamp, vo.stamp(mStamp));
        if (mRemind != null) {
            mRemind.setVisibility(vo.remind(mRemind) ? View.VISIBLE : View.INVISIBLE);
        }
    }

    private static class InnerClickListener implements View.OnClickListener {

        private final Intent mExt;

        private final Context mContext;

        private final String mTitle;

        InnerClickListener(Context context, Intent ext, String title) {
            mExt = ext;
            mContext = context;
            mTitle = title;
        }

        @Override
        public void onClick(View v) {
            try {
                final String uri = mExt.getDataString();
                if (isHttpUrl(uri) || isHttpsUrl(uri)) {
                    startWeb(mContext, getRequestUrl(uri), mTitle);
                    return;
                }
                startActivity(mContext, mExt);
                doTrackEvent(mExt);
            } catch (Exception e) {
                LogUtil.e(TAG, "start activity fail: %s", e);
                showToast(mContext, mContext.getString(R.string.lauch_fail, e));
            }
        }
    }
//    private void loadViewMethod(Class<? extends Viewable> clz) {
//        String pkg = null;
//        Scope scope = clz.getAnnotation(Scope.class);
//        if (scope != null) {
//            pkg = scope.value();
//        }
//        Method[] methods = clz.getDeclaredMethods();
//        for (Method m : methods) {
//            ViewID viewID = m.getAnnotation(ViewID.class);
//            int id = 0;
//            if (viewID != null) {
//                id = viewID.value();
//                if (id == 0) {
//                    throw new IllegalStateException(format("id is 0, on %s", m));
//                }
//            }
//            View view = null;
//            if (id != 0) {
//                view = findViewById(id);
//            }
//            if (view == null) {
//                view = findViewByIdentifier(m.getName(), pkg);
//            }
//            if (view == null) {
//                throw new IllegalStateException(
//                        format("can't find view by id(%s) or name(%s)", id, m.getName()));
//            }
//
//        }
//    }
//    private <T extends View> T findViewById(int id) {
//        return itemView.findViewById(id);
//    }
//
//    private <T extends View> T findViewByIdentifier(String identifier, String scope) {
//        int id = itemView.getResources().getIdentifier(identifier, ID, scope);
//        return itemView.findViewById(id);
//    }
//    private static void validateViewableObject(Viewable vo) {
//        final Class<? extends Viewable> clz = vo.getClass();
//        if (clz.isInterface()) {
//            throw new IllegalStateException(format("must be implement %s", vo));
//        }
//        if (clz.getInterfaces().length > 1) {
//            throw new IllegalStateException(format("can't implement other interface %s",
//                    Arrays.toString(clz.getInterfaces())));
//        }
//    }
}
