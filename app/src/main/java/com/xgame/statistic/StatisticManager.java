package com.xgame.statistic;

import android.util.ArrayMap;
import android.util.Base64;

import com.xgame.app.XgameApplication;
import com.xgame.common.api.OnCallback;
import com.xgame.common.util.LogUtil;
import com.xgame.statistic.api.StatisticResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import static com.xgame.base.ServiceFactory.statisticService;

public class StatisticManager {
    private static class Holder {
        static final StatisticManager INSTANCE = new StatisticManager();
    }

    private static final String TAG = "StatisticManager";
    private Map<String, Object> mCommonMap;

    private StatisticManager() {

    }

    public static StatisticManager getInstance() {
        return Holder.INSTANCE;
    }

    public void trackRealData(Map<String, Object> map) {
        if (map == null || map.size() == 0) {
            return;
        }
        map.putAll(getCommonMap());
        JSONObject data = new JSONObject();
        for (String key : map.keySet()) {
            try {
                data.put(key, data.get(key));
            } catch (JSONException e) {
            }
        }
        final String dataString = data.toString();
        LogUtil.json(TAG, dataString);
        statisticService().track(Base64
                .encodeToString(
                        dataString.getBytes(), Base64.NO_WRAP)).enqueue(new OnCallback<StatisticResult>() {
            @Override
            public void onResponse(StatisticResult result) {

            }

            @Override
            public void onFailure(StatisticResult result) {
//                try {
//                    writeToFile(dataString);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
            }
        });
    }

    private Map<String, Object> getCommonMap() {
        if (mCommonMap == null) {
            mCommonMap = new ArrayMap<>();
        }
        return mCommonMap;
    }

    protected void writeToFile(String data) throws IOException {
        FileWriter fw = null;
        BufferedWriter bw = null;
        try {
            fw = new FileWriter(getTrackFile(), true);
            bw = new BufferedWriter(fw);
            bw.write(data + "\n");
            bw.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bw != null) {
                bw.close();
            }
            if (fw != null) {
                fw.close();
            }
        }
    }


    protected File getTrackFile() throws IOException {
        File dir = new File(XgameApplication.getApplication().getApplicationInfo().dataDir, "/files/analytics/");
        if (!dir.exists() || !dir.isDirectory()) {
            if (!dir.mkdirs()) {
                LogUtil.e(TAG, "failed to create the data folder");
            }
        }
        File file = new File(dir, "track");
        if (!file.exists()) {
            file.createNewFile();
        }
        return file;
    }
}
