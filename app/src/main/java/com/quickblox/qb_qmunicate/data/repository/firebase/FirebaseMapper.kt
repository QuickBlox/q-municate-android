package com.quickblox.qb_qmunicate.data.repository.firebase

import com.quickblox.qb_qmunicate.domain.entity.UserEntity
import com.quickblox.qb_qmunicate.domain.entity.UserEntityImpl
import com.quickblox.users.model.QBUser

object FirebaseMapper {
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
}