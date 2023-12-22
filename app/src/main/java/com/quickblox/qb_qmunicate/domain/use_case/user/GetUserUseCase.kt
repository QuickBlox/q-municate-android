package com.quickblox.qb_qmunicate.domain.use_case.user

import com.quickblox.qb_qmunicate.domain.entity.UserEntity
import com.quickblox.qb_qmunicate.domain.exception.DOMAIN_UNEXPECTED_EXCEPTION
import com.quickblox.qb_qmunicate.domain.exception.DomainException
import com.quickblox.qb_qmunicate.domain.repository.UserRepository
import com.quickblox.qb_qmunicate.domain.use_case.base.BaseUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetUserUseCase @Inject constructor(private val userRepository: UserRepository) :
    BaseUseCase<Unit, UserEntity?>() {

    override suspend fun execute(args: Unit): UserEntity? {
        var localLoggedUser: UserEntity? = null

        withContext(Dispatchers.IO) {
            runCatching {
                localLoggedUser = userRepository.getLocalUser()
            }.onFailure { error ->
                throw DomainException(error.message ?: DOMAIN_UNEXPECTED_EXCEPTION)
            }
        }

        return localLoggedUser
    }
}