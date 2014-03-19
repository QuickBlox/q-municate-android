package com.quickblox.qmunicate.ui.main;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.ui.base.BaseActivity;
import com.quickblox.qmunicate.ui.base.BaseListAdapter;

import java.util.ArrayList;
import java.util.List;

public class FriendListAdapter extends BaseListAdapter<Friend> implements Filterable {

    private final LayoutInflater inflater;
    private List<Friend> originalObjects;
    private FriendListAdapter.FriendListFilter filter;

    public FriendListAdapter(BaseActivity activity, List<Friend> objects) {
        super(activity, objects);
        inflater = LayoutInflater.from(activity);
        filter = new FriendListFilter();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        Friend friend = objects.get(position);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_friend, null);
            holder = createViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (null != friend.getFileId()) {
            displayImage(friend.getFileId(), holder.avatarImageView);
        }
        holder.fullnameTextView.setText(friend.getFullname());
        holder.statusTextView.setText(friend.getOnlineStatus());
        if (friend.isOnline()) {
            holder.onlineImageView.setVisibility(View.VISIBLE);
        } else {
            holder.onlineImageView.setVisibility(View.INVISIBLE);
        }

        return convertView;
    }

    private ViewHolder createViewHolder(View view) {
        ViewHolder holder = new ViewHolder();
        holder.avatarImageView = (ImageView) view.findViewById(R.id.avatarImageView);
        holder.fullnameTextView = (TextView) view.findViewById(R.id.nameTextView);
        holder.statusTextView = (TextView) view.findViewById(R.id.statusTextView);
        holder.onlineImageView = (ImageView) view.findViewById(R.id.onlineImageView);
        return holder;
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
        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            objects = (List<Friend>) results.values;
            notifyDataSetChanged();
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            List<Friend> filteredList = new ArrayList<Friend>();

            if (originalObjects == null) {
                originalObjects = new ArrayList<Friend>(objects);
            }

            if (TextUtils.isEmpty(constraint)) {
                results.count = originalObjects.size();
                results.values = originalObjects;
            } else {
                constraint = constraint.toString().toLowerCase();
                for (Friend data : originalObjects) {
                    if (data.getFullname().toLowerCase().startsWith(constraint.toString())) {
                        filteredList.add(data);
                    }
                }
                results.count = filteredList.size();
                results.values = filteredList;
            }
            return results;
        }
    }
}
