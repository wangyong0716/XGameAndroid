package com.xgame.common.net;


import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RetrofitHttpClient {

    private static class Holder {
        static final RetrofitHttpClient INSTANCE = new RetrofitHttpClient();
    }

    public static RetrofitHttpClient getInstance() {
        return Holder.INSTANCE;
    }

    private RetrofitHttpClient() {
    }


    public <T> T forRetrofit(String baseUrl, Class<T> service) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .addConverterFactory(ScalarsConverterFactory.create())
                .client(OKHttpClientHelper.create())
                .build();
        return retrofit.create(service);
    }

}
