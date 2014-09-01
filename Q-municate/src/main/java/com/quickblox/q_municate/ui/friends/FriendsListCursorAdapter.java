package com.quickblox.q_municate.ui.friends;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.quickblox.q_municate.R;
import com.quickblox.q_municate.db.DatabaseManager;
import com.quickblox.q_municate.model.Friend;
import com.quickblox.q_municate.ui.base.BaseCursorAdapter;
import com.quickblox.q_municate.ui.views.RoundedImageView;
import com.quickblox.q_municate.utils.TextViewHelper;

public class FriendsListCursorAdapter extends BaseCursorAdapter {

    private String searchCharacters;

    public FriendsListCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, true);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = layoutInflater.inflate(R.layout.list_item_friend, null, true);

        ViewHolder holder = new ViewHolder();

        holder.avatarImageView = (RoundedImageView) view.findViewById(R.id.avatar_imageview);
        holder.fullnameTextView = (TextView) view.findViewById(R.id.name_textview);
        holder.statusTextView = (TextView) view.findViewById(R.id.status_textview);
        holder.onlineImageView = (ImageView) view.findViewById(R.id.online_imageview);

        view.setTag(holder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();

        Friend friend = DatabaseManager.getFriendFromCursor(cursor);

        holder.fullnameTextView.setText(friend.getFullname());
        holder.statusTextView.setText(friend.getOnlineStatus());

        if (friend.isOnline()) {
            holder.onlineImageView.setVisibility(View.VISIBLE);
        } else {
            holder.onlineImageView.setVisibility(View.INVISIBLE);
        }

        String avatarUrl = getAvatarUrlForFriend(friend);
        displayAvatarImage(avatarUrl, holder.avatarImageView);

        if (!TextUtils.isEmpty(searchCharacters)) {
            TextViewHelper.changeTextColorView(context, holder.fullnameTextView, searchCharacters);
        }
    }

    public void setSearchCharacters(String searchCharacters) {
        this.searchCharacters = searchCharacters;
    }

    private static class ViewHolder {

        public RoundedImageView avatarImageView;
        public TextView fullnameTextView;
        public TextView statusTextView;
        public ImageView onlineImageView;
    }
}