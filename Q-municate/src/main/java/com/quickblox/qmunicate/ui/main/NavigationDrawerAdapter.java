package com.quickblox.qmunicate.ui.main;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.ui.base.BaseActivity;
import com.quickblox.qmunicate.ui.base.BaseListAdapter;
import com.quickblox.qmunicate.utils.Consts;

import java.util.List;

public class NavigationDrawerAdapter extends BaseListAdapter<String> implements NavigationDrawerFragment.UpdateCountUnreadChatsDialogsListener {

    private TextView counterUnreadChatsDialogs;

    public NavigationDrawerAdapter(BaseActivity activity, List<String> objects) {
        super(activity, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        final String data = getItem(position);
        String chatItem = resources.getStringArray(
                R.array.nvd_items_array)[MainActivity.ID_CHATS_LIST_FRAGMENT];

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.list_item_navigation_drawer, null);
            holder = new ViewHolder();
            holder.nameTextView = (TextView) convertView.findViewById(R.id.name_textview);
            holder.unreadMessagesTextView = (TextView) convertView.findViewById(
                    R.id.unread_messages_textview);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (data.equals(chatItem)) {
            counterUnreadChatsDialogs = holder.unreadMessagesTextView;
        }

        holder.nameTextView.setText(data);

        return convertView;
    }

    @Override
    public void onUpdateCountUnreadChatsDialogs(int count) {
        if (count > Consts.ZERO_VALUE) {
            counterUnreadChatsDialogs.setVisibility(View.VISIBLE);
            counterUnreadChatsDialogs.setText(count + Consts.EMPTY_STRING);
        }
    }

    private static class ViewHolder {

        TextView nameTextView;
        TextView unreadMessagesTextView;
    }
}