package com.stackedprogrammer.fitsync

import android.app.Application
import com.facebook.drawee.backends.pipeline.Fresco

class FitSyncApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Fresco.initialize(this)
    }
}