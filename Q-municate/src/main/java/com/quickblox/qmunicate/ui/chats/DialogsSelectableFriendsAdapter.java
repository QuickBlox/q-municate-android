package com.quickblox.qmunicate.ui.chats;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.caching.DatabaseManager;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.ui.base.BaseCursorAdapter;
import com.quickblox.qmunicate.ui.views.RoundedImageView;

import java.util.ArrayList;
import java.util.List;

public class DialogsSelectableFriendsAdapter extends BaseCursorAdapter {

    private LayoutInflater layoutInflater;
    private NewDialogCounterFriendsListener counterChangedListener;
    private int counterFriends;
    private List<Friend> selectedFriends;

    public DialogsSelectableFriendsAdapter(Context context, Cursor cursor) {
        super(context, cursor, true);
        selectedFriends = new ArrayList<Friend>();
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setCounterChangedListener(NewDialogCounterFriendsListener listener) {
        counterChangedListener = listener;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        ViewHolder viewHolder;
        View view = layoutInflater.inflate(R.layout.list_item_chat_friend_selectable, null);
        viewHolder = new ViewHolder();

        viewHolder.contentRelativeLayout = (RelativeLayout) view.findViewById(R.id.contentRelativeLayout);
        viewHolder.avatarImageView = (RoundedImageView) view.findViewById(R.id.avatar_imageview);
        viewHolder.avatarImageView.setOval(true);
        viewHolder.nameTextView = (TextView) view.findViewById(R.id.name_textview);
        viewHolder.onlineImageView = (ImageView) view.findViewById(R.id.online_imageview);
        viewHolder.statusMessageTextView = (TextView) view.findViewById(R.id.statusMessageTextView);
        viewHolder.selectFriendCheckBox = (CheckBox) view.findViewById(R.id.selected_friend_checkbox);

        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        final ViewHolder viewHolder = (ViewHolder) view.getTag();

        Friend friend = DatabaseManager.getFriendFromCursor(cursor);

        viewHolder.nameTextView.setText(friend.getFullname());
        viewHolder.statusMessageTextView.setText(friend.getStatus());
        viewHolder.nameTextView.setText(friend.getFullname());
        viewHolder.selectFriendCheckBox.setChecked(friend.isSelected());
        viewHolder.selectFriendCheckBox.setTag(friend);
        if (friend.isOnline()) {
            viewHolder.onlineImageView.setVisibility(View.VISIBLE);
        } else {
            viewHolder.onlineImageView.setVisibility(View.GONE);
        }
        viewHolder.selectFriendCheckBox.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                CheckBox checkBox = (CheckBox) view;
                Friend friend = (Friend) checkBox.getTag();
                friend.setSelected(checkBox.isChecked());
                notifyCounterChanged(checkBox.isChecked());
                if (checkBox.isChecked()) {
                    selectedFriends.add(friend);
                } else if (selectedFriends.contains(friend)) {
                    selectedFriends.remove(friend);
                }
                viewHolder.contentRelativeLayout.setBackgroundColor(getBackgroundColorItem(
                        viewHolder.selectFriendCheckBox.isChecked()));
            }
        });

        String avatarUrl = getAvatarUrlForFriend(friend);
        displayAvatarImage(avatarUrl, viewHolder.avatarImageView);

        viewHolder.contentRelativeLayout.setBackgroundColor(getBackgroundColorItem(
                viewHolder.selectFriendCheckBox.isChecked()));
    }

    private void notifyCounterChanged(boolean isIncrease) {
        changeCounter(isIncrease);
        counterChangedListener.onCounterFriendsChanged(counterFriends);
    }

    private int getBackgroundColorItem(boolean isSelect) {
        return isSelect ? resources.getColor(R.color.list_item_background_pressed_color) : resources.getColor(
                R.color.white);
    }

    private void changeCounter(boolean isIncrease) {
        if (isIncrease) {
            counterFriends++;
        } else {
            counterFriends--;
        }
    }

    public ArrayList<Friend> getSelectedFriends() {
        return (ArrayList<Friend>) selectedFriends;
    }

    private static class ViewHolder {

        RelativeLayout contentRelativeLayout;
        RoundedImageView avatarImageView;
        TextView nameTextView;
        ImageView onlineImageView;
        TextView statusMessageTextView;
        CheckBox selectFriendCheckBox;
    }
}