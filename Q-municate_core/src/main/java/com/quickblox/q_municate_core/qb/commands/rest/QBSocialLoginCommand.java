package com.quickblox.q_municate_core.qb.commands.rest;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.quickblox.auth.model.QBProvider;
import com.quickblox.q_municate_core.R;
import com.quickblox.q_municate_core.core.command.ServiceCommand;
import com.quickblox.q_municate_core.models.UserCustomData;
import com.quickblox.q_municate_core.qb.helpers.QBAuthHelper;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_core.utils.helpers.CoreSharedHelper;
import com.quickblox.q_municate_core.utils.Utils;
import com.quickblox.users.model.QBUser;

public class QBSocialLoginCommand extends ServiceCommand {

    private static final String TAG = QBSocialLoginCommand.class.getSimpleName();

    private final QBAuthHelper authHelper;

    public QBSocialLoginCommand(Context context, QBAuthHelper authHelper, String successAction,
            String failAction) {
        super(context, successAction, failAction);
        this.authHelper = authHelper;
    }

    public static void start(Context context, String socialProvider, String accessToken,
            String accessTokenSecret) {
        Intent intent = new Intent(QBServiceConsts.SOCIAL_LOGIN_ACTION, null, context, QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_SOCIAL_PROVIDER, socialProvider);
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
        UserCustomData userCustomData = Utils.customDataToObject(user.getCustomData());
        if (QBProvider.FACEBOOK.equals(socialProvider) && TextUtils.isEmpty(userCustomData.getAvatarUrl())) {
            //Actions for first login via Facebook
            CoreSharedHelper.getInstance().saveUsersImportInitialized(false);
            extras.putSerializable(QBServiceConsts.AUTH_ACTION_TYPE, QBServiceConsts.AUTH_TYPE_REGISTRATION);
            extras.putSerializable(QBServiceConsts.EXTRA_USER, getFBUserWithAvatar(user));
        } else if (QBProvider.TWITTER_DIGITS.equals(socialProvider) && TextUtils.isEmpty(user.getFullName())) {
            //Actions for first login via Twitter Digits
            CoreSharedHelper.getInstance().saveUsersImportInitialized(false);
            extras.putSerializable(QBServiceConsts.AUTH_ACTION_TYPE, QBServiceConsts.AUTH_TYPE_REGISTRATION);
            extras.putSerializable(QBServiceConsts.EXTRA_USER, getTDUserWithFullName(user));
        } else {
            CoreSharedHelper.getInstance().saveUsersImportInitialized(true);
            extras.putSerializable(QBServiceConsts.AUTH_ACTION_TYPE, QBServiceConsts.AUTH_TYPE_LOGIN);
            extras.putSerializable(QBServiceConsts.EXTRA_USER, user);
        }
        return extras;
    }

    private QBUser getFBUserWithAvatar(QBUser user) {
        String avatarUrl = context.getString(R.string.url_to_facebook_avatar, user.getFacebookId());
        user.setCustomData(Utils.customDataToString(getUserCustomData(avatarUrl)));
        return user;
    }

    private QBUser getTDUserWithFullName(QBUser user){
        user.setFullName(user.getPhone());
        user.setCustomData(Utils.customDataToString(getUserCustomData(ConstsCore.EMPTY_STRING)));
        return user;
    }

    private UserCustomData getUserCustomData(String avatarUrl) {
        String isImport = "1"; // TODO: temp, first FB or TD login (for correct work need use crossplatform)
        return new UserCustomData(avatarUrl, ConstsCore.EMPTY_STRING, isImport);
    }
}