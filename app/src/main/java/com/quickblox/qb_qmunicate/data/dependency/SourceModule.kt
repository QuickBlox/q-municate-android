package com.quickblox.qb_qmunicate.data.dependency

import android.content.Context
import com.quickblox.qb_qmunicate.data.repository.firebase.FirebaseSource
import com.quickblox.qb_qmunicate.data.repository.quickblox.QuickBloxSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SourceModule {
    @Provides
    @Singleton
    fun provideFirebaseSource(): FirebaseSource {
        return FirebaseSource()
    }

    @Provides
    @Singleton
    fun provideQuickBloxSource(@ApplicationContext applicationContext: Context): QuickBloxSource {
        return QuickBloxSource(applicationContext)
    }
}