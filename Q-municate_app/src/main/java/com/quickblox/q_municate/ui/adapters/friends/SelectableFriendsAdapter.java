package com.quickblox.q_municate.ui.adapters.friends;

import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RelativeLayout;

import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.activities.base.BaseActivity;
import com.quickblox.q_municate.ui.adapters.base.BaseClickListenerViewHolder;
import com.quickblox.q_municate.utils.listeners.SelectUsersListener;
import com.quickblox.q_municate_core.utils.ChatUtils;
import com.quickblox.q_municate_db.models.User;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;

public class SelectableFriendsAdapter extends FriendsAdapter {

    private SelectUsersListener selectUsersListener;
    private int counterFriends;
    private List<User> selectedFriendsList;
    private SparseBooleanArray sparseArrayCheckBoxes;

    public SelectableFriendsAdapter(BaseActivity baseActivity, List<User> userList, boolean withFirstLetter) {
        super(baseActivity, userList, withFirstLetter);
        selectedFriendsList = new ArrayList<User>();
        sparseArrayCheckBoxes = new SparseBooleanArray(userList.size());
    }

    public void setSelectUsersListener(SelectUsersListener listener) {
        selectUsersListener = listener;
    }

    @Override
    public BaseClickListenerViewHolder<User> onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(this, layoutInflater.inflate(R.layout.item_friend_selectable, parent, false));
    }

    @Override
    public void onBindViewHolder(BaseClickListenerViewHolder<User> baseClickListenerViewHolder, final int position) {
        super.onBindViewHolder(baseClickListenerViewHolder, position);

        final User user = getItem(position);
        final ViewHolder viewHolder = (ViewHolder) baseClickListenerViewHolder;

        viewHolder.selectFriendCheckBox.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                CheckBox checkBox = (CheckBox) view;
                sparseArrayCheckBoxes.put(position, checkBox.isChecked());
                if (checkBox.isChecked()) {
                    selectedFriendsList.add(user);
                } else if (selectedFriendsList.contains(user)) {
                    selectedFriendsList.remove(user);
                }

                notifyCounterChanged(checkBox.isChecked());

                viewHolder.contentRelativeLayout.setBackgroundColor(
                        getBackgroundColorItem(viewHolder.selectFriendCheckBox.isChecked()));
            }
        });

        boolean checked = sparseArrayCheckBoxes.get(position);
        viewHolder.selectFriendCheckBox.setChecked(checked);

        viewHolder.contentRelativeLayout.setBackgroundColor(getBackgroundColorItem(checked));
    }

    private void notifyCounterChanged(boolean isIncrease) {
        changeCounter(isIncrease);
        if (selectUsersListener != null) {
            String fullNames = ChatUtils.getSelectedFriendsFullNamesFromMap(selectedFriendsList);
            selectUsersListener.onSelectedUsersChanged(counterFriends, fullNames);
        }
    }

    private int getBackgroundColorItem(boolean isSelect) {
        return isSelect ? resources.getColor(R.color.button_general_pressed) : resources.getColor(R.color.white);
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
        selectedFriendsList.clear();
        counterFriends = 0;
        notifyDataSetChanged();
    }

    public List<User> getSelectedFriendsList() {
        return selectedFriendsList;
    }

    protected static class ViewHolder extends FriendsAdapter.ViewHolder {

        @Bind(R.id.contentRelativeLayout)
        RelativeLayout contentRelativeLayout;

        @Bind(R.id.selected_friend_checkbox)
        CheckBox selectFriendCheckBox;

        public ViewHolder(FriendsAdapter adapter, View view) {
            super(adapter, view);
        }
    }
}