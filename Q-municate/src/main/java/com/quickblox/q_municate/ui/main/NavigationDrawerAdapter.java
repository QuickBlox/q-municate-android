package com.quickblox.q_municate.ui.main;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.base.BaseActivity;
import com.quickblox.q_municate.ui.base.BaseListAdapter;
import com.quickblox.q_municate.utils.Consts;

import java.util.List;

public class NavigationDrawerAdapter extends BaseListAdapter<String> implements NavigationDrawerFragment.NavigationDrawerCounterListener {

    private TextView counterUnreadChatsDialogsTextView;
    private TextView counterContactRequestsTextView;

    public NavigationDrawerAdapter(BaseActivity activity, List<String> objects) {
        super(activity, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        final String data = getItem(position);

        String chatItem = resources.getStringArray(
                R.array.nvd_items_array)[MainActivity.ID_CHATS_LIST_FRAGMENT];
        String contactsItem = resources.getStringArray(
                R.array.nvd_items_array)[MainActivity.ID_CONTACTS_LIST_FRAGMENT];

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.list_item_navigation_drawer, null);
            viewHolder = new ViewHolder();
            viewHolder.nameTextView = (TextView) convertView.findViewById(R.id.name_textview);
            viewHolder.counterTextView = (TextView) convertView.findViewById(
                    R.id.counter_textview);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (data.equals(chatItem)) {
            counterUnreadChatsDialogsTextView = viewHolder.counterTextView;
        }

        if (data.equals(contactsItem)) {
            counterContactRequestsTextView = viewHolder.counterTextView;
        }

        viewHolder.nameTextView.setText(data);

        return convertView;
    }

    @Override
    public void onUpdateCountUnreadDialogs(int count) {
        updateCounter(counterUnreadChatsDialogsTextView, count);
    }

    @Override
    public void onUpdateCountContactRequests(int count) {
        updateCounter(counterContactRequestsTextView, count);
    }

    private void updateCounter(TextView counterTextView, int count) {
        if (count > Consts.ZERO_INT_VALUE) {
            counterTextView.setVisibility(View.VISIBLE);
            counterTextView.setText(count + Consts.EMPTY_STRING);
        } else {
            counterTextView.setVisibility(View.GONE);
        }
    }

    private static class ViewHolder {

        TextView nameTextView;
        TextView counterTextView;
    }
}