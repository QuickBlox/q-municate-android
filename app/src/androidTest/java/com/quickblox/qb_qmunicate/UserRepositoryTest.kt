package com.quickblox.qb_qmunicate

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.quickblox.qb_qmunicate.domain.entity.UserEntity
import com.quickblox.qb_qmunicate.domain.entity.UserEntityImpl
import com.quickblox.qb_qmunicate.domain.repository.UserRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
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
class UserRepositoryTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var userRepository: UserRepository

    @Before
    fun initHilt() {
        hiltRule.inject()
    }

    @After
    fun cleanDb() {
        userRepository.deleteLocalUser()
    }

    @Test
    fun createUser_saveUserAndLoadUser_createdAndLoadedUsersAreSame() {
        val createdUser = createUser()
        userRepository.saveLocalUser(createdUser)

        val loadedUser = userRepository.getLocalUser()
        assertEquals(createdUser, loadedUser)
    }

    @Test
    fun createUser_updateUser_createdAndUpdatedAreNotSame() {
        val createdUser = createUser()
        userRepository.saveLocalUser(createdUser)

        userRepository.updateLocalUser(updateUser(createdUser))

        val loadedUser = userRepository.getLocalUser()
        assertNotEquals(createdUser, loadedUser)
    }

    @Test
    fun createUser_deleteUser_loadedUserIsNull() {
        userRepository.saveLocalUser(createUser())
        userRepository.deleteLocalUser()

        val loadedUser = userRepository.getLocalUser()
        assertNull(loadedUser)
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

    private fun updateUser(user: UserEntity): UserEntity {
        return UserEntityImpl(
            id = user.id,
            login = user.login,
            fullName = "test${System.currentTimeMillis()}",
            avatarFileId = user.avatarFileId?.plus(Random.nextInt()),
            avatarFileUrl = "avatar_file_url"
        )
    }
}