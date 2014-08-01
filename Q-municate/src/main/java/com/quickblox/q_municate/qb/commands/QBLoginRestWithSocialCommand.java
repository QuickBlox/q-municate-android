package com.quickblox.q_municate.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.quickblox.module.users.model.QBUser;
import com.quickblox.q_municate.App;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.core.command.ServiceCommand;
import com.quickblox.q_municate.qb.helpers.QBAuthHelper;
import com.quickblox.q_municate.service.QBService;
import com.quickblox.q_municate.service.QBServiceConsts;
import com.quickblox.q_municate.utils.PrefsHelper;

public class QBLoginRestWithSocialCommand extends ServiceCommand {

    private static final String TAG = QBLoginRestWithSocialCommand.class.getSimpleName();

    private final QBAuthHelper authHelper;

    public QBLoginRestWithSocialCommand(Context context, QBAuthHelper authHelper, String successAction,
            String failAction) {
        super(context, successAction, failAction);
        this.authHelper = authHelper;
    }

    public static void start(Context context, String socialProvier, String accessToken,
            String accessTokenSecret) {
        Intent intent = new Intent(QBServiceConsts.SOCIAL_LOGIN_ACTION, null, context, QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_SOCIAL_PROVIDER, socialProvier);
        intent.putExtra(QBServiceConsts.EXTRA_ACCESS_TOKEN, accessToken);
        intent.putExtra(QBServiceConsts.EXTRA_ACCESS_TOKEN_SECRET, accessTokenSecret);
        context.startService(intent);
    }

    @Override
    public Bundle perform(Bundle extras) throws Exception {
        String socialProvider = (String) extras.getSerializable(QBServiceConsts.EXTRA_SOCIAL_PROVIDER);
        String accessToken = (String) extras.getSerializable(QBServiceConsts.EXTRA_ACCESS_TOKEN);
        String accessTokenSecret = (String) extras.getSerializable(QBServiceConsts.EXTRA_ACCESS_TOKEN_SECRET);
        QBUser user = authHelper.login(socialProvider, accessToken, accessTokenSecret);
        if(TextUtils.isEmpty(user.getWebsite())) {
            App.getInstance().getPrefsHelper().savePref(PrefsHelper.PREF_IMPORT_INITIALIZED, false);
            extras.putSerializable(QBServiceConsts.EXTRA_USER, getUserWithAvatar(user));
        } else {
            App.getInstance().getPrefsHelper().savePref(PrefsHelper.PREF_IMPORT_INITIALIZED, true);
            extras.putSerializable(QBServiceConsts.EXTRA_USER, user);
        }
        return extras;
    }

    private QBUser getUserWithAvatar(QBUser user) {
        QBUser newUser = new QBUser();
        newUser.setId(user.getId());
        newUser.setPassword(user.getPassword());
        newUser.setWebsite(context.getString(R.string.inf_url_to_facebook_avatar, user.getFacebookId()));
        return newUser;
    }
}