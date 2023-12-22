package com.quickblox.qb_qmunicate.data.repository.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import dagger.hilt.android.AndroidEntryPoint

@Dao
interface UserDao {
    @Query("SELECT * FROM logged_user_table ORDER BY id DESC LIMIT 1")
    fun getUser(): UserDbModel

    @Query("UPDATE logged_user_table SET id=:id, login=:login, full_name=:fullName, avatar_file_id=:avatarFileId, avatar_file_url=:avatarFileUrl WHERE id = :id")
    fun update(id: Int, login: String, fullName: String, avatarFileId: Int, avatarFileUrl: String)

    @Insert
    fun insert(user: UserDbModel)

    @Delete
    fun delete(user: UserDbModel)

    @Query("DELETE FROM logged_user_table")
    fun clear()
}