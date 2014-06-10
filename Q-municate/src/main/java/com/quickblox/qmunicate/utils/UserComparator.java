package com.quickblox.qmunicate.utils;

import com.quickblox.module.users.model.QBUser;

import java.util.Comparator;

public class UserComparator implements Comparator<QBUser> {

    @Override
    public int compare(QBUser lhs, QBUser rhs) {
        if (lhs.getFullName() == null || rhs.getFullName() == null) {
            return 0;
        }
        return String.CASE_INSENSITIVE_ORDER.compare(lhs.getFullName(), rhs.getFullName());
    }
}
