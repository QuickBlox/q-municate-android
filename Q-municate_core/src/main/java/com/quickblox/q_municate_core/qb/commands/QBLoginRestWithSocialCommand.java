package com.quickblox.q_municate_core.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.quickblox.core.exception.BaseServiceException;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.q_municate_core.R;
import com.quickblox.q_municate_core.models.UserCustomData;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_core.utils.PrefsHelper;
import com.quickblox.q_municate_core.utils.Utils;
import com.quickblox.users.model.QBUser;
import com.quickblox.q_municate_core.core.command.ServiceCommand;
import com.quickblox.q_municate_core.qb.helpers.QBAuthHelper;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

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
    public Bundle perform(Bundle extras) throws QBResponseException {
        String socialProvider = (String) extras.getSerializable(QBServiceConsts.EXTRA_SOCIAL_PROVIDER);
        String accessToken = (String) extras.getSerializable(QBServiceConsts.EXTRA_ACCESS_TOKEN);
        String accessTokenSecret = (String) extras.getSerializable(QBServiceConsts.EXTRA_ACCESS_TOKEN_SECRET);

        try {

            QBUser user = authHelper.login(socialProvider, accessToken, accessTokenSecret);
            if (TextUtils.isEmpty(user.getWebsite())) {
                PrefsHelper.getPrefsHelper().savePref(PrefsHelper.PREF_IMPORT_INITIALIZED, false);
                extras.putSerializable(QBServiceConsts.AUTH_ACTION_TYPE, QBServiceConsts.AUTH_TYPE_REGISTRATION);
                extras.putSerializable(QBServiceConsts.EXTRA_USER, getUserWithAvatar(user));
            } else {
                PrefsHelper.getPrefsHelper().savePref(PrefsHelper.PREF_IMPORT_INITIALIZED, true);
                extras.putSerializable(QBServiceConsts.AUTH_ACTION_TYPE, QBServiceConsts.AUTH_TYPE_LOGIN);
                extras.putSerializable(QBServiceConsts.EXTRA_USER, user);
            }
        } catch (BaseServiceException e){
            throw new QBResponseException(e.getLocalizedMessage());
        }
        return extras;
    }

    private QBUser getUserWithAvatar(QBUser user) {
        String avatarUrl = context.getString(R.string.inf_url_to_facebook_avatar, user.getFacebookId());
        QBUser newUser = new QBUser();
        newUser.setId(user.getId());
        newUser.setPassword(user.getPassword());
        newUser.setCustomData(Utils.customDataToString(getUserCustomData(avatarUrl)));

        // TODO temp field
        newUser.setWebsite(avatarUrl);
        // end todo

        return newUser;
    }

    private UserCustomData getUserCustomData(String avatarUrl) {
        String isImport = "1";
        return new UserCustomData(avatarUrl, ConstsCore.EMPTY_STRING, isImport);
    }
}