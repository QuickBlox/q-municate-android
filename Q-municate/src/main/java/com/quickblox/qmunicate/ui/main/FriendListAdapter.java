package com.quickblox.qmunicate.ui.main;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.ui.base.BaseListAdapter;

import java.util.ArrayList;
import java.util.List;

public class FriendListAdapter extends BaseListAdapter<Friend> implements Filterable {
    private List<Friend> originalObjects;

    public FriendListAdapter(Activity activity, List<Friend> objects) {
        super(activity, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        Friend friend = objects.get(position);
        LayoutInflater vi = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = vi.inflate(R.layout.list_item_friend, null);
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
        return new FriendListFilter();
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
            List<Friend> FilteredArrList = new ArrayList<Friend>();

            if (originalObjects == null) {
                originalObjects = new ArrayList<Friend>(objects);
            }

            if (constraint == null || constraint.length() == 0) {
                // set the Original result to return
                results.count = originalObjects.size();
                results.values = originalObjects;
            } else {
                constraint = constraint.toString().toLowerCase();
                for (Friend data : originalObjects) {
                    if (data.getFullname().toLowerCase().startsWith(constraint.toString())) {
                        FilteredArrList.add(data);
                    }
                }
                results.count = FilteredArrList.size();
                results.values = FilteredArrList;
            }
            return results;
        }
    }
}
