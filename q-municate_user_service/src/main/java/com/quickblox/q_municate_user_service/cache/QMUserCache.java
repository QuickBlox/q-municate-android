package com.quickblox.q_municate_user_service.cache;

import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.quickblox.q_municate_base_cache.QMBaseCache;
import com.quickblox.q_municate_base_cache.model.QMBaseColumns;
import com.quickblox.q_municate_base_cache.utils.ErrorUtils;
import com.quickblox.users.model.QBUser;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import rx.Observable;

public interface QMUserCache extends QMBaseCache<QBUser> {

    void deleteUserByExternalId(String externalId);

    List<QBUser> getUsersByIDs(Collection<Integer> idsList);

    QBUser getUserByColumn(String column, String value);

    List<QBUser> getUsersByFilter(Collection<?> filterValue, String filter);

    List<QBUser> getUsersByColumn(String column, String value);

    List<QBUser> getUsersByColumn(String column, Collection<String> values);
}
