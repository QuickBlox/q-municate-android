package com.quickblox.q_municate.ui.adapters.chats;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.quickblox.q_municate.utils.listeners.UserOperationListener;
import com.quickblox.q_municate_core.qb.helpers.QBFriendListHelper;
import com.quickblox.q_municate_core.utils.OnlineStatusUtils;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.models.User;
import com.quickblox.users.model.QBUser;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate.ui.activities.base.BaseActivity;
import com.quickblox.q_municate.ui.adapters.base.BaseListAdapter;
import com.quickblox.q_municate.ui.views.roundedimageview.RoundedImageView;

import java.util.List;

public class GroupDialogOccupantsAdapter extends BaseListAdapter<User> {

    private UserOperationListener userOperationListener;
    private QBFriendListHelper friendListHelper;

    public GroupDialogOccupantsAdapter(BaseActivity baseActivity, UserOperationListener userOperationListener, List<User> objectsList) {
        super(baseActivity, objectsList);
        this.userOperationListener = userOperationListener;
    }

    public void setFriendListHelper(QBFriendListHelper friendListHelper) {
        this.friendListHelper = friendListHelper;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        User user = getItem(position);

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.item_dialog_friend, null);
            viewHolder = new ViewHolder();

            viewHolder.avatarImageView = (RoundedImageView) convertView.findViewById(R.id.avatar_imageview);
            viewHolder.nameTextView = (TextView) convertView.findViewById(R.id.name_textview);
            viewHolder.onlineImageView = (ImageView) convertView.findViewById(R.id.online_imageview);
            viewHolder.onlineStatusTextView = (TextView) convertView.findViewById(R.id.status_textview);
            viewHolder.addFriendImageView = (ImageView) convertView.findViewById(R.id.add_friend_imagebutton);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        String fullName;
        if (isFriendValid(user)) {
            fullName = user.getFullName();
            viewHolder.onlineStatusTextView.setVisibility(View.VISIBLE);
        } else {
            fullName = String.valueOf(user.getUserId());
            viewHolder.onlineStatusTextView.setVisibility(View.GONE);
        }
        viewHolder.nameTextView.setText(fullName);

        setOnlineStatusVisibility(viewHolder, user);
        viewHolder.addFriendImageView.setVisibility(isFriend(user) ? View.GONE : View.VISIBLE);

        initListeners(viewHolder, user.getUserId());

        displayAvatarImage(user.getAvatar(), viewHolder.avatarImageView);

        return convertView;
    }

    private void initListeners(ViewHolder viewHolder, final int userId) {
        viewHolder.addFriendImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userOperationListener.onAddUserClicked(userId);
            }
        });
    }

    private void setOnlineStatusVisibility(ViewHolder viewHolder, User user) {
        boolean online = friendListHelper != null && friendListHelper.isUserOnline(user.getUserId());

        if (isMe(user)) {
            online = true;
        }

        viewHolder.onlineStatusTextView.setText(OnlineStatusUtils.getOnlineStatus(online));

        if (online) {
            viewHolder.onlineImageView.setVisibility(View.VISIBLE);
        } else {
            viewHolder.onlineImageView.setVisibility(View.GONE);
        }
    }

    private boolean isFriendValid(User user) {
        return user.getFullName() != null;
    }

    private boolean isFriend(User user) {
        if (isMe(user)) {
            return true;
        } else {
            boolean outgoingUserRequest = DataManager.getInstance().getUserRequestDataManager().existsByUserId(user.getUserId());
            boolean friend = DataManager.getInstance().getFriendDataManager().getByUserId(user.getUserId()) != null;
            return friend || outgoingUserRequest;
        }
    }

    private boolean isMe(User inputUser) {
        QBUser currentUser = AppSession.getSession().getUser();
        return currentUser.getId() == inputUser.getUserId();
    }

    private static class ViewHolder {

        RoundedImageView avatarImageView;
        TextView nameTextView;
        ImageView addFriendImageView;
        ImageView onlineImageView;
        TextView onlineStatusTextView;
    }
}