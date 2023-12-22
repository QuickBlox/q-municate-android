package com.quickblox.qb_qmunicate.data.repository

import com.quickblox.qb_qmunicate.data.repository.firebase.FirebaseSource
import com.quickblox.qb_qmunicate.data.repository.quickblox.QuickBloxSource
import com.quickblox.qb_qmunicate.domain.exception.RepositoryException
import com.quickblox.qb_qmunicate.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseSource: FirebaseSource, private val quickBloxSource: QuickBloxSource
) : AuthRepository {
    override fun subscribeSessionExpiredFlow(): Flow<Boolean> {
        return quickBloxSource.subscribeSessionExpiredFlow()
    }

    override fun getFirebaseToken(): String {
        try {
            return firebaseSource.getToken()
        } catch (e: Exception) {
            throw RepositoryException(e.message ?: RepositoryException.Types.UNEXPECTED.name)
        }
    }

    override fun getFirebaseProjectId(): String {
        try {
            return firebaseSource.getProjectId()
        } catch (e: Exception) {
            throw RepositoryException(e.message ?: RepositoryException.Types.UNEXPECTED.name)
        }
    }
}