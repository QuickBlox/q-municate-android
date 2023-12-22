package com.quickblox.qb_qmunicate.source

import android.app.Activity
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import com.quickblox.auth.session.QBSessionManager
import com.quickblox.auth.session.QBSettings
import com.quickblox.qb_qmunicate.BuildConfig.FIREBASE_APP_ID
import com.quickblox.qb_qmunicate.BuildConfig.QB_ACCOUNT_KEY
import com.quickblox.qb_qmunicate.BuildConfig.QB_APPLICATION_ID
import com.quickblox.qb_qmunicate.BuildConfig.QB_AUTH_KEY
import com.quickblox.qb_qmunicate.BuildConfig.QB_AUTH_SECRET
import com.quickblox.qb_qmunicate.data.repository.firebase.FirebaseSource
import com.quickblox.qb_qmunicate.data.repository.quickblox.QuickBloxSource
import com.quickblox.qb_qmunicate.presentation.profile.create.CreateProfileActivity
import com.quickblox.users.QBUsers
import com.quickblox.users.model.QBUser
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class QuickBloxSourceTest {
    private val quickBloxSource: QuickBloxSource =
        QuickBloxSource(InstrumentationRegistry.getInstrumentation().targetContext)

    @Before
    fun init() {

        initQuickBloxSDK()
    }

    @After
    fun release() {
        quickBloxSource.clearSession()
    }

    private fun initQuickBloxSDK() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        QBSettings.getInstance().init(context, QB_APPLICATION_ID, QB_AUTH_KEY, QB_AUTH_SECRET)
        QBSettings.getInstance().accountKey = QB_ACCOUNT_KEY
    }

    @Test
    fun userExist_logout_noErrors() = runBlocking {
        loginQBUser(buildQBUser())
        quickBloxSource.signOut()

        assertFalse(QBSessionManager.getInstance().isValidActiveSession)
        assertNull(QBSessionManager.getInstance().activeSession)
    }

    private fun buildQBUser(): QBUser {
        return QBUser().apply {
            login = "qwe11"
            password = "quickblox"
        }
    }

    private fun loginQBUser(qbUser: QBUser): QBUser = runBlocking {
        withContext(Dispatchers.IO) {
            QBUsers.signIn(qbUser).perform()
        }
    }

    @Test
    fun userNotExist_logout_noErrors() {
        quickBloxSource.signOut()

        assertFalse(QBSessionManager.getInstance().isValidActiveSession)
        assertNull(QBSessionManager.getInstance().activeSession)
    }

    @Test
    fun userExist_updateUser_userIsUpdated() = runBlocking {
        val loggedUser = loginQBUser(buildQBUser())
        loggedUser.fullName = "updated_full_name_${System.currentTimeMillis()}"

        val updatedUser = quickBloxSource.updateUser(loggedUser)

        assertEquals(loggedUser.fullName, updatedUser.fullName)
    }

    @Test(expected = IllegalArgumentException::class)
    fun userNotExist_createToken_receivedException() {
        FirebaseSource().getToken()
    }

    @Test
    @Ignore("Need to fix. After sign in by phone, the firebase user is not exist")
    fun signInByFirebasePhone_signIn_u(): Unit = runBlocking {
        val phoneSignInCountDown = CountDownLatch(1)

        var firebaseToken = ""
        val verificationCallback = object : OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                println(credential)

                firebaseToken = credential.zzc() ?: ""
                assertTrue(firebaseToken.isNotEmpty())

                phoneSignInCountDown.countDown()
            }

            override fun onVerificationFailed(exception: FirebaseException) {
                println(exception)
            }
        }

        signInByPhone(verificationCallback)

        phoneSignInCountDown.await(20, TimeUnit.SECONDS)
        assertEquals(0, phoneSignInCountDown.count)

        val qbUser = quickBloxSource.signIn(FIREBASE_APP_ID, firebaseToken)
        assertNotNull(qbUser)
    }

    private fun signInByPhone(verificationCallback: OnVerificationStateChangedCallbacks) {
        ActivityScenario.launch(CreateProfileActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val firebaseAuth = FirebaseAuth.getInstance()
                val firebaseAuthSettings = firebaseAuth.firebaseAuthSettings

                val phoneNumber = "+380507777777"
                val smsCode = "111111"
                firebaseAuthSettings.setAutoRetrievedSmsCodeForPhoneNumber(phoneNumber, smsCode)

                val options = buildPhoneAuthOptions(phoneNumber, firebaseAuth, activity, verificationCallback)
                PhoneAuthProvider.verifyPhoneNumber(options)
            }
        }
    }

    private fun buildPhoneAuthOptions(
        phoneNumber: String,
        firebaseAuth: FirebaseAuth,
        activity: Activity,
        callback: OnVerificationStateChangedCallbacks,
    ): PhoneAuthOptions {
        return PhoneAuthOptions.newBuilder(firebaseAuth).setPhoneNumber(phoneNumber).setTimeout(20, TimeUnit.SECONDS)
            .setActivity(activity).setCallbacks(callback).build()
    }
}