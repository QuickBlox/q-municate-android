package com.quickblox.qmunicate.ui.main;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.quickblox.internal.core.exception.BaseServiceException;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.caching.tables.FriendTable;
import com.quickblox.qmunicate.ui.base.BaseCursorAdapter;
import com.quickblox.qmunicate.utils.ErrorUtils;
import com.quickblox.qmunicate.utils.TextViewHelper;
import com.quickblox.qmunicate.utils.UriCreator;

public class FriendsListCursorAdapter extends BaseCursorAdapter {

    private String searchCharacters;

    public FriendsListCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, true);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = layoutInflater.inflate(R.layout.list_item_friend, null, true);

        ViewHolder holder = new ViewHolder();

        holder.avatarImageView = (ImageView) view.findViewById(R.id.avatarImageView);
        holder.fullnameTextView = (TextView) view.findViewById(R.id.nameTextView);
        holder.statusTextView = (TextView) view.findViewById(R.id.statusTextView);
        holder.onlineImageView = (ImageView) view.findViewById(R.id.onlineImageView);

        view.setTag(holder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();

        String fullname = cursor.getString(cursor.getColumnIndex(FriendTable.Cols.FULLNAME));
        String avatarUid = cursor.getString(cursor.getColumnIndex(FriendTable.Cols.AVATAR_UID));
        String status = cursor.getString(cursor.getColumnIndex(FriendTable.Cols.STATUS));
        boolean online = cursor.getInt(cursor.getColumnIndex(FriendTable.Cols.ONLINE)) > 0;

        String url = null;
        if (null != avatarUid) {
            try {
                url = UriCreator.getUri(avatarUid);
            } catch (BaseServiceException e) {
                ErrorUtils.showError(context, e);
            }
        }

        displayImage(url, holder.avatarImageView);
        holder.fullnameTextView.setText(fullname);
        holder.statusTextView.setText(status);
        if (online) {
            holder.onlineImageView.setVisibility(View.VISIBLE);
        } else {
            holder.onlineImageView.setVisibility(View.INVISIBLE);
        }

        if (!TextUtils.isEmpty(searchCharacters)) {
            TextViewHelper.changeTextColorView(context, holder.fullnameTextView, searchCharacters);
        }
    }

    public void setSearchCharacters(String searchCharacters) {
        this.searchCharacters = searchCharacters;
    }

    private static class ViewHolder {

        public ImageView avatarImageView;
        public TextView fullnameTextView;
        public TextView statusTextView;
        public ImageView onlineImageView;
    }
}