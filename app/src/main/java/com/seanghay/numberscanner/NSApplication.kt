package com.seanghay.numberscanner

import android.app.Application
import logcat.AndroidLogcatLogger
import logcat.LogPriority

class NSApplication: Application() {
  override fun onCreate() {
    super.onCreate()
    AndroidLogcatLogger.installOnDebuggableApp(this, minPriority = LogPriority.VERBOSE)
  }
}