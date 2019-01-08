package com.xgame.common.api;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.concurrent.Executor;

import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved.
 *
 * Created by jackwang
 * on 18-1-25.
 */


public class FutureCallAdapterFactory extends CallAdapter.Factory {

    private final Class<? extends Packable> mWrapType;

    private FutureCallAdapterFactory(Class<? extends Packable> wrapType) {
        this.mWrapType = wrapType;
    }

    public static CallAdapter.Factory create() {
        return create(null);
    }

    public static CallAdapter.Factory create(Class<? extends Packable> wrap) {
        return new FutureCallAdapterFactory(wrap);
    }

    @Nullable
    @Override
    public CallAdapter<?, ?> get(@NonNull Type returnType, Annotation[] annotations, Retrofit retrofit) {
        final Class<?> rawReturnType = getRawType(returnType);
        if (rawReturnType == FutureCall.class) {
            if (!(returnType instanceof ParameterizedType)) {
                throw new IllegalStateException("return type must be parameterized.");
            }
            final Class<? extends Packable> wrapType = mWrapType;
            final Type resType = getParameterUpperBound(0, (ParameterizedType) returnType);
            final Class<?> rawResType = getRawType(resType);
            boolean isPack = false;
            boolean isPackData = false;
            boolean noWrap = false;
            Type type;
            if (wrapType == null) {
                type = resType;
                noWrap = true;
            } else if (rawResType == Response.class) {
                if (!(resType instanceof ParameterizedType)) {
                    throw new IllegalStateException("Response must be parameterized"
                            + " as Response<Foo> or Response<? extends Foo>");
                }
                type = getParameterUpperBound(0, (ParameterizedType) resType);
            } else if (rawResType == wrapType) {
                if (!(resType instanceof ParameterizedType)) {
                    throw new IllegalStateException(wrapType + " must be parameterized"
                            + " as " + wrapType + "<Foo>"
                            + " or " + wrapType + "<? extends Foo>");
                }
                type = resType;
                isPack = true;
            } else if (Data.class.isAssignableFrom(rawResType)) {
                type = ParameterizedTypeAdapter.create(wrapType, resType);
                isPackData = true;
            } else if (rawResType.isArray()
                    && Data.class.isAssignableFrom(rawResType.getComponentType())) {
                type = ParameterizedTypeAdapter.create(wrapType, resType);
                isPackData = true;
            } else if (Collection.class.isAssignableFrom(rawResType)
                    && resType instanceof ParameterizedType
                    && Data.class.isAssignableFrom(
                    getRawType(getParameterUpperBound(0, (ParameterizedType) resType)))) {
                type = ParameterizedTypeAdapter.create(wrapType, resType);
                isPackData = true;
            } else {
                type = resType;
                noWrap = true;
            }
            return new FutureCallAdapter<>(type, retrofit.callbackExecutor(), isPack,
                    isPackData, noWrap, wrapType);
        }
        return null;
    }

    private static class FutureCallAdapter<R> implements CallAdapter<R, FutureCall> {

        private final Type responseType;

        private final Executor mCallbackExecutor;

        private final boolean isPackData;

        private final boolean isPack;

        private final boolean noWrap;

        private final Class<? extends Packable> packClz;

        FutureCallAdapter(Type responseType, Executor callbackExecutor, boolean isPack,
                boolean isPackData, boolean noWrap, Class<? extends Packable> packClz) {
            this.responseType = responseType;
            this.mCallbackExecutor = callbackExecutor;
            this.isPack = isPack;
            this.isPackData = isPackData;
            this.noWrap = noWrap;
            this.packClz = packClz;
        }

        @Override
        public Type responseType() {
            return this.responseType;
        }

        @Override
        public FutureCall adapt(@NonNull Call<R> call) {
            FutureResponseCall<?> respCall = new FutureResponseCall<>(call, mCallbackExecutor);
            FutureCall future = respCall;
            if (isPack) {
                future = new FuturePackableCall(respCall, packClz);
            } else if (isPackData) {
                FuturePackableCall<?> c = new FuturePackableCall(respCall, packClz);
                future = new FutureDataCall(c);
            } else if (noWrap) {
                future = new FutureCallImpl(respCall);
            }
            return future;
        }
    }

    private static class ParameterizedTypeAdapter implements ParameterizedType {

        private final Type mRawType;

        private final Type[] mActualType;

        private ParameterizedTypeAdapter(Type rawType, Type... actualType) {
            this.mRawType = rawType;
            this.mActualType = actualType;
        }

        static ParameterizedType create(Type rawType, Type... actualType) {
            return new ParameterizedTypeAdapter(rawType, actualType);
        }

        @Override
        public Type[] getActualTypeArguments() {
            return this.mActualType;
        }

        @Override
        public Type getRawType() {
            return this.mRawType;
        }

        @Override
        public Type getOwnerType() {
            return null;
        }
    }
}


