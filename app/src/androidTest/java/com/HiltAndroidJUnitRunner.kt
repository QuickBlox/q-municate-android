package com

import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication
import android.app.Application as Application1

class HiltAndroidJUnitRunner : AndroidJUnitRunner() {
    override fun newApplication(cl: ClassLoader?, name: String?, context: Context?): Application1 {
        return super.newApplication(cl, HiltTestApplication::class.java.name, context)
    }
}