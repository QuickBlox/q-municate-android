package com.quickblox.q_municate_core.qb.helpers;

import android.content.Context;
import android.util.Log;

import com.quickblox.core.exception.QBResponseException;
import com.quickblox.q_municate_core.utils.UserFriendUtils;
import com.quickblox.q_municate_user_service.QMUserService;
import com.quickblox.q_municate_user_service.model.QMUser;

import java.util.List;

public class QBRestHelper extends BaseHelper {
    private static final String TAG = QBRestHelper.class.getSimpleName();

    public QBRestHelper(Context context) {
        super(context);
    }

    public static QMUser loadAndSaveUser(int userId) {
        QMUser resultUser = null;
        try {
            QMUser user = QMUserService.getInstance().getUserSync(userId, true);
            resultUser = user;
        } catch (QBResponseException e) {
            // user not found
            resultUser = createDeletedUser(userId);
        }

        return resultUser;
    }

    public static List<QMUser> loadAndSaveUserByIds(List<Integer> userIds) {
        List<QMUser> resultUser = null;
        try {
            List<QMUser> loadedUserList = QMUserService.getInstance().getUsersByIDsSync(userIds, null);
            resultUser = loadedUserList;

            // user not found
            if(resultUser.isEmpty()){
                createDeletedUser(userIds.iterator().next());
            }
        } catch (QBResponseException e) {
            Log.e(TAG, "failed load users" + e);
        }

        return resultUser;
    }

    private static QMUser createDeletedUser(int userId) {
        QMUser resultUser = UserFriendUtils.createDeletedUser(userId);
        QMUserService.getInstance().getUserCache().createOrUpdate(resultUser);
        return resultUser;
    }

}