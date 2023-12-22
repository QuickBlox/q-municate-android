package com.quickblox.qb_qmunicate.domain.entity

interface UserEntity {
    val id: Int
    val login: String
    var fullName: String?
    var avatarFileId: Int?
    var avatarFileUrl: String?

    fun applyEmptyAvatar()
}