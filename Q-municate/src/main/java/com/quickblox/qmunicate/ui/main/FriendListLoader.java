package com.quickblox.qmunicate.ui.main;

import android.content.Context;

import com.quickblox.internal.module.custom.request.QBCustomObjectRequestBuilder;
import com.quickblox.module.custom.QBCustomObjects;
import com.quickblox.module.custom.model.QBCustomObject;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.utils.Consts;

import java.util.ArrayList;
import java.util.List;

public class FriendListLoader extends AbsFriendListLoader {
    public static final int ID = 0;

    public FriendListLoader(Context context) {
        super(context);
    }

    @Override
    protected List<Integer> getUserIds() throws Exception {
        Arguments arguments = (Arguments) args;
        QBCustomObjectRequestBuilder builder = new QBCustomObjectRequestBuilder();
        QBUser user = App.getInstance().getUser();
        builder.eq(Consts.FRIEND_FIELD_USER_ID, user.getId());
        builder.setPagesLimit(arguments.perPage);
        int pagesSkip = arguments.perPage * (arguments.page - 1);
        builder.setPagesSkip(pagesSkip);

        List<QBCustomObject> objects = QBCustomObjects.getObjects(Consts.FRIEND_CLASS_NAME, builder);
        List<Integer> userIds = new ArrayList<Integer>();
        for (QBCustomObject o : objects) {
            userIds.add(Integer.parseInt((String) o.getFields().get(Consts.FRIEND_FIELD_FRIEND_ID)));
        }
        return userIds;
    }


}
