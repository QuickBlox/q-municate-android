package com.quickblox.qb_qmunicate.data.repository.db

import androidx.room.Database
import androidx.room.RoomDatabase

const val DB_NAME = "q-municate-db"

@Database(entities = [UserDbModel::class], version = 1)
abstract class Database : RoomDatabase() {
    abstract fun getUserDao(): UserDao
}