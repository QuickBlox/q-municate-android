package com.quickblox.qmunicate.ui.importfriends;

import android.content.Context;
import android.os.Bundle;

import com.quickblox.internal.core.exception.QBResponseException;
import com.quickblox.internal.core.request.QBPagedRequestBuilder;
import com.quickblox.module.users.QBUsers;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.core.ui.BaseLoader;

import java.util.ArrayList;
import java.util.List;

public class UsersByEmailLoader extends BaseLoader<List<QBUser>> {
    public static final int ID = 5;
    private static ArrayList<String> idsList;

    public UsersByEmailLoader(Context context) {
        super(context);
    }

    public static Arguments newArguments(int page, int perPage, ArrayList<String> ids) {
        Arguments arguments = new Arguments();
        arguments.page = page;
        arguments.perPage = perPage;

        idsList = ids;

        return arguments;
    }

    @Override
    public List<QBUser> performInBackground() throws QBResponseException {
        Arguments arguments = (Arguments) args;

        QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder();
        requestBuilder.setPage(arguments.page);
        requestBuilder.setPerPage(arguments.perPage);

        Bundle params = new Bundle();

        List<QBUser> users = QBUsers.getUsersByEmails(idsList, requestBuilder, params);

        return users;
    }

    private static class Arguments extends BaseLoader.Args {
        int page;
        int perPage;
    }
}