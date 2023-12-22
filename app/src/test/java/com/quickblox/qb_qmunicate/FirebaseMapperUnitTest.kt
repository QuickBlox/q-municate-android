package com.quickblox.qb_qmunicate

import com.quickblox.qb_qmunicate.data.repository.firebase.FirebaseMapper
import com.quickblox.users.model.QBUser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.random.Random

class FirebaseMapperUnitTest {
    @Test
    fun qbUser_mapQBUserToUserEntity_loginAndFullNameAreSame() {
        val qbUser = QBUser().apply {
            id = Random.nextInt()
            login = "login"
            fullName = "fullName"
        }
        val userEntity = FirebaseMapper.mapQBUserToUserEntity(qbUser)
        assertEquals(qbUser.login, userEntity?.login)
        assertEquals(qbUser.fullName, userEntity?.fullName)
        assertEquals(qbUser.id, userEntity?.id)
    }

    @Test
    fun qbUserIsNull_mapQBUserToUserEntity_userEntityIsNull() {
        val userEntity = FirebaseMapper.mapQBUserToUserEntity(null)
        assertNull(userEntity)
    }

    @Test
    fun qbUserFullNameIsNull_mapUserDbModelToUserEntity_loginAndFullNameAreSame() {
        val qbUser = QBUser().apply {
            id = Random.nextInt()
            login = "login"
            fullName = null
        }
        val userEntity = FirebaseMapper.mapQBUserToUserEntity(qbUser)
        assertEquals(qbUser.login, userEntity?.login)
        assertEquals(qbUser.fullName, userEntity?.fullName)
        assertEquals(qbUser.id, userEntity?.id)
    }
}