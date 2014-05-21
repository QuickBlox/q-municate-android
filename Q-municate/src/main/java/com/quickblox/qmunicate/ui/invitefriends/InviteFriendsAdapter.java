package com.quickblox.qmunicate.ui.invitefriends;

import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.model.InviteFriend;
import com.quickblox.qmunicate.ui.base.BaseActivity;
import com.quickblox.qmunicate.ui.base.BaseListAdapter;
import com.quickblox.qmunicate.ui.views.RoundedImageView;

import java.util.List;

public class InviteFriendsAdapter extends BaseListAdapter<InviteFriend> {

    private CounterChangedListener counterChangedListener;
    private String selectedFriendFromFacebook;
    private String selectedFriendFromContacts;
    private int counterFacebook;
    private int counterContacts;

    public InviteFriendsAdapter(BaseActivity activity, List<InviteFriend> objects) {
        super(activity, objects);
        selectedFriendFromFacebook = activity.getString(R.string.inf_from_facebook);
        selectedFriendFromContacts = activity.getString(R.string.inf_from_contacts);
    }

    public void setCounterChangedListener(CounterChangedListener listener) {
        counterChangedListener = listener;
    }

    public void setCounterFacebook(int counterFacebook) {
        this.counterFacebook = counterFacebook;
    }

    public void setCounterContacts(int counterContacts) {
        this.counterContacts = counterContacts;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        final InviteFriend data = getItem(position);

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.list_item_invite_friend, null);
            holder = new ViewHolder();

            holder.contentRelativeLayout = (RelativeLayout) convertView.findViewById(
                    R.id.contentRelativeLayout);
            holder.avatarImageView = (RoundedImageView) convertView.findViewById(R.id.avatar_imageview);
            holder.avatarImageView.setOval(true);
            holder.nameTextView = (TextView) convertView.findViewById(R.id.name_textview);
            holder.viaTextView = (TextView) convertView.findViewById(R.id.viaTextView);
            holder.checkBox = (CheckBox) convertView.findViewById(R.id.selectUserCheckBox);

            convertView.setTag(holder);

            final ViewHolder finalHolder = holder;
            holder.checkBox.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    CheckBox checkBox = (CheckBox) view;
                    InviteFriend inviteFriend = (InviteFriend) checkBox.getTag();
                    inviteFriend.setSelected(checkBox.isChecked());
                    notifyCounterChanged(checkBox.isChecked(), inviteFriend.getViaLabelType());
                    finalHolder.contentRelativeLayout.setBackgroundColor(getBackgroundColorItem(checkBox.isChecked()));
                }
            });
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.nameTextView.setText(data.getName());
        holder.viaTextView.setText(getViaLabelById(data.getViaLabelType()));
        holder.checkBox.setChecked(data.isSelected());
        holder.checkBox.setTag(data);

        String uri = null;
        if (data.getViaLabelType() == InviteFriend.VIA_CONTACTS_TYPE) {
            uri = data.getUri().toString();
        } else if (data.getViaLabelType() == InviteFriend.VIA_FACEBOOK_TYPE) {
            uri = baseActivity.getString(R.string.inf_url_to_facebook_avatar, data.getId());
        }

        holder.contentRelativeLayout.setBackgroundColor(getBackgroundColorItem(holder.checkBox.isChecked()));

        displayImage(uri, holder.avatarImageView);
        return convertView;
    }

    private void notifyCounterChanged(boolean isIncrease, int type) {
        switch (type) {
            case InviteFriend.VIA_FACEBOOK_TYPE:
                counterFacebook = getChangedCounter(isIncrease, counterFacebook);
                counterChangedListener.onCounterFacebookChanged(counterFacebook);
                break;
            case InviteFriend.VIA_CONTACTS_TYPE:
                counterContacts = getChangedCounter(isIncrease, counterContacts);
                counterChangedListener.onCounterContactsChanged(counterContacts);
                break;
        }
    }

    private String getViaLabelById(int type) {
        String viaLabel = null;
        switch (type) {
            case InviteFriend.VIA_FACEBOOK_TYPE:
                viaLabel = selectedFriendFromFacebook;
                break;
            case InviteFriend.VIA_CONTACTS_TYPE:
                viaLabel = selectedFriendFromContacts;
                break;
        }
        return viaLabel;
    }

    private int getChangedCounter(boolean isIncrease, int counter) {
        if (isIncrease) {
            counter++;
        } else {
            counter--;
        }
        return counter;
    }

    private int getBackgroundColorItem(boolean isSelect) {
        return isSelect ? resources.getColor(R.color.list_item_background_pressed_color) : resources.getColor(
                R.color.white);
    }

    private static class ViewHolder {

        RelativeLayout contentRelativeLayout;
        RoundedImageView avatarImageView;
        TextView nameTextView;
        TextView viaTextView;
        CheckBox checkBox;
    }
}