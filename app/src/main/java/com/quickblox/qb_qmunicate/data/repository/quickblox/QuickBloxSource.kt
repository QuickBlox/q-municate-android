package com.quickblox.qb_qmunicate.data.repository.quickblox

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.quickblox.auth.model.QBProvider
import com.quickblox.auth.session.QBSessionListenerImpl
import com.quickblox.auth.session.QBSessionManager
import com.quickblox.auth.session.QBSettings
import com.quickblox.chat.QBChatService
import com.quickblox.content.QBContent
import com.quickblox.content.model.QBFile
import com.quickblox.core.ServiceZone
import com.quickblox.qb_qmunicate.BuildConfig
import com.quickblox.users.QBUsers
import com.quickblox.users.model.QBUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val UNEXPECTED_ERROR_MESSAGE = "Unexpected error: "

class QuickBloxSource @Inject constructor(private val context: Context) {
    private val sessionExpiredFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)

    init {
        QBSessionManager.getInstance().addListener(object : QBSessionListenerImpl() {
            override fun onProviderSessionExpired(provider: String?) {
                if (provider == QBProvider.FIREBASE_PHONE) {
                    CoroutineScope(Dispatchers.Main).launch {
                        sessionExpiredFlow.emit(true)
                    }
                }
            }
        })
    }

    fun initSDK() {
        QBSettings.getInstance().init(
            context, BuildConfig.QB_APPLICATION_ID, BuildConfig.QB_AUTH_KEY, BuildConfig.QB_AUTH_SECRET
        )

        QBSettings.getInstance().accountKey = BuildConfig.QB_ACCOUNT_KEY

        if (isApiAndChatPointAvailable()) {
            QBSettings.getInstance()
                .setEndpoints(BuildConfig.QB_API_DOMAIN, BuildConfig.QB_CHAT_DOMAIN, ServiceZone.PRODUCTION);
        }

        initChatSettings()
    }

    private fun isApiAndChatPointAvailable(): Boolean {
        return BuildConfig.QB_API_DOMAIN.isNotEmpty() && BuildConfig.QB_CHAT_DOMAIN.isNotEmpty()
    }

    private fun initChatSettings() {
        val configurationBuilder = QBChatService.ConfigurationBuilder().apply {
            socketTimeout = 360
        }
        QBChatService.setConfigurationBuilder(configurationBuilder)

        QBChatService.setDefaultPacketReplyTimeout(10000)
        QBChatService.getInstance().setUseStreamManagement(true)
    }

    fun subscribeSessionExpiredFlow() = sessionExpiredFlow

    fun signIn(firebaseProjectId: String, firebaseToken: String): QBUser {
        clearSession()

        try {
            val user = QBUsers.signInUsingFirebase(firebaseProjectId, firebaseToken).perform()
            QBSessionManager.getInstance().activeSession.userId = user.id
            return user
        } catch (e: Exception) {
            throw IllegalArgumentException(e.message ?: (UNEXPECTED_ERROR_MESSAGE + "sign in user in QuickBlox"))
        }
    }

    fun signOut() {
        try {
            QBUsers.signOut().perform()
            clearSession()
        } catch (e: Exception) {
            throw IllegalArgumentException(e.message ?: (UNEXPECTED_ERROR_MESSAGE + "sign out user in QuickBlox"))
        }
    }

    fun updateUser(user: QBUser): QBUser {
        try {
            return QBUsers.updateUser(user).perform()
        } catch (e: Exception) {
            throw IllegalArgumentException(e.message ?: (UNEXPECTED_ERROR_MESSAGE + "sign out user in QuickBlox"))
        }
    }

    @VisibleForTesting
    fun clearSession() {
        QBSessionManager.getInstance().deleteActiveSession()
        QBSessionManager.getInstance().deleteSessionParameters()

        QBSessionManager.getInstance().isManuallyCreated = false
        QBSettings.getInstance().isAutoCreateSession = true
    }

    fun buildFileUrlFrom(fileId: Int?): String? {
        if (fileId == null) {
            return null
        }

        try {
            val file = QBContent.getFile(fileId).perform()
            return QBFile.getPrivateUrlForUID(file.uid)
        } catch (e: Exception) {
            return null
        }
    }
}