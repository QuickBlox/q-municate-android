package com.quickblox.q_municate.ui.chats.dialogslist;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.base.BaseActivity;
import com.quickblox.q_municate.ui.base.BaseListAdapter;
import com.quickblox.q_municate.core.listeners.NewDialogCounterFriendsListener;
import com.quickblox.q_municate.ui.views.RoundedImageView;
import com.quickblox.q_municate_core.utils.OnlineStatusHelper;
import com.quickblox.q_municate_db.models.User;

import java.util.ArrayList;
import java.util.List;

public class DialogsSelectableFriendsAdapter extends BaseListAdapter<User> {

    private LayoutInflater layoutInflater;
    private NewDialogCounterFriendsListener counterChangedListener;
    private int counterFriends;
    private List<User> selectedFriends;
    private SparseBooleanArray sparseArrayCheckBoxes;

    public DialogsSelectableFriendsAdapter(BaseActivity context, List<User> userList) {
        super(context, userList);
        selectedFriends = new ArrayList<User>();
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        sparseArrayCheckBoxes = new SparseBooleanArray(userList.size());
    }

    public void setCounterChangedListener(NewDialogCounterFriendsListener listener) {
        counterChangedListener = listener;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        final User user = getItem(position);

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.list_item_chat_friend_selectable, null);
            viewHolder = new ViewHolder();

            viewHolder.contentRelativeLayout = (RelativeLayout) convertView.findViewById(
                    R.id.contentRelativeLayout);
            viewHolder.avatarImageView = (RoundedImageView) convertView.findViewById(R.id.avatar_imageview);
            viewHolder.nameTextView = (TextView) convertView.findViewById(R.id.name_textview);
            viewHolder.onlineImageView = (ImageView) convertView.findViewById(R.id.online_imageview);
            viewHolder.statusMessageTextView = (TextView) convertView.findViewById(R.id.status_textview);
            viewHolder.selectFriendCheckBox = (CheckBox) convertView.findViewById(
                    R.id.selected_friend_checkbox);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.nameTextView.setText(user.getFullName());
        viewHolder.statusMessageTextView.setText(OnlineStatusHelper.getOnlineStatus(user.isOnline()));
        viewHolder.nameTextView.setText(user.getFullName());

        if (user.isOnline()) {
            viewHolder.onlineImageView.setVisibility(View.VISIBLE);
        } else {
            viewHolder.onlineImageView.setVisibility(View.GONE);
        }

        viewHolder.selectFriendCheckBox.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                CheckBox checkBox = (CheckBox) view;
                sparseArrayCheckBoxes.put(position, checkBox.isChecked());
                //                friend.setSelected(checkBox.isChecked());
                notifyCounterChanged(checkBox.isChecked());
                if (checkBox.isChecked()) {
                    selectedFriends.add(user);
                } else if (selectedFriends.contains(user)) {
                    selectedFriends.remove(user);
                }
                viewHolder.contentRelativeLayout.setBackgroundColor(getBackgroundColorItem(
                        viewHolder.selectFriendCheckBox.isChecked()));
            }
        });

        boolean checked = sparseArrayCheckBoxes.get(position);

        viewHolder.selectFriendCheckBox.setChecked(checked);

        displayAvatarImage(user.getAvatar(), viewHolder.avatarImageView);

        viewHolder.contentRelativeLayout.setBackgroundColor(getBackgroundColorItem(checked));

        return convertView;
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