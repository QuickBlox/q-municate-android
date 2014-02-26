package com.quickblox.qmunicate.ui.main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.ui.base.BaseListAdapter;

import java.util.List;

public class FriendListAdapter extends BaseListAdapter<Friend> {

    public FriendListAdapter(Context context, int resource, int textViewResourceId, List<Friend> objects) {
        super(context, resource, textViewResourceId, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        Friend friend = getItem(position);
        LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = vi.inflate(R.layout.list_item_friend, null);
            holder = createViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        // TODO add image loading
        // holder.avatarImageView.setImageBitmap();
        holder.fullnameTextView.setText(friend.getFullname());
        holder.statusTextView.setText(friend.getStatus());
        if (friend.isOnline()) {
            holder.onlineImageView.setVisibility(View.VISIBLE);
        } else {
            holder.onlineImageView.setVisibility(View.INVISIBLE);
        }

        return convertView;
    }

    private ViewHolder createViewHolder(View v) {
        ViewHolder holder = new ViewHolder();
        holder.avatarImageView = (ImageView) v.findViewById(R.id.avatarImageView);
        holder.fullnameTextView = (TextView) v.findViewById(R.id.nameTextView);
        holder.statusTextView = (TextView) v.findViewById(R.id.statusTextView);
        holder.onlineImageView = (ImageView) v.findViewById(R.id.onlineImageView);
        return holder;
    }

    private static class ViewHolder {
        public ImageView avatarImageView;
        public TextView fullnameTextView;
        public TextView statusTextView;
        public ImageView onlineImageView;
    }
}
