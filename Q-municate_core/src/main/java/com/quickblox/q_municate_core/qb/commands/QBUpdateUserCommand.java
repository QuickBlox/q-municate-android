package com.quickblox.q_municate_core.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.quickblox.core.exception.QBResponseException;
import com.quickblox.q_municate_core.core.command.ServiceCommand;
import com.quickblox.q_municate_core.qb.helpers.QBAuthHelper;
import com.quickblox.q_municate_core.qb.helpers.QBFriendListHelper;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.SmackException;

import java.io.File;

public class QBUpdateUserCommand extends ServiceCommand {

    private static final String TAG = QBUpdateUserCommand.class.getSimpleName();

    private final QBAuthHelper authHelper;
    private final QBFriendListHelper friendListHelper;

    public QBUpdateUserCommand(Context context, QBAuthHelper authHelper, QBFriendListHelper friendListHelper,
            String successAction, String failAction) {
        super(context, successAction, failAction);
        this.authHelper = authHelper;
        this.friendListHelper = friendListHelper;
    }

    public static void start(Context context, QBUser user, File file) {
        Intent intent = new Intent(QBServiceConsts.UPDATE_USER_ACTION, null, context, QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_USER, user);
        intent.putExtra(QBServiceConsts.EXTRA_FILE, file);
        context.startService(intent);
    }

    @Override
    protected Bundle perform(Bundle extras) throws Exception {
        QBUser user = (QBUser) extras.getSerializable(QBServiceConsts.EXTRA_USER);
        File file = (File) extras.getSerializable(QBServiceConsts.EXTRA_FILE);

        Bundle result = new Bundle();

        QBUser newUser = updateUser(user, file);
        result.putSerializable(QBServiceConsts.EXTRA_USER, newUser);

        return result;
    }

    private boolean isLoggedViaSocial(QBUser user, int authorizationType){
        return hasSocialIdentifier(user) && QBServiceConsts.AUTH_TYPE_LOGIN == authorizationType;
    }

    private boolean hasSocialIdentifier(QBUser user){
        boolean isLoggedViaFB = !TextUtils.isEmpty(user.getFacebookId());
        boolean isLoggedViaTwitter = !TextUtils.isEmpty(user.getTwitterId());
        boolean isLoggedViaTwitterDigits = !TextUtils.isEmpty(user.getTwitterDigitsId());
        return isLoggedViaFB || isLoggedViaTwitter || isLoggedViaTwitterDigits;
    }

    private QBUser updateUser(QBUser user, File file) throws QBResponseException, SmackException.NotConnectedException {
        if (file == null) {
            return authHelper.updateUser(user);
        } else {
            return authHelper.updateUser(user, file);
        }
        // TODO SF temp
        // friendListHelper.sendStatus(status);
    }
}