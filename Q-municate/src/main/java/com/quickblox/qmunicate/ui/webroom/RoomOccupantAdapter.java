package com.quickblox.qmunicate.ui.webroom;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.quickblox.internal.core.exception.BaseServiceException;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.ui.base.BaseActivity;
import com.quickblox.qmunicate.ui.base.BaseListAdapter;
import com.quickblox.qmunicate.utils.ErrorUtils;
import com.quickblox.qmunicate.utils.UriCreator;

import java.util.List;

public class RoomOccupantAdapter extends BaseListAdapter<Friend> {

    public RoomOccupantAdapter(BaseActivity activity, List<Friend> objects) {
        super(activity, objects);
        layoutInflater = LayoutInflater.from(activity);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        Friend friend = objectsList.get(position);

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.list_item_friend, null);
            holder = createViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        String url = null;
        if (null != friend.getAvatarUid()) {
            try {
                url = UriCreator.getUri(friend.getAvatarUid());
            } catch (BaseServiceException e) {
                ErrorUtils.showError(baseActivity, e);
            }
        }
        displayImage(url, holder.avatarImageView);
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
        holder.avatarImageView = (ImageView) view.findViewById(R.id.avatar_imageview);
        holder.fullnameTextView = (TextView) view.findViewById(R.id.name_textview);
        holder.statusTextView = (TextView) view.findViewById(R.id.status_textview);
        holder.onlineImageView = (ImageView) view.findViewById(R.id.online_imageview);
        return holder;
    }

    private static class ViewHolder {

        public ImageView avatarImageView;
        public TextView fullnameTextView;
        public TextView statusTextView;
        public ImageView onlineImageView;
    }
}
