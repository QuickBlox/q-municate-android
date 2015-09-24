package com.quickblox.q_municate.ui.adapters.contacts;

import android.app.Activity;
import android.content.res.Resources;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.adapters.base.BaseClickListenerViewHolder;
import com.quickblox.q_municate.ui.adapters.base.BaseViewHolder;
import com.quickblox.q_municate.ui.adapters.base.FilterAdapter;
import com.quickblox.q_municate.core.listeners.UserOperationListener;
import com.quickblox.q_municate.ui.views.roundedimageview.RoundedImageView;
import com.quickblox.q_municate.utils.helpers.TextViewHelper;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.qb.helpers.QBFriendListHelper;
import com.quickblox.q_municate_core.utils.OnlineStatusHelper;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.models.Friend;
import com.quickblox.q_municate_db.models.User;

import java.util.List;

import butterknife.Bind;

public class ContactsAdapter extends FilterAdapter<User, BaseClickListenerViewHolder<User>> {

    private Resources resources;
    private DataManager dataManager;
    private UserType userType;
    private UserOperationListener userOperationListener;
    private QBFriendListHelper friendListHelper;

    public ContactsAdapter(Activity activity, List<User> list) {
        super(activity, list);
        resources = context.getResources();
        dataManager = DataManager.getInstance();
        userType = UserType.LOCAl;
    }

    @Override
    protected boolean isMatch(User item, String query) {
        return item.getFullName() != null && item.getFullName().toLowerCase().contains(query);
    }

    @Override
    public BaseClickListenerViewHolder<User> onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(this, layoutInflater.inflate(R.layout.item_friend, parent, false));
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
            TextViewHelper.changeTextColorView(context, holder.fullNameTextView, query);
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

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    public void setUserOperationListener(UserOperationListener userOperationListener) {
        this.userOperationListener = userOperationListener;
    }

    public void setFriendListHelper(QBFriendListHelper friendListHelper) {
        this.friendListHelper = friendListHelper;
        notifyDataSetChanged();
    }

    private void checkVisibilityItems(ViewHolder viewHolder, User user) {
        if (UserType.GLOBAL.equals(userType)) {
            checkVisibilityItemsAllUsers(viewHolder, user);
        } else if (UserType.LOCAl.equals(userType)) {
            checkVisibilityItemsMyContacts(viewHolder, user);
        }
    }

    private void checkVisibilityItemsAllUsers(ViewHolder viewHolder, User user) {
        boolean me = AppSession.getSession().getUser().getId() == user.getUserId();
        boolean friendOrPending = isFriendOrPending(user);
        viewHolder.addFriendImageView.setVisibility(me || friendOrPending ? View.GONE : View.VISIBLE);
        viewHolder.onlineImageView.setVisibility(View.GONE);
        viewHolder.statusTextView.setVisibility(View.GONE);
    }

    private void checkVisibilityItemsMyContacts(ViewHolder viewHolder, User user) {
        Friend friend = dataManager.getFriendDataManager().getByUserId(user.getUserId());
        User pendingUser = dataManager.getUserRequestDataManager().getUserRequestById(user.getUserId());

        if (friend == null && pendingUser == null) {
            return;
        }

        String status;
        boolean online = friendListHelper != null && friendListHelper.isUserOnline(user.getUserId());

        if (pendingUser != null) {
            viewHolder.onlineImageView.setVisibility(View.GONE);
            status = resources.getString(R.string.frl_pending_request_status);
        } else {
            status = resources.getString(OnlineStatusHelper.getOnlineStatus(online));
        }

        viewHolder.addFriendImageView.setVisibility(View.GONE);
        viewHolder.statusTextView.setText(status);

        viewHolder.statusTextView.setVisibility(View.VISIBLE);

        setStatusVisibility(viewHolder, online);
    }

    private boolean isFriendOrPending(User user) {
        Friend friend = dataManager.getFriendDataManager().getByUserId(user.getUserId());
        User pendingUser = dataManager.getUserRequestDataManager().getUserRequestById(user.getUserId());
        return friend != null || pendingUser != null;
    }

    private void setStatusVisibility(ViewHolder viewHolder, boolean status) {
        if (status) {
            viewHolder.onlineImageView.setVisibility(View.VISIBLE);
        } else {
            viewHolder.onlineImageView.setVisibility(View.INVISIBLE);
        }
    }

    public enum UserType {
        LOCAl, GLOBAL
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

        @Bind(R.id.online_imageview)
        ImageView onlineImageView;

        public ViewHolder(ContactsAdapter adapter, View view) {
            super(adapter, view);
        }
    }
}