package com.quickblox.qb_qmunicate.domain.use_case.user

import com.quickblox.qb_qmunicate.domain.exception.DOMAIN_UNEXPECTED_EXCEPTION
import com.quickblox.qb_qmunicate.domain.exception.DomainException
import com.quickblox.qb_qmunicate.domain.repository.UserRepository
import com.quickblox.qb_qmunicate.domain.use_case.base.BaseUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CheckUserExistUseCase @Inject constructor(private val userRepository: UserRepository) :
    BaseUseCase<Unit, Boolean>() {

    override suspend fun execute(args: Unit): Boolean {
        var isUserExist: Boolean? = null

        withContext(Dispatchers.IO) {
            runCatching {
                val user = userRepository.getLocalUser()
                isUserExist = user != null
            }.onFailure { error ->
                throw DomainException(error.message ?: DOMAIN_UNEXPECTED_EXCEPTION)
            }
        }

        return isUserExist!!
    }
}