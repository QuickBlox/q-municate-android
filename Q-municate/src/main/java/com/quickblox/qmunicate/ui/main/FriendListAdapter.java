package com.quickblox.qmunicate.ui.main;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.quickblox.internal.core.exception.BaseServiceException;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.caching.DatabaseManager;
import com.quickblox.qmunicate.caching.tables.FriendTable;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.utils.Consts;
import com.quickblox.qmunicate.utils.ErrorUtils;
import com.quickblox.qmunicate.utils.UriCreator;

import java.util.ArrayList;
import java.util.List;

public class FriendListAdapter extends CursorAdapter implements Filterable {
    private final LayoutInflater layoutInflater;
    private List<Friend> friendsList;
    private FriendListAdapter.FriendListFilter filter;

    public FriendListAdapter(Context context, Cursor cursor) {
        super(context, cursor, true);
        layoutInflater = LayoutInflater.from(context);
        filter = new FriendListFilter();
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
    public Filter getFilter() {
        return filter;
    }

    private static class ViewHolder {

        public ImageView avatarImageView;
        public TextView fullnameTextView;
        public TextView statusTextView;
        public ImageView onlineImageView;
    }

    private class FriendListFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            List<Friend> filteredList = new ArrayList<Friend>();

            friendsList = new ArrayList<Friend>();

            Cursor cursor = getCursor();
            if(cursor != null) {
                cursor.moveToFirst();
                while (cursor.moveToNext()) {
                    friendsList.add(DatabaseManager.getFriendFromCursor(cursor));
                }
            }

            if (TextUtils.isEmpty(constraint)) {
                results.count = friendsList.size();
                results.values = friendsList;
            } else {
                constraint = constraint.toString().toLowerCase();
                for (Friend friend : friendsList) {
                    if (friend.getFullname().toLowerCase().startsWith(constraint.toString())) {
                        filteredList.add(friend);
                    }
                }
                results.count = filteredList.size();
                results.values = filteredList;
            }
            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            friendsList = (List<Friend>) results.values;
            notifyDataSetChanged();
        }
    }

    private void displayImage(String uri, ImageView imageView) {
        ImageLoader.getInstance().displayImage(uri, imageView, Consts.UIL_AVATAR_DISPLAY_OPTIONS);
    }
}