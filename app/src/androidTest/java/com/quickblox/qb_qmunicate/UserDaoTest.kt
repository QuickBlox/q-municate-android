package com.quickblox.qb_qmunicate

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.quickblox.qb_qmunicate.data.repository.db.Database
import com.quickblox.qb_qmunicate.data.repository.db.UserDao
import com.quickblox.qb_qmunicate.data.repository.db.UserDbModel
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.random.Random

@RunWith(AndroidJUnit4::class)
class UserDaoTest {
    private lateinit var userDao: UserDao
    private lateinit var db: Database

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, Database::class.java
        ).build()
        userDao = db.getUserDao()
    }

    @After
    fun closeDb() {
        userDao.clear()
        db.close()
    }

    @Test
    fun createUser_saveUserAndLoadUser_createdAndLoadedUsersAreSame() {
        val createdUser = createUser()
        userDao.insert(createdUser)

        val loadedUser = userDao.getUser()
        assertEquals(createdUser, loadedUser)
    }

    @Test
    fun createUser_saveUserAndCleanDb_loadUserIsNull() {
        val createdUser = createUser()
        userDao.insert(createdUser)

        userDao.clear()

        val loadedUser = userDao.getUser()
        assertNull(loadedUser)
    }

    @Test
    fun createUser_updateUserAndLoadUser_updatedAndLoadedUsersAreSame() {
        userDao.insert(createUser())
        val loadedUser = userDao.getUser()

        val updatedUser = updateUser(loadedUser)

        userDao.update(
            updatedUser.id,
            updatedUser.login,
            updatedUser.fullName!!,
            updatedUser.avatarFileId!!,
            updatedUser.avatarFileUrl!!
        )

        val loadedUpdatedUser = userDao.getUser()
        assertEquals(updatedUser, loadedUpdatedUser)
    }

    @Test
    fun createUser_deleteUser_loadUserIsNull() {
        val createdUser = createUser()
        userDao.insert(createdUser)

        userDao.delete(createdUser)

        val loadedUser = userDao.getUser()
        assertNull(loadedUser)
    }

    @Test
    fun create2Users_loadUser_loadedCreatedUserWithHighestId() {
        val createdUserA = createUser(100)
        userDao.insert(createdUserA)

        val createdUserB = createUser(101)
        userDao.insert(createdUserB)

        val loadedUser = userDao.getUser()

        assertEquals(createdUserB, loadedUser)
        assertNotEquals(createdUserA, loadedUser)
    }

    private fun createUser(userId: Int = Random.nextInt()): UserDbModel {
        val timeStamp = System.currentTimeMillis()
        return UserDbModel(
            userId,
            "test_login_$timeStamp",
            "test_full_name_$timeStamp",
            Random.nextInt(),
            "test_avatar_url_$timeStamp"
        )
    }

    private fun updateUser(user: UserDbModel): UserDbModel {
        return UserDbModel(
            user.id,
            user.login + "_updated",
            user.fullName + "_updated",
            user.avatarFileId?.plus(Random.nextInt()),
            user.avatarFileUrl + "_updated"
        )
    }
}