package com.quickblox.qb_qmunicate

import android.app.Application
import com.quickblox.qb_qmunicate.domain.use_case.auth.SessionUpdateUseCase
import com.quickblox.qb_qmunicate.presentation.start.StartActivity
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class App : Application() {
    @Inject
    lateinit var sessionUpdateUseCase: SessionUpdateUseCase

    override fun onCreate() {
        super.onCreate()
        CoroutineScope(Dispatchers.Main).launch {
            sessionUpdateUseCase.execute(Unit).collect {
                StartActivity.show(applicationContext)
            }
        }
    }
}