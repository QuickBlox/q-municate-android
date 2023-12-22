package com.quickblox.qb_qmunicate.use_case

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.quickblox.auth.session.QBSettings
import com.quickblox.qb_qmunicate.BuildConfig.QB_ACCOUNT_KEY
import com.quickblox.qb_qmunicate.BuildConfig.QB_APPLICATION_ID
import com.quickblox.qb_qmunicate.BuildConfig.QB_AUTH_KEY
import com.quickblox.qb_qmunicate.BuildConfig.QB_AUTH_SECRET
import com.quickblox.qb_qmunicate.data.repository.quickblox.QuickBloxMapper
import com.quickblox.qb_qmunicate.data.repository.quickblox.QuickBloxSource
import com.quickblox.qb_qmunicate.domain.entity.UserEntity
import com.quickblox.qb_qmunicate.domain.entity.UserEntityImpl
import com.quickblox.qb_qmunicate.domain.exception.DomainException
import com.quickblox.qb_qmunicate.domain.repository.UserRepository
import com.quickblox.qb_qmunicate.domain.use_case.user.UpdateUserUseCase
import com.quickblox.users.QBUsers
import com.quickblox.users.model.QBUser
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject
import kotlin.random.Random

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class UpdateUserUseCaseTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var quickBloxSource: QuickBloxSource

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var useCase: UpdateUserUseCase

    @Before
    fun init() {
        hiltRule.inject()
        initQuickBloxSDK()
    }

    @After
    fun release() {
        userRepository.deleteLocalUser()
        quickBloxSource.clearSession()
    }

    private fun initQuickBloxSDK() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        QBSettings.getInstance().init(context, QB_APPLICATION_ID, QB_AUTH_KEY, QB_AUTH_SECRET)
        QBSettings.getInstance().accountKey = QB_ACCOUNT_KEY
    }

    @Test
    fun createAndLoginUser_execute_updatedUserExist() = runBlocking {
        val loggedQBUser = loginQBUser(buildQBUser())
        val loggedUserEntity = QuickBloxMapper.mapQBUserToUserEntity(loggedQBUser)
        userRepository.saveLocalUser(loggedUserEntity!!)

        val userWithUpdatedFullName = changedUserFullName(loggedUserEntity)

        var updatedUser: UserEntity?
        withContext(Dispatchers.Main) {
            updatedUser = useCase.execute(userWithUpdatedFullName)
        }

        assertEquals(userWithUpdatedFullName, updatedUser)
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

    @Test(expected = DomainException::class)
    fun userNotExist_execute_updatedUserExist(): Unit = runBlocking {
        val updatedUser = changedUserFullName(createUser())
        useCase.execute(updatedUser)
    }

    private fun createUser(): UserEntity {
        return UserEntityImpl(
            id = Random.nextInt(),
            login = "test ${System.currentTimeMillis()}",
            fullName = "test${System.currentTimeMillis()}",
            avatarFileId = Random.nextInt(),
            avatarFileUrl = "avatar_file_url"
        )
    }

    private fun changedUserFullName(user: UserEntity): UserEntity {
        return UserEntityImpl(
            id = user.id,
            login = user.login,
            fullName = "updated_fullName_${System.currentTimeMillis()}",
            avatarFileId = user.avatarFileId,
            avatarFileUrl = user.avatarFileUrl
        )
    }
}