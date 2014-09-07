package com.quickblox.q_municate.ui.friends;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.quickblox.q_municate.R;
import com.quickblox.q_municate.db.DatabaseManager;
import com.quickblox.q_municate.model.User;
import com.quickblox.q_municate.ui.base.BaseActivity;
import com.quickblox.q_municate.ui.base.BaseListAdapter;
import com.quickblox.q_municate.ui.views.RoundedImageView;
import com.quickblox.q_municate.utils.TextViewHelper;

import java.util.List;

public class UserListAdapter extends BaseListAdapter<User> {

    private FriendsListFragment.FriendOperationListener friendOperationListener;
    private String searchCharacters;

    public UserListAdapter(BaseActivity activity, List<User> users, FriendsListFragment.FriendOperationListener friendOperationListener) {
        super(activity, users);
        this.friendOperationListener = friendOperationListener;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        User user = objectsList.get(position);

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.list_item_user, null);
            holder = createViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String url = user.getAvatarUrl();

        displayImage(url, holder.avatarImageView);

        holder.fullNameTextView.setText(user.getFullName());
        holder.addFriendImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                friendOperationListener.onAddUserClicked(position);
            }
        });

        if (DatabaseManager.isFriendInBase(baseActivity, user.getUserId())) {
            holder.addFriendImageButton.setVisibility(View.INVISIBLE);
        } else {
            holder.addFriendImageButton.setVisibility(View.VISIBLE);
        }

        if (!TextUtils.isEmpty(searchCharacters)) {
            TextViewHelper.changeTextColorView(baseActivity, holder.fullNameTextView, searchCharacters);
        }

        return convertView;
    }

    public void setSearchCharacters(String searchCharacters) {
        this.searchCharacters = searchCharacters;
    }

    private ViewHolder createViewHolder(View view) {
        ViewHolder holder = new ViewHolder();
        holder.avatarImageView = (RoundedImageView) view.findViewById(R.id.avatar_imageview);
        holder.fullNameTextView = (TextView) view.findViewById(R.id.name_textview);
        holder.addFriendImageButton = (ImageButton) view.findViewById(R.id.add_friend_imagebutton);
        return holder;
    }

    private static class ViewHolder {

        public RoundedImageView avatarImageView;
        public TextView fullNameTextView;
        public ImageButton addFriendImageButton;
    }
}