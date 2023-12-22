package com.quickblox.qb_qmunicate.data.repository

import com.quickblox.qb_qmunicate.data.repository.db.DaoMapper
import com.quickblox.qb_qmunicate.data.repository.db.UserDao
import com.quickblox.qb_qmunicate.data.repository.firebase.FirebaseMapper
import com.quickblox.qb_qmunicate.data.repository.quickblox.QuickBloxMapper
import com.quickblox.qb_qmunicate.data.repository.quickblox.QuickBloxSource
import com.quickblox.qb_qmunicate.domain.entity.UserEntity
import com.quickblox.qb_qmunicate.domain.exception.RepositoryException
import com.quickblox.qb_qmunicate.domain.repository.UserRepository
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao, private val quickBloxSource: QuickBloxSource
) : UserRepository {
    init {
        quickBloxSource.initSDK()
    }

    override fun signInRemoteUser(firebaseProjectId: String, firebaseToken: String): UserEntity? {
        try {
            val qbUser = quickBloxSource.signIn(firebaseProjectId, firebaseToken)
            val userEntity = FirebaseMapper.mapQBUserToUserEntity(qbUser)
            userEntity?.avatarFileUrl = buildAvatarUrlFrom(userEntity?.avatarFileId)
            return userEntity
        } catch (e: Exception) {
            throw RepositoryException(e.message ?: RepositoryException.Types.UNEXPECTED.name)
        }
    }

    override fun getLocalUser(): UserEntity? {
        val userDbModel = userDao.getUser()
        val userEntity = DaoMapper.mapUserDbModelToUserEntity(userDbModel)
        return userEntity
    }

    override fun saveLocalUser(userEntity: UserEntity) {
        val userDbModel = DaoMapper.mapUserEntityToUserDbModel(userEntity)
        userDbModel?.let {
            userDao.insert(it)
        }
    }

    override fun updateLocalUser(userEntity: UserEntity) {
        val userDbModel = DaoMapper.mapUserEntityToUserDbModel(userEntity)
        userDbModel?.let {
            userDao.update(it.id, it.login, it.fullName ?: "", it.avatarFileId ?: -1, it.avatarFileUrl ?: "")
        }
    }

    override fun deleteLocalUser() {
        userDao.clear()
    }

    override fun updateRemoteUser(userEntity: UserEntity): UserEntity? {
        val qbUser = QuickBloxMapper.mapUserEntityToQBUser(userEntity)
        if (qbUser == null) {
            throw RepositoryException("The mapping of the user entity to the QBUser is null")
        }

        try {
            val updatedQBUser = quickBloxSource.updateUser(qbUser)
            val updatedUserEntity = QuickBloxMapper.mapQBUserToUserEntity(updatedQBUser)
            updatedUserEntity?.avatarFileUrl = buildAvatarUrlFrom(updatedUserEntity?.avatarFileId)
            return updatedUserEntity
        } catch (e: Exception) {
            throw RepositoryException(e.message ?: RepositoryException.Types.UNEXPECTED.name)
        }
    }

    private fun buildAvatarUrlFrom(avatarFileId: Int?): String? {
        val fileIdExist = avatarFileId != null && avatarFileId > 0
        if (fileIdExist) {
            val avatarUrl = quickBloxSource.buildFileUrlFrom(avatarFileId)
            return avatarUrl
        }
        return ""
    }

    override fun logoutRemoteUser() {
        try {
            quickBloxSource.signOut()
        } catch (e: Exception) {
            throw RepositoryException(e.message ?: RepositoryException.Types.UNEXPECTED.name)
        }
    }
}