package com.quickblox.qb_qmunicate.domain.dependency

import com.quickblox.qb_qmunicate.domain.repository.AuthRepository
import com.quickblox.qb_qmunicate.domain.repository.UserRepository
import com.quickblox.qb_qmunicate.domain.use_case.auth.SessionUpdateUseCase
import com.quickblox.qb_qmunicate.domain.use_case.user.CheckUserExistUseCase
import com.quickblox.qb_qmunicate.domain.use_case.user.SignInUserUseCase
import com.quickblox.qb_qmunicate.domain.use_case.user.SignOutUserUseCase
import com.quickblox.qb_qmunicate.domain.use_case.user.GetUserUseCase
import com.quickblox.qb_qmunicate.domain.use_case.user.UpdateUserUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {
    @Provides
    @Singleton
    fun provideCheckLoggedUserExistUseCase(userRepository: UserRepository): CheckUserExistUseCase {
        return CheckUserExistUseCase(userRepository)
    }

    @Provides
    @Singleton
    fun provideCreateLoggedUserUseCase(
        userRepository: UserRepository,
        authRepository: AuthRepository
    ): SignInUserUseCase {
        return SignInUserUseCase(userRepository, authRepository)
    }

    @Provides
    @Singleton
    fun provideDeleteLoggedUserUseCase(userRepository: UserRepository): SignOutUserUseCase {
        return SignOutUserUseCase(userRepository)
    }

    @Provides
    @Singleton
    fun provideGetLoggedUserUseCase(userRepository: UserRepository): GetUserUseCase {
        return GetUserUseCase(userRepository)
    }

    @Provides
    @Singleton
    fun provideUpdateLoggedUserUseCase(userRepository: UserRepository): UpdateUserUseCase {
        return UpdateUserUseCase(userRepository)
    }

    @Provides
    @Singleton
    fun provideSessionUpdateUseCase(
        userRepository: UserRepository, authRepository: AuthRepository
    ): SessionUpdateUseCase {
        return SessionUpdateUseCase(userRepository, authRepository)
    }
}