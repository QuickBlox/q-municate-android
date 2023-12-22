package com.quickblox.qb_qmunicate.data.dependency

import com.quickblox.qb_qmunicate.data.repository.AuthRepositoryImpl
import com.quickblox.qb_qmunicate.data.repository.UserRepositoryImpl
import com.quickblox.qb_qmunicate.data.repository.db.UserDao
import com.quickblox.qb_qmunicate.data.repository.firebase.FirebaseSource
import com.quickblox.qb_qmunicate.data.repository.quickblox.QuickBloxSource
import com.quickblox.qb_qmunicate.domain.repository.AuthRepository
import com.quickblox.qb_qmunicate.domain.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideUserRepository(userDao: UserDao, quickBloxSource: QuickBloxSource): UserRepository {
        return UserRepositoryImpl(userDao, quickBloxSource)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(firebaseSource: FirebaseSource, quickBloxSource: QuickBloxSource): AuthRepository {
        return AuthRepositoryImpl(firebaseSource, quickBloxSource)
    }
}