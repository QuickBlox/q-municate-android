package com.quickblox.q_municate.ui.adapters.search;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.activities.base.BaseActivity;
import com.quickblox.q_municate.ui.adapters.base.BaseClickListenerViewHolder;
import com.quickblox.q_municate.ui.adapters.base.BaseViewHolder;
import com.quickblox.q_municate.ui.adapters.base.BaseFilterAdapter;
import com.quickblox.q_municate.utils.DateUtils;
import com.quickblox.q_municate.utils.listeners.UserOperationListener;
import com.quickblox.q_municate.ui.views.roundedimageview.RoundedImageView;
import com.quickblox.q_municate.utils.helpers.TextViewHelper;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.qb.helpers.QBFriendListHelper;
import com.quickblox.q_municate_core.utils.OnlineStatusUtils;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.models.Friend;
import com.quickblox.q_municate_db.models.User;

import java.util.List;

import butterknife.Bind;

public class GlobalSearchAdapter extends BaseFilterAdapter<User, BaseClickListenerViewHolder<User>> {

    private DataManager dataManager;
    private UserOperationListener userOperationListener;
    private QBFriendListHelper friendListHelper;

    public GlobalSearchAdapter(BaseActivity baseActivity, List<User> list) {
        super(baseActivity, list);
        dataManager = DataManager.getInstance();
    }

    @Override
    protected boolean isMatch(User item, String query) {
        return item.getFullName() != null && item.getFullName().toLowerCase().contains(query);
    }

    @Override
    public BaseClickListenerViewHolder<User> onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(this, layoutInflater.inflate(R.layout.item_user, parent, false));
    }

    @Override
    public void onBindViewHolder(BaseClickListenerViewHolder<User> baseClickListenerViewHolder, int position) {
        User user = getItem(position);
        ViewHolder holder = (ViewHolder) baseClickListenerViewHolder;

        if (user.getFullName() != null) {
            holder.fullNameTextView.setText(user.getFullName());
        } else {
            holder.fullNameTextView.setText(user.getUserId());
        }

        String avatarUrl = user.getAvatar();
        displayAvatarImage(avatarUrl, holder.avatarImageView);

        checkVisibilityItems(holder, user);

        initListeners(holder, user.getUserId());

        if (!TextUtils.isEmpty(query)) {
            TextViewHelper.changeTextColorView(baseActivity, holder.fullNameTextView, query);
        }
    }

    private void initListeners(ViewHolder viewHolder, final int userId) {
        viewHolder.addFriendImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userOperationListener.onAddUserClicked(userId);
            }
        });
    }

    public void setUserOperationListener(UserOperationListener userOperationListener) {
        this.userOperationListener = userOperationListener;
    }

    public void setFriendListHelper(QBFriendListHelper friendListHelper) {
        this.friendListHelper = friendListHelper;
        notifyDataSetChanged();
    }

    private void checkVisibilityItems(ViewHolder viewHolder, User user) {
        if (isFriendOrPending(user)) {
            checkVisibilityItemsMyContacts(viewHolder, user);
        } else {
            checkVisibilityItemsAllUsers(viewHolder, user);
        }
    }

    private void checkVisibilityItemsAllUsers(ViewHolder viewHolder, User user) {
        boolean me = AppSession.getSession().getUser().getId() == user.getUserId();
        viewHolder.addFriendImageView.setVisibility(me ? View.GONE : View.VISIBLE);
        viewHolder.statusTextView.setVisibility(View.GONE);
    }

    private void checkVisibilityItemsMyContacts(ViewHolder viewHolder, User user) {
        String status;
        User pendingUser = dataManager.getUserRequestDataManager().getUserRequestById(user.getUserId());

        if (pendingUser != null) {
            status = resources.getString(R.string.search_pending_request_status);
            viewHolder.statusTextView.setTextColor(resources.getColor(R.color.dark_gray));
            viewHolder.statusTextView.setText(status);
        } else {
            setOnlineStatus(viewHolder, user);
        }

        viewHolder.statusTextView.setVisibility(View.VISIBLE);
        viewHolder.addFriendImageView.setVisibility(View.GONE);
    }

    private boolean isFriendOrPending(User user) {
        Friend friend = dataManager.getFriendDataManager().getByUserId(user.getUserId());
        User pendingUser = dataManager.getUserRequestDataManager().getUserRequestById(user.getUserId());
        return friend != null || pendingUser != null;
    }

    private void setOnlineStatus(ViewHolder viewHolder, User user) {
        boolean online = friendListHelper != null && friendListHelper.isUserOnline(user.getUserId());

        if (online) {
            viewHolder.statusTextView.setText(OnlineStatusUtils.getOnlineStatus(online));
            viewHolder.statusTextView.setTextColor(resources.getColor(R.color.green));
        } else {
            viewHolder.statusTextView.setText(resources.getString(R.string.last_seen,
                    DateUtils.toTodayYesterdayShortDateWithoutYear2(user.getLastLogin()),
                    DateUtils.formatDateSimpleTime(user.getLastLogin())));
            viewHolder.statusTextView.setTextColor(resources.getColor(R.color.dark_gray));
        }
    }

    protected static class ViewHolder extends BaseViewHolder<User> {

        @Bind(R.id.avatar_imageview)
        RoundedImageView avatarImageView;

        @Bind(R.id.name_textview)
        TextView fullNameTextView;

        @Bind(R.id.status_textview)
        TextView statusTextView;

        @Bind(R.id.add_friend_imagebutton)
        ImageView addFriendImageView;

        public ViewHolder(GlobalSearchAdapter adapter, View view) {
            super(adapter, view);
        }
    }
}