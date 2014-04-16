package com.quickblox.qmunicate.ui.main;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.quickblox.internal.core.exception.BaseServiceException;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.caching.DatabaseManager;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.ui.base.BaseActivity;
import com.quickblox.qmunicate.ui.base.BaseListAdapter;
import com.quickblox.qmunicate.utils.ErrorUtils;
import com.quickblox.qmunicate.utils.TextViewHelper;
import com.quickblox.qmunicate.utils.UriCreator;

import java.util.List;

public class UserListAdapter extends BaseListAdapter<Friend> {

    private UserListListener listener;
    private String searchCharacters;

    public UserListAdapter(BaseActivity activity, List<Friend> users, UserListListener listener) {
        super(activity, users);
        this.listener = listener;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        Friend user = objectsList.get(position);

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.list_item_user, null);
            holder = createViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String url = null;
        if (null != user.getAvatarUid()) {
            try {
                url = UriCreator.getUri(user.getAvatarUid());
            } catch (BaseServiceException e) {
                ErrorUtils.showError(baseActivity, e);
            }
        }
        displayImage(url, holder.avatarImageView);

        holder.fullnameTextView.setText(user.getFullname());
        holder.addFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onUserSelected(position);
            }
        });

        if (DatabaseManager.searchFriendInBase(baseActivity, user.getId())) {
            holder.addFriendButton.setVisibility(View.INVISIBLE);
        } else {
            holder.addFriendButton.setVisibility(View.VISIBLE);
        }

        if (!TextUtils.isEmpty(searchCharacters)) {
            TextViewHelper.changeTextColorView(baseActivity, holder.fullnameTextView, searchCharacters);
        }

        return convertView;
    }

    public void setSearchCharacters(String searchCharacters) {
        this.searchCharacters = searchCharacters;
    }

    private ViewHolder createViewHolder(View view) {
        ViewHolder holder = new ViewHolder();
        holder.avatarImageView = (ImageView) view.findViewById(R.id.avatarImageView);
        holder.fullnameTextView = (TextView) view.findViewById(R.id.nameTextView);
        holder.addFriendButton = (ImageButton) view.findViewById(R.id.addFriendButton);
        return holder;
    }

    public interface UserListListener {

        void onUserSelected(int position);
    }

    private static class ViewHolder {

        public ImageView avatarImageView;
        public TextView fullnameTextView;
        public ImageButton addFriendButton;
    }
}