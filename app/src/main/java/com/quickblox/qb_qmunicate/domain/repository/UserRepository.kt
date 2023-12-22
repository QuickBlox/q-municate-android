package com.quickblox.qb_qmunicate.domain.repository

import com.quickblox.qb_qmunicate.domain.entity.UserEntity

interface UserRepository {
    fun getLocalUser(): UserEntity?

    fun saveLocalUser(userEntity: UserEntity)

    fun updateLocalUser(userEntity: UserEntity)

    fun deleteLocalUser()

    fun signInRemoteUser(firebaseProjectId: String, firebaseToken: String): UserEntity?

    fun updateRemoteUser(userEntity: UserEntity): UserEntity?

    fun logoutRemoteUser()
}