package com.quickblox.qmunicate.ui.invitefriends;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.model.InviteFriend;
import com.quickblox.qmunicate.ui.base.BaseActivity;
import com.quickblox.qmunicate.ui.base.BaseListAdapter;

import java.util.List;

public class InviteFriendsAdapter extends BaseListAdapter<InviteFriend> {

    private LayoutInflater layoutInflater;
    private CounterChangedListener counterChangedListener;
    private String selectedFriendFromFacebook;
    private String selectedFriendFromContacts;
    private int counterFacebook;
    private int counterContacts;

    public InviteFriendsAdapter(BaseActivity activity, List<InviteFriend> objects) {
        super(activity, objects);
        layoutInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
        ViewHolder holder = null;
        final InviteFriend data = getItem(position);

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.list_item_invite_friend, null);
            holder = new ViewHolder();

            holder.avatarImageView = (ImageView) convertView.findViewById(R.id.avatarImageView);
            holder.nameTextView = (TextView) convertView.findViewById(R.id.nameTextView);
            holder.viaTextView = (TextView) convertView.findViewById(R.id.viaTextView);
            holder.checkBox = (CheckBox) convertView.findViewById(R.id.selectUserCheckBox);

            convertView.setTag(holder);

            holder.checkBox.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    CheckBox cb = (CheckBox) v;
                    InviteFriend inviteFriend = (InviteFriend) cb.getTag();
                    inviteFriend.setSelected(cb.isChecked());
                    notifyCounterChanged(cb.isChecked(), inviteFriend.getViaLabelType());
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
            //holder.avatarImageView.setImageURI(data.getUri());
        } else if (data.getViaLabelType() == InviteFriend.VIA_FACEBOOK_TYPE) {
            uri = String.format(activity.getString(R.string.inf_url_to_facebook_avatar), data.getId());

        }
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

    private int getChangedCounter(boolean isIncrease, int counter) {
        if (isIncrease) {
            counter++;
        } else {
            counter--;
        }
        return counter;
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

    private static class ViewHolder {
        ImageView avatarImageView;
        TextView nameTextView;
        TextView viaTextView;
        CheckBox checkBox;
    }
}