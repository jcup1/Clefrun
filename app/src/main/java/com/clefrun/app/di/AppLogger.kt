package com.clefrun.app.di

import android.util.Log
import javax.inject.Inject

fun interface AppLogger {
    fun error(tag: String, message: String, throwable: Throwable?)
}

class AndroidAppLogger @Inject constructor() : AppLogger {
    override fun error(tag: String, message: String, throwable: Throwable?) {
        Log.e(tag, message, throwable)
    }
}
