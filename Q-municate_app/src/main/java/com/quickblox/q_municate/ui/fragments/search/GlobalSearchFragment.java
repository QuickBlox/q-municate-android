package com.quickblox.q_municate.ui.fragments.search;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.utils.listeners.UserOperationListener;
import com.quickblox.q_municate.utils.listeners.SearchListener;
import com.quickblox.q_municate.ui.activities.profile.UserProfileActivity;
import com.quickblox.q_municate.ui.adapters.search.GlobalSearchAdapter;
import com.quickblox.q_municate.ui.fragments.base.BaseFragment;
import com.quickblox.q_municate.ui.fragments.dialogs.base.OneButtonDialogFragment;
import com.quickblox.q_municate.utils.listeners.simple.SimpleOnRecycleItemClickListener;
import com.quickblox.q_municate.ui.views.recyclerview.SimpleDividerItemDecoration;
import com.quickblox.q_municate.utils.KeyboardUtils;
import com.quickblox.q_municate_core.core.command.Command;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.qb.commands.friend.QBAddFriendCommand;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.managers.base.BaseManager;
import com.quickblox.q_municate_user_service.QMUserService;
import com.quickblox.q_municate_user_service.model.QMUser;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;
import butterknife.OnTouch;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class GlobalSearchFragment extends BaseFragment implements SearchListener, SwipyRefreshLayout.OnRefreshListener {

    private static final String TAG = GlobalSearchFragment.class.getSimpleName();
    private static final int SEARCH_DELAY = 1000;
    private static final int MIN_VALUE_FOR_SEARCH = 3;

    @Bind(R.id.contacts_swipyrefreshlayout)
    SwipyRefreshLayout swipyRefreshLayout;

    @Bind(R.id.contacts_recyclerview)
    RecyclerView contactsRecyclerView;

    private Timer searchTimer;
    private int page = 1;
    private int totalEntries;
    private UserOperationAction userOperationAction;
    private DataManager dataManager;
    private Observer commonObserver;
    private GlobalSearchAdapter globalSearchAdapter;
    private List<QBUser> usersList;
    private String searchQuery;
    private boolean excludedMe;

    public static GlobalSearchFragment newInstance() {
        return new GlobalSearchFragment();
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
        View view = layoutInflater.inflate(R.layout.fragment_global_search, container, false);

        activateButterKnife(view);

        initFields();
        initContactsList(usersList);
        initCustomListeners();

        addActions();
        addObservers();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        globalSearchAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeActions();
        deleteObservers();
    }

    @OnTouch(R.id.contacts_recyclerview)
    boolean touchContactsList(View view, MotionEvent event) {
        KeyboardUtils.hideKeyboard(baseActivity);
        return false;
    }

    @Override
    public void prepareSearch() {
        clearOldData();
    }

    @Override
    public void search(String searchQuery) {
        this.searchQuery = searchQuery;
        clearOldData();

        if (!baseActivity.checkNetworkAvailableWithError()) {
            return;
        }

        startSearch();
        if (globalSearchAdapter != null && !globalSearchAdapter.getAllItems().isEmpty()) {
            globalSearchAdapter.setFilter(searchQuery);
        }
    }

    @Override
    public void cancelSearch() {
        searchQuery = null;
        searchTimer.cancel();
        clearOldData();

        if (globalSearchAdapter != null) {
            updateList();
        }
    }

    @Override
    public void onRefresh(SwipyRefreshLayoutDirection swipyRefreshLayoutDirection) {
        if (!usersList.isEmpty() && usersList.size() < totalEntries) {
            page++;
            searchUsers();
        } else {
            swipyRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onConnectedToService(QBService service) {
        super.onConnectedToService(service);
        if (friendListHelper != null && globalSearchAdapter != null) {
            globalSearchAdapter.setFriendListHelper(friendListHelper);
        }
    }

    @Override
    public void onChangedUserStatus(int userId, boolean online) {
        super.onChangedUserStatus(userId, online);
        globalSearchAdapter.notifyDataSetChanged();
    }

    private void initFields() {
        dataManager = DataManager.getInstance();
        searchTimer = new Timer();
        usersList = new ArrayList<>();
        userOperationAction = new UserOperationAction();
        commonObserver = new CommonObserver();
        swipyRefreshLayout.setEnabled(false);
    }

    private void initContactsList(List<QBUser> usersList) {
        globalSearchAdapter = new GlobalSearchAdapter(baseActivity, usersList);
        globalSearchAdapter.setFriendListHelper(friendListHelper);
        contactsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        contactsRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));;
        contactsRecyclerView.setAdapter(globalSearchAdapter);
        globalSearchAdapter.setUserOperationListener(userOperationAction);
    }

    private void initCustomListeners() {
        globalSearchAdapter.setOnRecycleItemClickListener(new SimpleOnRecycleItemClickListener<QBUser>() {

            @Override
            public void onItemClicked(View view, QBUser user, int position) {
                boolean isFriend = dataManager.getFriendDataManager().existsByUserId(user.getId());
                boolean outgoingUser = dataManager.getUserRequestDataManager()
                        .existsByUserId(user.getId());
                if (isFriend || outgoingUser) {
                    UserProfileActivity.start(baseActivity, user.getId());
                }
            }
        });

        swipyRefreshLayout.setOnRefreshListener(this);
    }

    private void updateList() {
        globalSearchAdapter.setList(usersList);
    }

    private void updateContactsList(List<QBUser> usersList) {
        this.usersList = usersList;
        globalSearchAdapter.setList(usersList);
        globalSearchAdapter.setFilter(searchQuery);
    }

    private void removeActions() {
        baseActivity.removeAction(QBServiceConsts.FIND_USERS_SUCCESS_ACTION);
        baseActivity.removeAction(QBServiceConsts.FIND_USERS_FAIL_ACTION);

        baseActivity.removeAction(QBServiceConsts.ADD_FRIEND_SUCCESS_ACTION);
        baseActivity.removeAction(QBServiceConsts.ADD_FRIEND_FAIL_ACTION);

        baseActivity.updateBroadcastActionList();
    }

    private void addActions() {
        baseActivity.addAction(QBServiceConsts.FIND_USERS_SUCCESS_ACTION, new FindUserSuccessAction());
        baseActivity.addAction(QBServiceConsts.FIND_USERS_FAIL_ACTION, new FindUserFailAction());

        baseActivity.addAction(QBServiceConsts.ADD_FRIEND_SUCCESS_ACTION, new AddFriendSuccessAction());
        baseActivity.addAction(QBServiceConsts.ADD_FRIEND_FAIL_ACTION, failAction);

        baseActivity.updateBroadcastActionList();
    }

    private void addObservers() {
        dataManager.getUserRequestDataManager().addObserver(commonObserver);
        dataManager.getFriendDataManager().addObserver(commonObserver);
    }

    private void deleteObservers() {
        if(dataManager != null) {
            dataManager.getUserRequestDataManager().deleteObserver(commonObserver);
            dataManager.getFriendDataManager().deleteObserver(commonObserver);
        }
    }

    private void clearOldData() {
        usersList.clear();
        page = 1;
        excludedMe = false;
    }

    private void startSearch() {
        searchTimer.cancel();
        searchTimer = new Timer();
        searchTimer.schedule(new SearchTimerTask(), SEARCH_DELAY);
    }

    private void searchUsers() {
        if (!TextUtils.isEmpty(searchQuery) && checkSearchDataWithError(searchQuery)) {

            QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder();
            requestBuilder.setPage(page);
            requestBuilder.setPerPage(ConstsCore.FL_FRIENDS_PER_PAGE);

            QMUserService.getInstance().getUsersByFullName(searchQuery, requestBuilder, true).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new rx.Observer<List<QMUser>>() {

                @Override
                public void onCompleted() {
                    Log.d(TAG, "onCompleted()");
                }

                @Override
                public void onError(Throwable e) {
                    Log.d(TAG, "onError" + e.getMessage());
                    swipyRefreshLayout.setRefreshing(false);
                }

                @Override
                public void onNext(List<QMUser> qbUsers) {

                    if (qbUsers != null && !qbUsers.isEmpty()) {
                        checkForExcludeMe(qbUsers);

                        usersList.addAll(qbUsers);

                        updateContactsList(usersList);
                    }

                    swipyRefreshLayout.setRefreshing(false);
                    checkForEnablingRefreshLayout();
                }
            });
        }
    }

    private boolean checkSearchDataWithError(String searchQuery) {
        boolean correct = searchQuery != null && searchQuery.length() >= MIN_VALUE_FOR_SEARCH;
        if (correct) {
            return true;
        } else {
            OneButtonDialogFragment.show(getChildFragmentManager(), R.string.search_at_last_items, true);
            return false;
        }
    }

    private void addToFriendList(final int userId) {
        if (!baseActivity.checkNetworkAvailableWithError()) {
            return;
        }

        baseActivity.showProgress();
        QBAddFriendCommand.start(baseActivity, userId);
        KeyboardUtils.hideKeyboard(baseActivity);
    }

    private void checkForEnablingRefreshLayout() {
        swipyRefreshLayout.setEnabled(usersList.size() != totalEntries);
    }

    private void parseResult(Bundle bundle) {
    }

    private void checkForExcludeMe(Collection<QMUser> usersCollection) {
        QBUser qbUser = AppSession.getSession().getUser();
        QMUser me = QMUser.convert(qbUser);
        if (usersCollection.contains(me)) {
            usersCollection.remove(me);
            excludedMe = true;
            totalEntries--;
        }
    }

    private class SearchTimerTask extends TimerTask {

        @Override
        public void run() {
            searchUsers();
        }
    }

    private class FindUserSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            parseResult(bundle);

            swipyRefreshLayout.setRefreshing(false);
            checkForEnablingRefreshLayout();
        }
    }

    private class FindUserFailAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            OneButtonDialogFragment.show(getChildFragmentManager(), R.string.search_users_not_found, true);
            usersList.clear();

            swipyRefreshLayout.setRefreshing(false);
            checkForEnablingRefreshLayout();
        }
    }

    private class UserOperationAction implements UserOperationListener {

        @Override
        public void onAddUserClicked(int userId) {
            addToFriendList(userId);
        }
    }

    private class AddFriendSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            int userId = bundle.getInt(QBServiceConsts.EXTRA_FRIEND_ID);

            QMUser addedUser = QMUserService.getInstance().getUserCache().get((long)userId);
            globalSearchAdapter.notifyDataSetChanged();

            baseActivity.hideProgress();
        }
    }

    private class CommonObserver implements Observer {

        @Override
        public void update(Observable observable, Object data) {
            if (data != null) {
                String observerKey = ((Bundle) data).getString(BaseManager.EXTRA_OBSERVE_KEY);
                if (observerKey.equals(dataManager.getUserRequestDataManager().getObserverKey()) || observerKey.equals(dataManager.getFriendDataManager().getObserverKey())) {
                    updateList();
                }
            }
        }
    }
}