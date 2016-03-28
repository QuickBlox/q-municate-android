package com.quickblox.q_municate.ui.adapters.chats;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.quickblox.q_municate.utils.DateUtils;
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
    private QBFriendListHelper qbFriendListHelper;

    public GroupDialogOccupantsAdapter(BaseActivity baseActivity, UserOperationListener userOperationListener, List<User> objectsList) {
        super(baseActivity, objectsList);
        this.userOperationListener = userOperationListener;
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
            viewHolder.onlineStatusTextView = (TextView) convertView.findViewById(R.id.status_textview);
            viewHolder.addFriendImageView = (ImageView) convertView.findViewById(R.id.add_friend_imagebutton);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        String fullName;
        if (isFriendValid(user)) {
            fullName = user.getFullName();
        } else {
            fullName = String.valueOf(user.getUserId());
        }
        viewHolder.nameTextView.setText(fullName);

        setStatus(viewHolder, user);
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

    private void setStatus(ViewHolder viewHolder, User user) {
        boolean online = qbFriendListHelper != null && qbFriendListHelper.isUserOnline(user.getUserId());

        if (isMe(user)) {
            online = true;
        }

        if (online) {
            viewHolder.onlineStatusTextView.setText(OnlineStatusUtils.getOnlineStatus(online));
            viewHolder.onlineStatusTextView.setTextColor(context.getResources().getColor(R.color.green));
        } else {
            viewHolder.onlineStatusTextView.setText(context.getString(R.string.last_seen,
                    DateUtils.toTodayYesterdayShortDateWithoutYear2(user.getLastLogin()),
                    DateUtils.formatDateSimpleTime(user.getLastLogin())));
            viewHolder.onlineStatusTextView.setTextColor(context.getResources().getColor(R.color.dark_gray));
        }
    }

    public void setFriendListHelper(QBFriendListHelper qbFriendListHelper) {
        this.qbFriendListHelper = qbFriendListHelper;
        notifyDataSetChanged();
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
        TextView onlineStatusTextView;
    }
}