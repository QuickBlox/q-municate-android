package com.quickblox.sample.user;

import com.quickblox.sample.user.activities.UsersListActivity;

import javax.inject.Singleton;

import dagger.Component;

@Component(modules = {AppModule.class})
@Singleton
public interface AppComponent {
    void inject(UsersListActivity usersListActivity);
}