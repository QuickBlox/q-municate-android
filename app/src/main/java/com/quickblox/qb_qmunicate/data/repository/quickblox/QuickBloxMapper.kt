package com.quickblox.qb_qmunicate.data.repository.quickblox

import com.quickblox.qb_qmunicate.domain.entity.UserEntity
import com.quickblox.qb_qmunicate.domain.entity.UserEntityImpl
import com.quickblox.users.model.QBUser

object QuickBloxMapper {
    fun mapQBUserToUserEntity(qbUser: QBUser?): UserEntity? {
        return qbUser?.let {
            UserEntityImpl(
                id = qbUser.id,
                login = it.login,
                fullName = it.fullName,
                avatarFileId = it.fileId,
                avatarFileUrl = null
            )
        }
    }

    fun mapUserEntityToQBUser(userEntity: UserEntity?): QBUser? {
        return userEntity?.let {
            QBUser().apply {
                id = userEntity.id
                login = it.login
                fullName = it.fullName
                fileId = it.avatarFileId
            }
        }
    }
}