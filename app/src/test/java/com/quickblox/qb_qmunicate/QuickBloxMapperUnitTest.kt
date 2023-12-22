package com.quickblox.qb_qmunicate

import com.quickblox.qb_qmunicate.data.repository.quickblox.QuickBloxMapper
import com.quickblox.qb_qmunicate.domain.entity.UserEntityImpl
import com.quickblox.users.model.QBUser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.random.Random

class QuickBloxMapperUnitTest {
    @Test
    fun qbUser_mapQBUserToUserEntity_loginAndFullNameAreSame() {
        val qbUser = QBUser().apply {
            id = Random.nextInt()
            login = "login"
            fullName = "fullName"
        }
        val userEntity = QuickBloxMapper.mapQBUserToUserEntity(qbUser)
        assertEquals(qbUser.login, userEntity?.login)
        assertEquals(qbUser.fullName, userEntity?.fullName)
        assertEquals(qbUser.id, userEntity?.id)
    }

    @Test
    fun qbUserIsNull_mapQBUserToUserEntity_userEntityIsNull() {
        val userEntity = QuickBloxMapper.mapQBUserToUserEntity(null)
        assertNull(userEntity)
    }

    @Test
    fun qbUserFullNameIsNull_mapUserDbModelToUserEntity_loginAndFullNameAreSame() {
        val qbUser = QBUser().apply {
            id = Random.nextInt()
            login = "login"
            fullName = null
        }
        val userEntity = QuickBloxMapper.mapQBUserToUserEntity(qbUser)
        assertEquals(qbUser.login, userEntity?.login)
        assertEquals(qbUser.fullName, userEntity?.fullName)
        assertEquals(qbUser.id, userEntity?.id)
    }

    @Test
    fun userEntity_mapUserEntityToQBUser_loginAndFullNameAreSame() {
        val userEntity = UserEntityImpl(
            id = Random.nextInt(),
            login = "login",
            fullName = "fullName",
            avatarFileId = Random.nextInt(),
            avatarFileUrl = "avatar_file_url"
        )
        val qbUser = QuickBloxMapper.mapUserEntityToQBUser(userEntity)
        assertEquals(userEntity.login, qbUser?.login)
        assertEquals(userEntity.fullName, qbUser?.fullName)
        assertEquals(userEntity.id, qbUser?.id)
    }

    @Test
    fun userEntityIsNull_mapUserEntityToQBUser_qbUserIsNull() {
        val qbUser = QuickBloxMapper.mapUserEntityToQBUser(null)
        assertNull(qbUser)
    }

    @Test
    fun userEntityFullNameIsNull_mapUserEntityToQBUser_loginAndFullNameAreSame() {
        val userEntity = UserEntityImpl(
            id = Random.nextInt(),
            login = "login",
            fullName = null,
            avatarFileId = Random.nextInt(),
            avatarFileUrl = "avatar_file_url"
        )
        val qbUser = QuickBloxMapper.mapUserEntityToQBUser(userEntity)
        assertEquals(userEntity.login, qbUser?.login)
        assertEquals(userEntity.fullName, qbUser?.fullName)
        assertEquals(userEntity.id, qbUser?.id)
    }
}