package com.yano.interplan.imble3_app;


import android.app.Activity;
import android.content.Context;

import com.facebook.react.bridge.ReactApplicationContext;

/**
 * React Nativeコンテキストの受け渡し
 */
public class ContextHolder {
    private static ReactApplicationContext applicationContext;

    public static void init(ReactApplicationContext context) {
        applicationContext = context;
    }
    public static Activity getActivity(){
        if (applicationContext == null) {
            throw new IllegalStateException("activity is null");
        }
        return applicationContext.getCurrentActivity();
    }

    public static ReactApplicationContext getContext() {
        if (applicationContext == null) {
            throw new IllegalStateException("ContextHolder is not initialized. Call init() with application context first.");
        }
        return applicationContext;
    }
}
