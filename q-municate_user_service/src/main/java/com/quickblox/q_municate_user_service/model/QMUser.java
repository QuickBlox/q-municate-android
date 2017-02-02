package com.quickblox.q_municate_user_service.model;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.table.DatabaseTable;
import com.quickblox.q_municate_user_service.utils.Utils;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Created by pelipets on 1/10/17.
 */

@DatabaseTable(tableName = QMUserColumns.TABLE_NAME)
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
        QMUser result = new QMUser();
        result.setId(qbUser.getId());
        result.setFullName(qbUser.getFullName());
        result.setEmail(qbUser.getEmail());
        result.setLogin(qbUser.getLogin());
        result.setPhone(qbUser.getPhone());
        result.setWebsite(qbUser.getWebsite());
        result.setLastRequestAt(qbUser.getLastRequestAt());
        result.setExternalId(qbUser.getExternalId());
        result.setFacebookId(qbUser.getFacebookId());
        result.setTwitterId(qbUser.getTwitterId());
        result.setTwitterDigitsId(qbUser.getTwitterDigitsId());
        result.setFileId(qbUser.getFileId());
        result.setTags(qbUser.getTags());
        result.setPassword(qbUser.getPassword());
        result.setOldPassword(qbUser.getOldPassword());
        result.setCustomData(qbUser.getCustomData());
        result.setCreatedAt(qbUser.getCreatedAt());
        result.setUpdatedAt(qbUser.getUpdatedAt());

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
