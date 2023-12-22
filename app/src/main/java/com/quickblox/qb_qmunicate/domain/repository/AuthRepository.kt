package com.quickblox.qb_qmunicate.domain.repository

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun subscribeSessionExpiredFlow(): Flow<Boolean>

    fun getFirebaseToken(): String

    fun getFirebaseProjectId(): String
}