package com.quickblox.q_municate_core.new_db.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class FriendsRelationStatus {

    @DatabaseField(generatedId = true)
    private int relationStatusId;

    @DatabaseField
    private int relationStatus;

    public int getRelationStatusId() {
        return relationStatusId;
    }

    public void setRelationStatusId(int relationStatusId) {
        this.relationStatusId = relationStatusId;
    }

    public int getRelationStatus() {
        return relationStatus;
    }

    public void setRelationStatus(int relationStatus) {
        this.relationStatus = relationStatus;
    }
}