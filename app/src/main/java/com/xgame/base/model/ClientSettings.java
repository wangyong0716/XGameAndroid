package com.xgame.base.model;

import android.content.Context;

import com.miui.zeus.utils.CollectionUtils;
import com.xgame.R;
import com.xgame.base.api.DataProtocol;
import com.xgame.util.StringUtil;

import java.io.Serializable;

public class ClientSettings implements DataProtocol, Serializable {

    private static final long serialVersionUID = 7324790006240447840L;

    private static final int SHARE_TYPE_LINK = 0;
    private static final int SHARE_TYPE_IMAGE = 1;

    /**
     * Server config.
     */
    public String servers;
    public String domain;

    public int ipIndex;
    public String[] ips;

    public boolean hasDomain() {
        return !StringUtil.isEmpty(domain);
    }

    public boolean hasIps() {
        if (StringUtil.isEmpty(servers)) {
            return false;
        } else {
            if (ips == null) {
                ips = servers.split(",");
            }
            return !CollectionUtils.isEmpty(ips);
        }
    }

    public String getNextIp() {
        return CollectionUtils.get(ips, ipIndex++);
    }

    public void reset() {
        ipIndex = 0;
    }

    /**
     * 金币商城
     */
    public String mall;

    /**
     * 提现
     */
    public String cash;

    /**
     * 提现详情
     */
    public String cashRecord;

    /**
     * 分享链接
     */
    public String share;
    public int shareType = SHARE_TYPE_LINK;    // SHARE_TYPE_IMAGE, SHARE_TYPE_LINK
    public String shareTitle;
    public String shareSummary;

    public boolean isShareImage() {
        return shareType == SHARE_TYPE_IMAGE;
    }

    public boolean isShareLink() {
        return shareType == SHARE_TYPE_LINK;
    }

    public String getShareTitle(Context context) {
        return StringUtil.isEmpty(shareTitle) ? context.getString(R.string.share_title) : shareTitle;
    }

    public String getShareSummary(Context context) {
        return StringUtil.isEmpty(shareSummary) ? context.getString(R.string.share_summary) : shareSummary;
    }

    public String getShareLink(Context context) {
        return StringUtil.isEmpty(share) ? context.getString(R.string.share_link) : share;
    }
}