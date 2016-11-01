package com.quickblox.sample.user;

import android.content.Context;

import com.quickblox.q_municate_user_cache.QMUserCacheImpl;
import com.quickblox.q_municate_user_service.QMUserService;
import com.quickblox.q_municate_user_service.cache.QMUserCache;
import com.quickblox.q_municate_user_service.cache.QMUserMemoryCache;

import org.jetbrains.annotations.NotNull;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {

    private Context appContext;

    public AppModule(@NotNull Context context){
        appContext = context;
    }

    @Provides
    @NotNull
    @Singleton
    Context provideContext(){
        return appContext;
    }


    @Provides
    @NotNull
    @Singleton
    QMUserCache provideUserCache(@NotNull Context context){
        return new QMUserCacheImpl(context);
    }

}
