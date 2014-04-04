package com.quickblox.qmunicate.qb;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.core.command.ServiceCommand;
import com.quickblox.qmunicate.service.QBAuthHelper;
import com.quickblox.qmunicate.service.QBChatHelper;
import com.quickblox.qmunicate.service.QBService;
import com.quickblox.qmunicate.service.QBServiceConsts;

public class QBSocialLoginCommand extends ServiceCommand {

    private static final String TAG = QBSocialLoginCommand.class.getSimpleName();

    private final QBAuthHelper qbAuthHelper;
    private final QBChatHelper qbChatHelper;

    public QBSocialLoginCommand(Context context, QBAuthHelper qbAuthHelper, QBChatHelper qbChatHelper,
            String successAction, String failAction) {
        super(context, successAction, failAction);
        this.qbAuthHelper = qbAuthHelper;
        this.qbChatHelper = qbChatHelper;
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

        QBUser user = qbAuthHelper.login(socialProvider, accessToken, accessTokenSecret);
        qbChatHelper.init(context);

        Bundle result = new Bundle();
        result.putSerializable(QBServiceConsts.EXTRA_USER, user);

        return result;
    }
}