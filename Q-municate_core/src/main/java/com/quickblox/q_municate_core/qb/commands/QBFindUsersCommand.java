package com.quickblox.q_municate_core.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.q_municate_core.models.User;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_core.utils.FriendUtils;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
import com.quickblox.q_municate_core.core.command.ServiceCommand;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class QBFindUsersCommand extends ServiceCommand {

    public QBFindUsersCommand(Context context, String successAction, String failAction) {
        super(context, successAction, failAction);
    }

    public static void start(Context context, QBUser currentUser, String constraint) {
        Intent intent = new Intent(QBServiceConsts.LOAD_USERS_ACTION, null, context, QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_USER, currentUser);
        intent.putExtra(QBServiceConsts.EXTRA_CONSTRAINT, constraint);
        context.startService(intent);
    }

    @Override
    public Bundle perform(Bundle extras) throws Exception {
        String constraint = (String) extras.getSerializable(QBServiceConsts.EXTRA_CONSTRAINT);
        QBUser currentUser = (QBUser) extras.getSerializable(QBServiceConsts.EXTRA_USER);

        QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder();
        requestBuilder.setPage(ConstsCore.FL_FRIENDS_PAGE_NUM);
        requestBuilder.setPerPage(ConstsCore.FL_FRIENDS_PER_PAGE);

        Bundle requestParams = new Bundle();
        List<QBUser> userList = QBUsers.getUsersByFullName(constraint, requestBuilder, requestParams);
        Collections.sort(userList, new UserComparator());
        List<User> usersList = FriendUtils.createUsersList(userList);
        usersList.remove(FriendUtils.createUser(currentUser));

        Bundle params = new Bundle();
        params.putString(QBServiceConsts.EXTRA_CONSTRAINT, constraint);
        params.putSerializable(QBServiceConsts.EXTRA_USERS, (java.io.Serializable) usersList);

        return params;
    }

    private class UserComparator implements Comparator<QBUser> {

        @Override
        public int compare(QBUser first, QBUser second) {
            if (first.getFullName() == null || second.getFullName() == null) {
                return 0;
            }
            return String.CASE_INSENSITIVE_ORDER.compare(first.getFullName(), second.getFullName());
        }
    }
}