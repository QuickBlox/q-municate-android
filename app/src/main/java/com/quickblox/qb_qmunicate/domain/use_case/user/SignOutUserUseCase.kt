package com.quickblox.qb_qmunicate.domain.use_case.user

import com.quickblox.qb_qmunicate.domain.exception.DOMAIN_UNEXPECTED_EXCEPTION
import com.quickblox.qb_qmunicate.domain.exception.DomainException
import com.quickblox.qb_qmunicate.domain.repository.UserRepository
import com.quickblox.qb_qmunicate.domain.use_case.base.BaseUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SignOutUserUseCase @Inject constructor(private val userRepository: UserRepository) : BaseUseCase<Unit, Unit>() {
    override suspend fun execute(args: Unit) {
        withContext(Dispatchers.IO) {
            runCatching {
                userRepository.logoutRemoteUser()
                userRepository.deleteLocalUser()
            }.onFailure { error ->
                throw DomainException(error.message ?: DOMAIN_UNEXPECTED_EXCEPTION)
            }
        }
    }
}