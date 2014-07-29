package com.quickblox.q_municate.ui.main;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.base.BaseActivity;
import com.quickblox.q_municate.ui.base.BaseListAdapter;
import com.quickblox.q_municate.utils.Consts;

import java.util.List;

public class NavigationDrawerAdapter extends BaseListAdapter<String> implements NavigationDrawerFragment.UpdateCountUnreadDialogsListener {

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
    public void onUpdateCountUnreadDialogs(int count) {
        if (count > Consts.ZERO_INT_VALUE) {
            counterUnreadChatsDialogs.setVisibility(View.VISIBLE);
            counterUnreadChatsDialogs.setText(count + Consts.EMPTY_STRING);
        } else {
            counterUnreadChatsDialogs.setVisibility(View.GONE);
        }
    }

    private static class ViewHolder {

        TextView nameTextView;
        TextView unreadMessagesTextView;
    }
}