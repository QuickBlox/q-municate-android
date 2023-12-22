package com.quickblox.qb_qmunicate

import com.quickblox.qb_qmunicate.data.repository.db.DaoMapper
import com.quickblox.qb_qmunicate.data.repository.db.UserDbModel
import com.quickblox.qb_qmunicate.domain.entity.UserEntityImpl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.random.Random

class DaoMapperUnitTest {
    @Test
    fun userDBModel_mapUserDbModelToUserEntity_loginAndFullNameAreSame() {
        val userDbModel = UserDbModel(
            id = Random.nextInt(),
            login = "login",
            fullName = "fullName",
            avatarFileId = Random.nextInt(),
            avatarFileUrl = "avatar_file_url"
        )
        val userEntity = DaoMapper.mapUserDbModelToUserEntity(userDbModel)
        assertEquals(userDbModel.login, userEntity?.login)
        assertEquals(userDbModel.fullName, userEntity?.fullName)
        assertEquals(userDbModel.id, userEntity?.id)
    }

    @Test
    fun userDBModelIsNull_mapUserDbModelToUserEntity_userEntityIsNull() {
        val userEntity = DaoMapper.mapUserDbModelToUserEntity(null)
        assertNull(userEntity)
    }

    @Test
    fun userDBModelFullNameIsNull_mapUserDbModelToUserEntity_loginAndFullNameAreSame() {
        val userDbModel = UserDbModel(
            id = Random.nextInt(),
            login = "login",
            fullName = null,
            avatarFileId = Random.nextInt(),
            avatarFileUrl = "avatar_file_url"
        )
        val userEntity = DaoMapper.mapUserDbModelToUserEntity(userDbModel)
        assertEquals(userDbModel.login, userEntity?.login)
        assertEquals(userDbModel.fullName, userEntity?.fullName)
        assertEquals(userDbModel.id, userEntity?.id)
    }

    @Test
    fun userEntity_mapUserEntityToUserDbModel_loginAndFullNameAreSame() {
        val userEntity = UserEntityImpl(
            id = Random.nextInt(),
            login = "login",
            fullName = "fullName",
            avatarFileId = Random.nextInt(),
            avatarFileUrl = "avatar_file_url"
        )
        val userDbModel = DaoMapper.mapUserEntityToUserDbModel(userEntity)
        assertEquals(userEntity.login, userDbModel?.login)
        assertEquals(userEntity.fullName, userDbModel?.fullName)
        assertEquals(userEntity.id, userDbModel?.id)
    }

    @Test
    fun userEntityIsNull_mapUserDbModelToUserEntity_userDbModelIsNull() {
        val userDbModel = DaoMapper.mapUserEntityToUserDbModel(null)
        assertNull(userDbModel)
    }

    @Test
    fun userEntityFullNameIsNull_mapUserDbModelToUserEntity_loginAndFullNameAreSame() {
        val userEntity = UserEntityImpl(
            id = Random.nextInt(),
            login = "login",
            fullName = null,
            avatarFileId = Random.nextInt(),
            avatarFileUrl = "avatar_file_url"
        )
        val userDbModel = DaoMapper.mapUserEntityToUserDbModel(userEntity)
        assertEquals(userEntity.login, userDbModel?.login)
        assertEquals(userEntity.fullName, userDbModel?.fullName)
        assertEquals(userEntity.id, userDbModel?.id)
    }
}