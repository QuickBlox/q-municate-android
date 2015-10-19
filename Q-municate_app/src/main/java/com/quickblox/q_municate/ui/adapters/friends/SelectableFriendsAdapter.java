package com.quickblox.q_municate.ui.adapters.friends;

import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.activities.base.BaseActivity;
import com.quickblox.q_municate.ui.adapters.base.BaseClickListenerViewHolder;
import com.quickblox.q_municate.ui.adapters.base.BaseRecyclerViewAdapter;
import com.quickblox.q_municate.ui.adapters.base.BaseViewHolder;
import com.quickblox.q_municate.utils.listeners.SelectUsersListener;
import com.quickblox.q_municate.ui.views.roundedimageview.RoundedImageView;
import com.quickblox.q_municate_core.qb.helpers.QBFriendListHelper;
import com.quickblox.q_municate_core.utils.OnlineStatusUtils;
import com.quickblox.q_municate_db.models.User;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;

public class SelectableFriendsAdapter extends BaseRecyclerViewAdapter<User, BaseClickListenerViewHolder<User>> {

    private SelectUsersListener counterChangedListener;
    private int counterFriends;
    private List<User> selectedFriends;
    private SparseBooleanArray sparseArrayCheckBoxes;
    private QBFriendListHelper friendListHelper;

    public SelectableFriendsAdapter(BaseActivity baseActivity, List<User> userList) {
        super(baseActivity, userList);
        selectedFriends = new ArrayList<User>();
        sparseArrayCheckBoxes = new SparseBooleanArray(userList.size());
    }

    public void setCounterChangedListener(SelectUsersListener listener) {
        counterChangedListener = listener;
    }

    public void setFriendListHelper(QBFriendListHelper friendListHelper) {
        this.friendListHelper = friendListHelper;
        notifyDataSetChanged();
    }

    @Override
    public BaseClickListenerViewHolder<User> onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(this, layoutInflater.inflate(R.layout.item_chat_user_selectable, parent, false));
    }

    @Override
    public void onBindViewHolder(BaseClickListenerViewHolder<User> baseClickListenerViewHolder, final int position) {
        final User user = getItem(position);
        final ViewHolder viewHolder = (ViewHolder) baseClickListenerViewHolder;

        viewHolder.nameTextView.setText(user.getFullName());

        checkUserOnlineStatus(viewHolder, user);

        viewHolder.selectFriendCheckBox.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                CheckBox checkBox = (CheckBox) view;
                sparseArrayCheckBoxes.put(position, checkBox.isChecked());
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
    }

    private void checkUserOnlineStatus(ViewHolder viewHolder, User user) {
        if (friendListHelper != null) {
            boolean online = friendListHelper.isUserOnline(user.getUserId());

            if (online) {
                viewHolder.onlineImageView.setVisibility(View.VISIBLE);
            } else {
                viewHolder.onlineImageView.setVisibility(View.GONE);
            }

            viewHolder.statusMessageTextView.setText(OnlineStatusUtils.getOnlineStatus(online));
        }
    }

    private void notifyCounterChanged(boolean isIncrease) {
        changeCounter(isIncrease);
        counterChangedListener.onCounterUsersChanged(counterFriends);
    }

    private int getBackgroundColorItem(boolean isSelect) {
        return isSelect ? resources.getColor(R.color.button_general_pressed) : resources.getColor(
                R.color.white);
    }

    private void changeCounter(boolean isIncrease) {
        if (isIncrease) {
            counterFriends++;
        } else {
            counterFriends--;
        }
    }

    public void clearSelectedFriends() {
        sparseArrayCheckBoxes.clear();
        selectedFriends.clear();
        counterFriends = 0;
        notifyDataSetChanged();
    }

    public ArrayList<User> getSelectedFriends() {
        return (ArrayList<User>) selectedFriends;
    }

    protected static class ViewHolder extends BaseViewHolder<User> {

        @Bind(R.id.contentRelativeLayout)
        RelativeLayout contentRelativeLayout;

        @Bind(R.id.avatar_imageview)
        RoundedImageView avatarImageView;

        @Bind(R.id.name_textview)
        TextView nameTextView;

        @Bind(R.id.online_imageview)
        ImageView onlineImageView;

        @Bind(R.id.status_textview)
        TextView statusMessageTextView;

        @Bind(R.id.selected_friend_checkbox)
        CheckBox selectFriendCheckBox;

        public ViewHolder(BaseRecyclerViewAdapter adapter, View view) {
            super(adapter, view);
        }
    }
}