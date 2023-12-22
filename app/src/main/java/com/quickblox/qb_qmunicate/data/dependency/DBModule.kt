package com.quickblox.qb_qmunicate.data.dependency

import android.content.Context
import androidx.room.Room
import com.quickblox.qb_qmunicate.data.repository.db.DB_NAME
import com.quickblox.qb_qmunicate.data.repository.db.Database
import com.quickblox.qb_qmunicate.data.repository.db.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DBModule {

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext applicationContext: Context): Database {
        return Room.databaseBuilder(applicationContext, Database::class.java, DB_NAME).build()
    }

    @Provides
    fun provideUserDao(database: Database): UserDao {
        return database.getUserDao()
    }
}