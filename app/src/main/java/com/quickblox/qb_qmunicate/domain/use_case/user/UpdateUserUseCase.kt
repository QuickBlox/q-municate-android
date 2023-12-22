package com.quickblox.qb_qmunicate.domain.use_case.user

import com.quickblox.qb_qmunicate.domain.entity.UserEntity
import com.quickblox.qb_qmunicate.domain.exception.DOMAIN_UNEXPECTED_EXCEPTION
import com.quickblox.qb_qmunicate.domain.exception.DomainException
import com.quickblox.qb_qmunicate.domain.repository.UserRepository
import com.quickblox.qb_qmunicate.domain.use_case.base.BaseUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UpdateUserUseCase @Inject constructor(private val userRepository: UserRepository) :
    BaseUseCase<UserEntity, UserEntity?>() {

    override suspend fun execute(userEntity: UserEntity): UserEntity? {
        var updatedUser: UserEntity? = null

        withContext(Dispatchers.IO) {
            runCatching {
                val updatedUserInRemote = userRepository.updateRemoteUser(userEntity)

                updatedUserInRemote?.let {
                    userRepository.updateLocalUser(updatedUserInRemote)
                    updatedUser = userRepository.getLocalUser()
                }
            }.onFailure { error ->
                throw DomainException(error.message ?: DOMAIN_UNEXPECTED_EXCEPTION)
            }
        }

        return updatedUser
    }
}