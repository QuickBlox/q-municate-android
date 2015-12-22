package com.quickblox.q_municate.ui.adapters.invitefriends;

import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.quickblox.q_municate.R;
import com.quickblox.q_municate.utils.listeners.CounterChangedListener;
import com.quickblox.q_municate_core.models.InviteFriend;
import com.quickblox.q_municate.ui.activities.base.BaseActivity;
import com.quickblox.q_municate.ui.adapters.base.BaseListAdapter;
import com.quickblox.q_municate.ui.views.roundedimageview.RoundedImageView;

import java.util.List;

public class InviteFriendsAdapter extends BaseListAdapter<InviteFriend> {

    private CounterChangedListener counterChangedListener;
    private int counterContacts;

    public InviteFriendsAdapter(BaseActivity activity, List<InviteFriend> objects) {
        super(activity, objects);
    }

    public void setCounterChangedListener(CounterChangedListener listener) {
        counterChangedListener = listener;
    }

    public void setCounterContacts(int counterContacts) {
        this.counterContacts = counterContacts;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        final InviteFriend data = getItem(position);

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.item_invite_friend, null);
            viewHolder = new ViewHolder();

            viewHolder.contentRelativeLayout = (RelativeLayout) convertView.findViewById(
                    R.id.contentRelativeLayout);
            viewHolder.avatarImageView = (RoundedImageView) convertView.findViewById(R.id.avatar_imageview);
            viewHolder.nameTextView = (TextView) convertView.findViewById(R.id.name_textview);
            viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.select_user_checkbox);

            convertView.setTag(viewHolder);

            viewHolder.checkBox.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    CheckBox checkBox = (CheckBox) view;
                    InviteFriend inviteFriend = (InviteFriend) checkBox.getTag();
                    inviteFriend.setSelected(checkBox.isChecked());
                    notifyCounterChanged(checkBox.isChecked());
                }
            });
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.nameTextView.setText(data.getName());
        viewHolder.checkBox.setChecked(data.isSelected());
        viewHolder.checkBox.setTag(data);

        String uri = data.getUri().toString();

        displayAvatarImage(uri, viewHolder.avatarImageView);
        return convertView;
    }

    private void notifyCounterChanged(boolean isIncrease) {
        counterContacts = getChangedCounter(isIncrease, counterContacts);
        counterChangedListener.onCounterContactsChanged(counterContacts);
    }

    private int getChangedCounter(boolean isIncrease, int counter) {
        if (isIncrease) {
            counter++;
        } else {
            counter--;
        }
        return counter;
    }

    private static class ViewHolder {

        RelativeLayout contentRelativeLayout;
        RoundedImageView avatarImageView;
        TextView nameTextView;
        CheckBox checkBox;
    }
}