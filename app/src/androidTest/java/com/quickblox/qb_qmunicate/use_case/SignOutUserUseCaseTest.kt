package com.quickblox.qb_qmunicate.use_case

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.quickblox.qb_qmunicate.domain.entity.UserEntity
import com.quickblox.qb_qmunicate.domain.entity.UserEntityImpl
import com.quickblox.qb_qmunicate.domain.repository.UserRepository
import com.quickblox.qb_qmunicate.domain.use_case.user.SignOutUserUseCase
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
class SignOutUserUseCaseTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var useCase: SignOutUserUseCase

    @Before
    fun initHilt() {
        hiltRule.inject()
    }

    @After
    fun cleanDb() {
        userRepository.deleteLocalUser()
    }

    @Test
    fun createUser_execute_userDeleted() = runBlocking {
        val createdUser = createUser()
        userRepository.saveLocalUser(createdUser)

        withContext(Dispatchers.Main) {
            useCase.execute(Unit)
        }

        val loadedUser = userRepository.getLocalUser()
        assertNull(loadedUser)
    }

    @Test
    fun userNotExist_execute_userDeleted() = runBlocking {
        withContext(Dispatchers.Main) {
            useCase.execute(Unit)
        }

        val loadedUser = userRepository.getLocalUser()
        assertNull(loadedUser)
    }

    private fun createUser(): UserEntity {
        return UserEntityImpl(
            id = Random.nextInt(),
            login = "test ${System.currentTimeMillis()}",
            fullName = "test${System.currentTimeMillis()}",
            avatarFileId = Random.nextInt(),
            avatarFileUrl = "test${System.currentTimeMillis()}"
        )
    }
}