package com.quickblox.q_municate.ui.chats;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.quickblox.q_municate.ui.friends.FriendOperationListener;
import com.quickblox.q_municate_core.db.managers.UsersDatabaseManager;
import com.quickblox.users.model.QBUser;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.models.User;
import com.quickblox.q_municate.ui.base.BaseActivity;
import com.quickblox.q_municate.ui.base.BaseListAdapter;
import com.quickblox.q_municate.ui.views.RoundedImageView;

import java.util.List;

public class GroupDialogOccupantsAdapter extends BaseListAdapter<User> {

    private FriendOperationListener friendOperationListener;

    public GroupDialogOccupantsAdapter(BaseActivity baseActivity, FriendOperationListener friendOperationListener, List<User> objectsList) {
        super(baseActivity, objectsList);
        this.friendOperationListener = friendOperationListener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        User user = getItem(position);

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.list_item_dialog_friend, null);
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

        displayImage(user.getAvatarUrl(), viewHolder.avatarImageView);

        return convertView;
    }

    private void initListeners(ViewHolder viewHolder, final int userId) {
        viewHolder.addFriendImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                friendOperationListener.onAddUserClicked(userId);
            }
        });
    }

    private void setOnlineStatusVisibility(ViewHolder viewHolder, User user) {
        if (isMe(user)) {
            user.setOnline(true);
        }

        viewHolder.onlineStatusTextView.setText(user.getOnlineStatus(baseActivity));
        if (user.isOnline()) {
            viewHolder.onlineImageView.setVisibility(View.VISIBLE);
        } else {
            viewHolder.onlineImageView.setVisibility(View.GONE);
        }
    }

    private boolean isFriendValid(User user) {
        return user.getFullName() != null;
    }

    private boolean isFriend(User user) {
        boolean isFriend;
        isFriend = isMe(user) ? true : UsersDatabaseManager.isFriendInBaseWithPending(baseActivity, user.getUserId());
        return isFriend;
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