package com.quickblox.q_municate.ui.chats;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.quickblox.module.users.model.QBUser;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.model.AppSession;
import com.quickblox.q_municate.model.User;
import com.quickblox.q_municate.ui.base.BaseActivity;
import com.quickblox.q_municate.ui.base.BaseListAdapter;
import com.quickblox.q_municate.ui.views.RoundedImageView;

import java.util.List;

public class GroupDialogOccupantsAdapter extends BaseListAdapter<User> {

    public GroupDialogOccupantsAdapter(BaseActivity baseActivity, List<User> objectsList) {
        super(baseActivity, objectsList);
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
            viewHolder.onlineStatusMessageTextView = (TextView) convertView.findViewById(
                    R.id.statusMessageTextView);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        String fullName;
        if (isFriend(user)) {
            fullName = user.getFullName();
            viewHolder.onlineStatusMessageTextView.setVisibility(View.VISIBLE);
        } else {
            fullName = String.valueOf(user.getUserId());
            viewHolder.onlineStatusMessageTextView.setVisibility(View.GONE);
        }
        viewHolder.nameTextView.setText(fullName);

        setOnlineStatusVisibility(viewHolder, user);

        displayImage(user.getAvatarUrl(), viewHolder.avatarImageView);

        return convertView;
    }

    private void setOnlineStatusVisibility(ViewHolder viewHolder, User user) {
        if(isMe(user)) {
            user.setOnline(true);
        }

        viewHolder.onlineStatusMessageTextView.setText(user.getOnlineStatus());
        if (user.isOnline()) {
            viewHolder.onlineImageView.setVisibility(View.VISIBLE);
        } else {
            viewHolder.onlineImageView.setVisibility(View.GONE);
        }
    }

    private boolean isFriend(User user) {
        return user.getFullName() != null;
    }

    private boolean isMe(User inputUser) {
        QBUser currentUser = AppSession.getSession().getUser();
        return currentUser.getId() == inputUser.getUserId();
    }

    private static class ViewHolder {

        RoundedImageView avatarImageView;
        TextView nameTextView;
        ImageView onlineImageView;
        TextView onlineStatusMessageTextView;
    }
}