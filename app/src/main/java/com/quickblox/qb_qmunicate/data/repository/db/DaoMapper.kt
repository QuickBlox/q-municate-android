package com.quickblox.qb_qmunicate.data.repository.db

import com.quickblox.qb_qmunicate.domain.entity.UserEntity
import com.quickblox.qb_qmunicate.domain.entity.UserEntityImpl

object DaoMapper {
    fun mapUserDbModelToUserEntity(userDbModel: UserDbModel?): UserEntity? {
        return userDbModel?.let {
            UserEntityImpl(
                id = userDbModel.id,
                login = it.login,
                fullName = it.fullName,
                avatarFileId = it.avatarFileId,
                avatarFileUrl = it.avatarFileUrl
            )
        }
    }

    fun mapUserEntityToUserDbModel(userEntity: UserEntity?): UserDbModel? {
        return userEntity?.let {
            UserDbModel(
                id = userEntity.id,
                login = it.login,
                fullName = it.fullName,
                avatarFileId = it.avatarFileId,
                avatarFileUrl = it.avatarFileUrl
            )
        }
    }
}