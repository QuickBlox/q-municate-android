package com.quickblox.qmunicate.ui.main;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.ui.base.BaseActivity;
import com.quickblox.qmunicate.ui.base.BaseListAdapter;

import java.util.List;

public class NavigationDrawerAdapter extends BaseListAdapter<String> {

    public NavigationDrawerAdapter(BaseActivity activity, List<String> objects) {
        super(activity, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        final String data = getItem(position);

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.list_item_navigation_drawer, null);
            holder = new ViewHolder();
            holder.nameTextView = (TextView) convertView.findViewById(R.id.name_textview);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.nameTextView.setText(data);

        return convertView;
    }

    private static class ViewHolder {

        TextView nameTextView;
    }
}