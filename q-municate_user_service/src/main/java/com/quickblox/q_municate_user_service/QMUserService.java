package com.quickblox.q_municate_user_service;

import com.quickblox.q_municate_base_cache.QMBaseCache;
import com.quickblox.q_municate_base_cache.model.BaseModel;
import com.quickblox.q_municate_base_service.QMBaseService;
import com.quickblox.q_municate_base_service.QMServiceManagerListener;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;

public class QMUserService extends QMBaseService {

    private QMUserMemoryCache userMemoryCache;
    private List<QMUserServiceListener> listeners;
    private QMUserServiceCacheDataSource cacheDataSource;

    public void initWithServiceManager(QMServiceManagerListener serviceManagerListener, QMUserServiceCacheDataSource cacheDataSource){
        super.init(serviceManagerListener);
        this.cacheDataSource = cacheDataSource;
    }

    public void addListener(QMUserServiceListener listener){
        listeners.add(listener);
    }

    public void removeListener(QMUserServiceListener listener){
        listeners.remove(listener);
    }

    @Override
    public void init(QMBaseCache<BaseModel> cache) {
        //???
    }

    @Override
    protected void serviceWillStart() {
        this.listeners = new ArrayList<>();
        this.userMemoryCache = new QMUserMemoryCache();
    }


    /**
     * Data source for QMUsersService
     */

    public  interface QMUserServiceCacheDataSource {

        /**
         * Is called when users service will start. Need to use for inserting initial data QMUsersMemoryStorage.
         *
         * @param users for provide QBUUsers collection
         */
        void cachedUsers(List<QBUser> users);
    }

    /**
     * QMUsersServiceListener
     */

    public interface QMUserServiceListener {

        /**
         *  Is called when users were loaded from cache to memory storage
         *
         *  @param userService QMUserService instance
         *  @param users        List of QBUUser instances as users
         */
        void didLoadUsersFromCache(QMUserService userService, List<QBUser> users);


        /**
         *  Is called when users were added to QMUsersService.
         *
         *  @param userService     QMUserService instance
         *  @param users           List of QBUUser instances as users
         */
        void didAddUsers(QMUserService userService, List<QBUser> users);


        /**
         *  Is called when users were updated in cache by forcing its load from server.
         *
         *  @param userService     QMUserService instance
         *  @param users           List of QBUUser instances as users
         */
        void didUpdateUsers(QMUserService userService, List<QBUser> users);
    }

}
