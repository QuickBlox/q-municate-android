package com.quickblox.qb_qmunicate.domain.use_case.auth

import com.quickblox.qb_qmunicate.domain.entity.UserEntity
import com.quickblox.qb_qmunicate.domain.exception.RepositoryException
import com.quickblox.qb_qmunicate.domain.repository.AuthRepository
import com.quickblox.qb_qmunicate.domain.repository.UserRepository
import com.quickblox.qb_qmunicate.domain.use_case.base.FlowUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class SessionUpdateUseCase @Inject constructor(
    private val userRepository: UserRepository, private val authRepository: AuthRepository
) : FlowUseCase<Unit, Unit>() {
    override suspend fun execute(args: Unit): Flow<Unit> {
        return channelFlow {
            launch(Dispatchers.Main) {
                authRepository.subscribeSessionExpiredFlow().collect { expired ->
                    val notExpired = !expired
                    if (notExpired) {
                        return@collect
                    }

                    try {
                        val user = signInRemote()
                        if (user != null) {
                            saveLocalLoggedUser(user)
                        } else {
                            send(Unit)
                        }
                    } catch (e: RepositoryException) {
                        send(Unit)
                    }
                }
            }
        }
    }

    private fun signInRemote(): UserEntity? {
        val firebaseProjectId = authRepository.getFirebaseProjectId()
        val firebaseToken = authRepository.getFirebaseToken()

        return userRepository.signInRemoteUser(firebaseProjectId, firebaseToken)
    }

    private fun saveLocalLoggedUser(user: UserEntity) {
        userRepository.deleteLocalUser()
        userRepository.saveLocalUser(user)
    }
}