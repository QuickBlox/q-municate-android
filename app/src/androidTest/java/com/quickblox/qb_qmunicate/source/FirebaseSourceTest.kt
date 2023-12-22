package com.quickblox.qb_qmunicate.source

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.quickblox.qb_qmunicate.data.repository.firebase.FirebaseSource
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith


@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class FirebaseSourceTest {
    @Test(expected = IllegalArgumentException::class)
    fun userNotExist_createToken_receivedException() {
        FirebaseSource().getToken()
    }
}