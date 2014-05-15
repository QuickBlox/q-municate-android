package com.quickblox.qmunicate.ui.chats;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.quickblox.internal.core.helper.Lo;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.caching.DatabaseManager;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.ui.base.BaseCursorAdapter;
import com.quickblox.qmunicate.ui.views.RoundedImageView;
import com.quickblox.qmunicate.utils.TextViewHelper;

import java.util.ArrayList;
import java.util.List;

public class ChatSelectableFriendsAdapter extends BaseCursorAdapter {
    private Context context;
    private LayoutInflater layoutInflater;
    private NewChatCounterFriendsListener counterChangedListener;
    private int counterFriends;
//    private String searchCharacters;
    private List<Friend> selectedFriends;

    public ChatSelectableFriendsAdapter(Context context, Cursor cursor) {
        super(context, cursor, true);
        this.context = context;
        selectedFriends = new ArrayList<Friend>();
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setCounterChangedListener(NewChatCounterFriendsListener listener) {
        counterChangedListener = listener;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        ViewHolder holder;
        View view = layoutInflater.inflate(R.layout.list_item_chat_friend_selectable, null);
        holder = new ViewHolder();

        holder.avatarImageView = (RoundedImageView) view.findViewById(R.id.avatar_imageview);
        holder.avatarImageView.setOval(true);
        holder.nameTextView = (TextView) view.findViewById(R.id.name_textview);
        holder.onlineImageView = (ImageView) view.findViewById(R.id.online_imageview);
        holder.statusMessageTextView = (TextView) view.findViewById(R.id.statusMessageTextView);
        holder.selectFriendCheckBox = (CheckBox) view.findViewById(R.id.time_textview);

        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();

        Friend friend = DatabaseManager.getFriendFromCursor(cursor);

        holder.nameTextView.setText(friend.getFullname());
        holder.statusMessageTextView.setText(friend.getStatus());
        // TODO All fields
        holder.nameTextView.setText(friend.getFullname());
        holder.selectFriendCheckBox.setChecked(friend.isSelected());
        holder.selectFriendCheckBox.setTag(friend);
        if (friend.isOnline()) {
            holder.onlineImageView.setVisibility(View.VISIBLE);
        } else {
            holder.onlineImageView.setVisibility(View.INVISIBLE);
        }
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
        String avatarUrl = getAvatarUrlForFriend(friend);
        displayImage(avatarUrl, holder.avatarImageView);

//        if (!TextUtils.isEmpty(searchCharacters)) {
//            TextViewHelper.changeTextColorView(context, holder.nameTextView, searchCharacters);
//        }
    }

//    public void setSearchCharacters(String searchCharacters) {
//        this.searchCharacters = searchCharacters;
//    }

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