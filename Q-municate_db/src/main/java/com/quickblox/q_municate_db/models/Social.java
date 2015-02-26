package com.quickblox.q_municate_db.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = Social.TABLE_NAME)
public class Social {

    public static final String TABLE_NAME = "social";

    public static final String COLUMN_SOCIAL_ID = "social_id";

    @DatabaseField(id = true, columnName = COLUMN_SOCIAL_ID)
    private String socialId;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, canBeNull = false,
            columnName = SocialType.COLUMN_TYPE_ID)
    private SocialType socialType;

    public Social() {
    }

    public Social(String socialId, SocialType socialType) {
        this.socialId = socialId;
        this.socialType = socialType;
    }

    public String getSocialId() {
        return socialId;
    }

    public void setSocialId(String socialId) {
        this.socialId = socialId;
    }

    public SocialType getSocialType() {
        return socialType;
    }

    public void setSocialType(SocialType socialType) {
        this.socialType = socialType;
    }
}