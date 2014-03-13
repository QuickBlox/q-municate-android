package com.quickblox.qmunicate.ui.invitefriends;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.model.InviteFriend;

import java.util.ArrayList;
import java.util.List;

public class InviteFriendsAdapter extends ArrayAdapter<InviteFriend> {
    private Context context;
    private CounterChangedListener counterChangedListener;
    private String selectedFriendFromFacebook;
    private String selectedFriendFromContacts;
    private int counterFacebook = 0;

    public InviteFriendsAdapter(Context context, int textViewResourceId, ArrayList<InviteFriend> list) {
        super(context, textViewResourceId, list);
        this.context = context;
        selectedFriendFromFacebook = context.getResources().getString(R.string.stg_invite_friends_from_facebook);
        selectedFriendFromContacts = context.getResources().getString(R.string.stg_invite_friends_from_contacts);
    }

    public void setCounterChangedListener(CounterChangedListener listener) {
        counterChangedListener = listener;
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        final InviteFriend data = getItem(position);

        if (convertView == null) {
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.list_item_invite_friend, null);
            holder = new ViewHolder();

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

        return convertView;
    }

    public int getCounterFacebook() {
        return counterFacebook;
    }

    public void setCounterFacebook(int counterFacebook) {
        this.counterFacebook = counterFacebook;
    }

    private void notifyCounterChanged(boolean isIncrease, int type) {
        switch (type) {
            case InviteFriend.VIA_FACEBOOK_TYPE:
                counterFacebook = getChangedCounter(isIncrease, counterFacebook);
                counterChangedListener.onCounterFacebookChanged(counterFacebook);
                break;
            case InviteFriend.VIA_CONTACTS_TYPE:
                counterChangedListener.onCounterContactsChanged(0);
                break;
        }
    }

    private int getChangedCounter(boolean isIncrease, int counter) {
        if(isIncrease) {
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

    private class ViewHolder {
        TextView nameTextView;
        TextView viaTextView;
        CheckBox checkBox;
    }
}