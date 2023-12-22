package com.quickblox.qb_qmunicate.domain.use_case.user

import com.quickblox.qb_qmunicate.domain.entity.UserEntity
import com.quickblox.qb_qmunicate.domain.exception.DOMAIN_UNEXPECTED_EXCEPTION
import com.quickblox.qb_qmunicate.domain.exception.DomainException
import com.quickblox.qb_qmunicate.domain.repository.AuthRepository
import com.quickblox.qb_qmunicate.domain.repository.UserRepository
import com.quickblox.qb_qmunicate.domain.use_case.base.BaseUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SignInUserUseCase @Inject constructor(
    private val userRepository: UserRepository, private val authRepository: AuthRepository
) : BaseUseCase<Unit, UserEntity?>() {

    override suspend fun execute(args: Unit): UserEntity? {
        var createdUser: UserEntity? = null

        withContext(Dispatchers.IO) {
            runCatching {
                val firebaseProjectId = authRepository.getFirebaseProjectId()
                val firebaseToken = authRepository.getFirebaseToken()

                createdUser = userRepository.signInRemoteUser(firebaseProjectId, firebaseToken)
                createdUser?.let { user ->
                    userRepository.deleteLocalUser()
                    userRepository.saveLocalUser(user)
                }
            }.onFailure { error ->
                throw DomainException(error.message ?: DOMAIN_UNEXPECTED_EXCEPTION)
            }
        }

        return createdUser
    }
}