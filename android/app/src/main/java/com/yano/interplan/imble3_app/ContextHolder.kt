package com.yano.interplan.imble3_app

import android.app.Activity
import com.facebook.react.bridge.ReactApplicationContext


/**
 * React Nativeコンテキストの受け渡し
 */
object ContextHolder {
    private var applicationContext: ReactApplicationContext? = null

    fun init(context: ReactApplicationContext?) {
        applicationContext = context
    }

    val activity: Activity?
        get() {
            checkNotNull(applicationContext) { "activity is null" }
            return applicationContext!!.currentActivity
        }

    val context: ReactApplicationContext
        get() {
            checkNotNull(applicationContext) { "ContextHolder is not initialized. Call init() with application context first." }
            return applicationContext!!
        }
}
