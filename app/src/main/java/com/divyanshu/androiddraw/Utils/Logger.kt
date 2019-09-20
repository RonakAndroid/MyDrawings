package com.divyanshu.androiddraw.Utils

import android.util.Log
import com.divyanshu.androiddraw.BuildConfig

object Logger {
    fun d(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, message)
        }
    }

    fun e(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, message)
        }
    }

    fun i(tag: String, message  : String) {
        if (BuildConfig.DEBUG) {
            Log.i(tag, message)
        }
    }
}