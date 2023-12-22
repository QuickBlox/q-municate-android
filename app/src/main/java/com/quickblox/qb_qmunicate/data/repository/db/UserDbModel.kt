package com.quickblox.qb_qmunicate.data.repository.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "logged_user_table")
data class UserDbModel(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int,

    @ColumnInfo(name = "login")
    val login: String,

    @ColumnInfo(name = "full_name")
    val fullName: String?,

    @ColumnInfo(name = "avatar_file_id")
    val avatarFileId: Int?,

    @ColumnInfo(name = "avatar_file_url")
    val avatarFileUrl: String?
)