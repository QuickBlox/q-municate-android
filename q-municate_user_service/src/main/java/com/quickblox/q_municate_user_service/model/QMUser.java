package com.quickblox.q_municate_user_service.model;

import com.quickblox.q_municate_user_service.utils.Utils;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by pelipets on 1/10/17.
 */

public class QMUser extends QBUser {

    private String avatar;

    private String status;

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public static QMUser convert(QBUser qbUser){
        QMUser result = (QMUser) qbUser;
        final QMUserCustomData userCustomData = Utils.customDataToObject(qbUser.getCustomData());
        result.setAvatar(userCustomData.getAvatarUrl());
        result.setStatus(userCustomData.getStatus());
        return result;
    }

    public static List<QMUser> convertList(List<QBUser> qbUsers){
        List<QMUser> result = new ArrayList<QMUser>(qbUsers.size());
        for(QBUser qbUser: qbUsers){
            result.add(QMUser.convert(qbUser));
        }
        return result;
    }
}
