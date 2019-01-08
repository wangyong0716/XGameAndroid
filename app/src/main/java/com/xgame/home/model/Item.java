package com.xgame.home.model;

import android.content.Intent;
import android.net.Uri;
import android.webkit.URLUtil;

import com.xgame.common.api.IProtocol;

/**
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved.
 * <p>
 * Created by jackwang
 * on 18-1-25.
 */


public abstract class Item implements IProtocol {

    protected String img;

    protected String title;

    protected String subTitle;

    protected String extension;

    protected String stamp;

    @ItemType
    protected int type;

    private static final Uri sHomeUri = Uri.parse("xgame://pk.baiwan.com/home");

    private static boolean isAppUri(String extension) {
        return extension != null && extension.startsWith("xgame://");
    }

    public String img() {
        return img;
    }

    public String title() {
        return title;
    }

    public String subTitle() {
        return subTitle;
    }

    public Intent extension() {
        if (URLUtil.isNetworkUrl(extension)) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(extension));
            return intent;
        } else if (isAppUri(extension)) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(convert2redirectUri(extension));
            return intent;
        }
        return null;
    }

    private static Uri convert2redirectUri(String ext) {
        Uri uri = Uri.parse(ext);
        String query = uri.getQuery();
        String encodedPath = uri.getEncodedPath();
        if (encodedPath == null) {
            return uri;
        }
        return sHomeUri.buildUpon().fragment(encodedPath).encodedQuery(query).build();
    }

    public String stamp() {
        return stamp;
    }

    @ItemType
    public int type() {
        return this.type;
    }

    @Override
    public String toString() {
        return "Item{" +
                ", img='" + img + '\'' +
                ", title='" + title + '\'' +
                ", subTitle='" + subTitle + '\'' +
                ", extension='" + extension + '\'' +
                ", stamp='" + stamp + '\'' +
                ", type=" + type +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Item)) {
            return false;
        }
        final Item item = (Item) o;
        if (type != item.type) {
            return false;
        }
        if (img != null ? !img.equals(item.img) : item.img != null) {
            return false;
        }
        if (title != null ? !title.equals(item.title) : item.title != null) {
            return false;
        }
        if (subTitle != null ? !subTitle.equals(item.subTitle) : item.subTitle != null) {
            return false;
        }
        if (extension != null ? !extension.equals(item.extension) : item.extension != null) {
            return false;
        }
        return stamp != null ? stamp.equals(item.stamp) : item.stamp == null;
    }

    @Override
    public int hashCode() {
        int result = img != null ? img.hashCode() : 0;
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (subTitle != null ? subTitle.hashCode() : 0);
        result = 31 * result + (extension != null ? extension.hashCode() : 0);
        result = 31 * result + (stamp != null ? stamp.hashCode() : 0);
        result = 31 * result + type;
        return result;
    }
}

