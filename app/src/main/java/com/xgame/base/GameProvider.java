package com.xgame.base;

import android.support.annotation.Nullable;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import com.xgame.common.api.FutureCall;
import com.xgame.common.api.FutureCallHelper;
import com.xgame.home.model.BattleTabPage;
import com.xgame.home.model.XGameItem;

import static com.xgame.base.GameProvider.GameProfile.create;
import static com.xgame.base.ServiceFactory.homeService;
import static com.xgame.common.util.ExecutorHelper.runInBackground;

/**
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved.
 *
 * Created by jackwang
 * on 18-2-3.
 */


public class GameProvider {

    private static final GameProvider sProvider = new GameProvider();

    private static final AtomicReference<Map<String, GameProfile>> sData = new AtomicReference<>();

    private GameProvider() {
    }

    public static GameProvider get() {
        return sProvider;
    }

    public static void postFetch() {
        runInBackground(new Runnable() {
            @Override
            public void run() {
                sProvider.fetch();
            }
        });
    }

    public GameProfile load(String gameId) {
        Map<String, GameProfile> d = loadData();
        return d != null ? d.get(gameId) : null;
    }

    public Collection<GameProfile> list() {
        Map<String, GameProfile> d = loadData();
        return d != null ? d.values() : null;
    }

    public void postUpdate(final List<XGameItem> gameList) {
        if (gameList == null || gameList.isEmpty()) {
            return;
        }
        runInBackground(new Runnable() {
            @Override
            public void run() {
                inflateData(gameList);
            }
        });
    }

    public Map<String, GameProfile> loadData() {
        Map<String, GameProfile> d = sData.get();
        if (d == null) {
            synchronized (sData) {
                d = sData.get();
                if (d == null) {
                    d = fetch();
                }
            }
        }
        return d;
    }

    private Map<String, GameProfile> fetch() {
        FutureCall<BattleTabPage> tab = homeService().loadBattleTab(0, 0);
        try {
            BattleTabPage p = FutureCallHelper.get(tab.submit());
            if (p == null) {
                return null;
            }
            return inflateData(p.items());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Nullable
    private Map<String, GameProfile> inflateData(List<XGameItem> gameList) {
        int size = gameList.size();
        if (size == 0) {
            return null;
        }
        Map<String, GameProfile> m = new LinkedHashMap<>(size);
        for (XGameItem g : gameList) {
            m.put(g.gameId(), create(g));
        }
        sData.getAndSet(m);
        return m;
    }

    public static class GameProfile implements Serializable {

        private static final long serialVersionUID = 1L;

        public final String name;

        public final String icon;

        public final String id;

        public final String url;

        GameProfile(String name, String icon, String id, String url) {
            this.name = name;
            this.icon = icon;
            this.id = id;
            this.url = url;
        }

        static GameProfile create(XGameItem g) {
            return new GameProfile(g.title(), g.icon(), g.gameId(), g.gameUrl());
        }
    }

}
