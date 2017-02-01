package com.quickblox.sample.user;

import com.quickblox.q_municate_user_service.QMUserService;
import com.quickblox.sample.user.activities.UsersListActivity;

import javax.inject.Singleton;

import dagger.Component;

@Component(modules = {AppModule.class})
@Singleton
public interface AppComponent {
    void inject(QMUserService userService);
}