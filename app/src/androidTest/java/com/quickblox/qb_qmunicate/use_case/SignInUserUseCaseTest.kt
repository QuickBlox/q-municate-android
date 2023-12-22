package com.quickblox.qb_qmunicate.use_case

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.quickblox.qb_qmunicate.domain.entity.UserEntity
import com.quickblox.qb_qmunicate.domain.entity.UserEntityImpl
import com.quickblox.qb_qmunicate.domain.repository.AuthRepository
import com.quickblox.qb_qmunicate.domain.repository.UserRepository
import com.quickblox.qb_qmunicate.domain.use_case.user.SignInUserUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.random.Random

@RunWith(AndroidJUnit4::class)
class SignInUserUseCaseTest {
    // TODO: need to fix FirebaseSourceTest and add real firebase token and project id like in test FirebaseSourceTest

    @Test
    fun repositoriesHaveData_execute_userCreated() = runBlocking {
        var createdUser: UserEntity?
        withContext(Dispatchers.Main) {
            createdUser = SignInUserUseCase(buildSpyUserRepository(), buildSpyAuthRepository()).execute(Unit)
        }

        assertNotNull(createdUser)
    }

    private fun createUser(): UserEntity {
        return UserEntityImpl(
            id = Random.nextInt(),
            login = "test_login_${System.currentTimeMillis()}",
            fullName = "test_full_name_${System.currentTimeMillis()}",
            avatarFileId = Random.nextInt(),
            avatarFileUrl = "test_avatar_url_${System.currentTimeMillis()}"
        )
    }

    private fun updateUser(user: UserEntity): UserEntity {
        return UserEntityImpl(
            id = user.id,
            login = user.login,
            fullName = user.fullName + "_updated",
            avatarFileId = user.avatarFileId?.plus(Random.nextInt()),
            avatarFileUrl = user.avatarFileUrl + "_updated"
        )
    }

    private fun buildSpyAuthRepository(): AuthRepository {
        // TODO: need to change using Mockito library
        return object : AuthRepository {
            override fun getFirebaseProjectId(): String {
                return "dummy_project_id_${System.currentTimeMillis()}"
            }

            override fun subscribeSessionExpiredFlow(): Flow<Boolean> {
                return MutableStateFlow(false)
            }

            override fun getFirebaseToken(): String {
                return "dummy_token_${System.currentTimeMillis()}"
            }
        }
    }

    private fun buildSpyUserRepository(): UserRepository {
        // TODO: need to change using Mockito library
        return object : UserRepository {
            override fun signInRemoteUser(firebaseProjectId: String, firebaseToken: String): UserEntity? {
                return createUser()
            }

            override fun getLocalUser(): UserEntity? {
                return createUser()
            }

            override fun saveLocalUser(userEntity: UserEntity) {
                // do nothing
            }

            override fun updateLocalUser(userEntity: UserEntity) {
                // do nothing
            }

            override fun deleteLocalUser() {
                // do nothing
            }

            override fun updateRemoteUser(userEntity: UserEntity): UserEntity? {
                return updateUser(userEntity)
            }

            override fun logoutRemoteUser() {
                // do nothing
            }
        }
    }
}