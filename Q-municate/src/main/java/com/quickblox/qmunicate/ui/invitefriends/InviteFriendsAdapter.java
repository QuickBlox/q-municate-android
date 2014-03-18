package com.quickblox.qmunicate.ui.invitefriends;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.model.InviteFriend;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class InviteFriendsAdapter extends ArrayAdapter<InviteFriend> {
    private Context context;
    private LayoutInflater layoutInflater;

    private CounterChangedListener counterChangedListener;
    private String selectedFriendFromFacebook;
    private String selectedFriendFromContacts;
    private int counterFacebook;
    private int counterContacts;

    public InviteFriendsAdapter(Context context, int textViewResourceId, ArrayList<InviteFriend> list) {
        super(context, textViewResourceId, list);
        this.context = context;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        selectedFriendFromFacebook = context.getResources().getString(R.string.stg_invite_friends_from_facebook);
        selectedFriendFromContacts = context.getResources().getString(R.string.stg_invite_friends_from_contacts);
    }

    public void setCounterChangedListener(CounterChangedListener listener) {
        counterChangedListener = listener;
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

        if (data.getViaLabelType() == InviteFriend.VIA_CONTACTS_TYPE) {
            Picasso.with(context)
                    .load(data.getUri())
                    .placeholder(R.drawable.placeholder_user)
                    .into(holder.avatarImageView);
        } else if (data.getViaLabelType() == InviteFriend.VIA_FACEBOOK_TYPE) {
            Picasso.with(context)
                    .load(String.format(context.getResources().getString(R.string.stg_invite_friends_url_to_facebook_avatar), data.getId()))
                    .placeholder(R.drawable.placeholder_user)
                    .into(holder.avatarImageView);
        }

        return convertView;
    }

    public void setCounterFacebook(int counterFacebook) {
        this.counterFacebook = counterFacebook;
    }

    public void setCounterContacts(int counterContacts) {
        this.counterContacts = counterContacts;
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