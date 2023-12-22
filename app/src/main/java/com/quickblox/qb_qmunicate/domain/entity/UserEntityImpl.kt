package com.quickblox.qb_qmunicate.domain.entity

private const val EMPTY_AVATAR_FILE_ID = -1

class UserEntityImpl(
    override val id: Int,
    override val login: String,
    override var fullName: String?,
    override var avatarFileId: Int?,
    override var avatarFileUrl: String?
) : UserEntity {
    override fun applyEmptyAvatar() {
        avatarFileId = EMPTY_AVATAR_FILE_ID
        avatarFileUrl = null
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other !is UserEntityImpl) {
            return false
        }

        val idAreEqual = id == other.id
        val loginAreEqual = login == other.login
        val fullNameAreEqual = fullName == other.fullName

        return idAreEqual && loginAreEqual && fullNameAreEqual
    }

    override fun hashCode(): Int {
        var result = login.hashCode()
        result = 31 * result + (fullName?.hashCode() ?: 0)
        return result
    }
}