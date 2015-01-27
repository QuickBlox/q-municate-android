package com.quickblox.q_municate.ui.chats;

import android.content.Context;
import android.database.Cursor;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.chats.dialogs.NewDialogCounterFriendsListener;
import com.quickblox.q_municate_core.db.managers.UsersDatabaseManager;
import com.quickblox.q_municate_core.models.User;
import com.quickblox.q_municate.ui.base.BaseCursorAdapter;
import com.quickblox.q_municate.ui.views.RoundedImageView;

import java.util.ArrayList;
import java.util.List;

public class DialogsSelectableFriendsAdapter extends BaseCursorAdapter {

    private LayoutInflater layoutInflater;
    private NewDialogCounterFriendsListener counterChangedListener;
    private int counterFriends;
    private List<User> selectedFriends;
    private SparseBooleanArray sparseArrayCheckBoxes;

    public DialogsSelectableFriendsAdapter(Context context, Cursor cursor) {
        super(context, cursor, true);
        selectedFriends = new ArrayList<User>();
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        sparseArrayCheckBoxes = new SparseBooleanArray(cursor.getCount());
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
        viewHolder.nameTextView = (TextView) view.findViewById(R.id.name_textview);
        viewHolder.onlineImageView = (ImageView) view.findViewById(R.id.online_imageview);
        viewHolder.statusMessageTextView = (TextView) view.findViewById(R.id.status_textview);
        viewHolder.selectFriendCheckBox = (CheckBox) view.findViewById(R.id.selected_friend_checkbox);

        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, final Cursor cursor) {
        final ViewHolder viewHolder = (ViewHolder) view.getTag();

        final User friend = UsersDatabaseManager.getUserFromCursor(cursor);

        viewHolder.nameTextView.setText(friend.getFullName());
        viewHolder.statusMessageTextView.setText(friend.getOnlineStatus(context));
        viewHolder.nameTextView.setText(friend.getFullName());

        if (friend.isOnline()) {
            viewHolder.onlineImageView.setVisibility(View.VISIBLE);
        } else {
            viewHolder.onlineImageView.setVisibility(View.GONE);
        }

        final int position = cursor.getPosition();

        viewHolder.selectFriendCheckBox.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                CheckBox checkBox = (CheckBox) view;
                sparseArrayCheckBoxes.put(position, checkBox.isChecked());
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

        boolean checked = sparseArrayCheckBoxes.get(position);

        viewHolder.selectFriendCheckBox.setChecked(checked);

        String avatarUrl = getAvatarUrlForFriend(friend);
        displayAvatarImage(avatarUrl, viewHolder.avatarImageView);

        viewHolder.contentRelativeLayout.setBackgroundColor(getBackgroundColorItem(checked));
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

    public ArrayList<User> getSelectedFriends() {
        return (ArrayList<User>) selectedFriends;
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