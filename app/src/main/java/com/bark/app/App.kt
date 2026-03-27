package com.bark.app

import android.app.Application
import com.tree.Bark
import timber.log.Timber

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        Bark.init(applicationContext)
        Timber.d("App Initialized!")
    }
}