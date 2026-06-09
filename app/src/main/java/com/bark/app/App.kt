package com.bark.app

import android.app.Application
import com.tree.Bark
import com.tree.BuildConfig
import timber.log.Timber

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        Bark.init(applicationContext, BuildConfig.DEBUG)
        Timber.d("App Initialized!")
    }
}