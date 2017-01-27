package com.quickblox.q_municate_user_service.cache;

import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.quickblox.q_municate_base_cache.QMBaseCache;
import com.quickblox.q_municate_base_cache.model.QMBaseColumns;
import com.quickblox.q_municate_base_cache.utils.ErrorUtils;
import com.quickblox.q_municate_user_service.model.QMUser;
import com.quickblox.users.model.QBUser;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import rx.Observable;

public interface QMUserCache extends QMBaseCache<QMUser, Long> {

    void deleteUserByExternalId(String externalId);

    List<QMUser> getUsersByIDs(Collection<Integer> idsList);

    QMUser getUserByColumn(String column, String value);

    List<QMUser> getUsersByFilter(Collection<?> filterValue, String filter);

}
