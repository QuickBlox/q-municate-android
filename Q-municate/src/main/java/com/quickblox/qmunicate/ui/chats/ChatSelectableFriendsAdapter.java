package com.quickblox.qmunicate.ui.chats;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.ui.views.RoundedImageView;

import java.util.ArrayList;
import java.util.List;

public class ChatSelectableFriendsAdapter extends ArrayAdapter<Friend> {
    private Context context;
    private LayoutInflater layoutInflater;
    private NewChatCounterFriendsListener counterChangedListener;
    private int counterFriends;
    private List<Friend> selectedFriends;

    public ChatSelectableFriendsAdapter(Context context, int textViewResourceId, List<Friend> list) {
        super(context, textViewResourceId, list);
        this.context = context;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        selectedFriends = new ArrayList<Friend>();
    }

    public void setCounterChangedListener(NewChatCounterFriendsListener listener) {
        counterChangedListener = listener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        Friend data = getItem(position);

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.list_item_chat_friend_selectable, null);
            holder = new ViewHolder();

            holder.avatarImageView = (RoundedImageView) convertView.findViewById(R.id.avatar_imageview);
            holder.avatarImageView.setOval(true);
            holder.nameTextView = (TextView) convertView.findViewById(R.id.name_textview);
            holder.onlineImageView = (ImageView) convertView.findViewById(R.id.online_imageview);
            holder.statusMessageTextView = (TextView) convertView.findViewById(R.id.statusMessageTextView);
            holder.selectFriendCheckBox = (CheckBox) convertView.findViewById(R.id.time_textview);

            convertView.setTag(holder);

            holder.selectFriendCheckBox.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    CheckBox cb = (CheckBox) v;
                    Friend friend = (Friend) cb.getTag();
                    friend.setSelected(cb.isChecked());
                    notifyCounterChanged(cb.isChecked());
                    if(cb.isChecked()){
                        selectedFriends.add(friend);
                    } else if (selectedFriends.contains(friend)){
                        selectedFriends.remove(friend);
                    }
                }
            });
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // TODO All fields
        holder.nameTextView.setText(data.getEmail());
        holder.selectFriendCheckBox.setChecked(data.isSelected());
        holder.selectFriendCheckBox.setTag(data);

        return convertView;
    }

    private void notifyCounterChanged(boolean isIncrease) {
        changeCounter(isIncrease);
        counterChangedListener.onCounterFriendsChanged(counterFriends);
    }

    public ArrayList<Friend> getSelectedFriends() {
        return (ArrayList<Friend>)selectedFriends;
    }

    private void changeCounter(boolean isIncrease) {
        if (isIncrease) {
            counterFriends++;
        } else {
            counterFriends--;
        }
    }

    private static class ViewHolder {
        RoundedImageView avatarImageView;
        TextView nameTextView;
        ImageView onlineImageView;
        TextView statusMessageTextView;
        CheckBox selectFriendCheckBox;
    }
}