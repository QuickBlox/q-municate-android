package com.quickblox.qb_qmunicate.use_case

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.quickblox.auth.model.QBProvider
import com.quickblox.auth.session.QBSessionManager
import com.quickblox.qb_qmunicate.domain.use_case.auth.SessionUpdateUseCase
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class SessionUpdateUseCaseTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var useCase: SessionUpdateUseCase

    @Before
    fun initHilt() {
        hiltRule.inject()
    }

    @Test
    fun sessionExist_execute_sessionUpdateNotified() = runBlocking {
        val sessionUpdateCountDown = CountDownLatch(1)

        val sessionUseCaseJob = launch(Dispatchers.Main) {
            useCase.execute(Unit).collect {
                sessionUpdateCountDown.countDown()
            }
        }

        notifySessionExpired()

        sessionUpdateCountDown.await(10, TimeUnit.SECONDS)

        sessionUseCaseJob.cancel()

        assertEquals(0, sessionUpdateCountDown.count)
    }

    private fun notifySessionExpired() {
        QBSessionManager.getInstance().listeners.forEach { listener ->
            listener.onProviderSessionExpired(QBProvider.FIREBASE_PHONE)
        }
    }
}