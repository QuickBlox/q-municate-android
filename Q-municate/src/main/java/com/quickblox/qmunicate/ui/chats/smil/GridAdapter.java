package com.quickblox.qmunicate.ui.chats.smil;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.quickblox.qmunicate.R;

public class GridAdapter extends BaseAdapter {

    Context context;

    private String[] items;
    private LayoutInflater mInflater;
    public GridAdapter(Context context, String[] locations) {

        mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.context = context;
        items = locations;
    }

    @Override
    public int getCount() {
        if (items != null) {
            return items.length;
        }
        return 0;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    @Override
    public Object getItem(int position) {
        if (items != null && position >= 0 && position < getCount()) {
            return items[position];
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public void setItemsList(String[] locations) {
        this.items = locations;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ViewHolder viewHolder;

        if (view == null) {
            view = mInflater.inflate(R.layout.list_item_smile, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.textTitle = (TextView) view.findViewById(R.id.smile_item_textview);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        String gridItem = items[position];
        viewHolder.textTitle.setText(Emoji.ensure(gridItem, context) + "");

        return view;
    }

    public class ViewHolder {
        public TextView textTitle;
    }
}