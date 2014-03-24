package com.quickblox.qmunicate.ui.chats.smiles;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.quickblox.qmunicate.R;

import java.util.Collections;
import java.util.List;

public class SmilesAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private List<Integer> resources;

    public SmilesAdapter(Context context) {
        inflater = LayoutInflater.from(context);
        resources = Collections.emptyList();
    }

    @Override
    public int getCount() {
        return resources.size();
    }

    @Override
    public Object getItem(int position) {
        return resources.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SmileHolder holder;
        if (convertView == null) {
            holder = new SmileHolder();
            convertView = inflater.inflate(R.layout.smile_view, parent, false);
            holder.smile = (ImageView) convertView.findViewById(R.id.smile);
            convertView.setTag(holder);
        } else {
            holder = (SmileHolder) convertView.getTag();
        }
        holder.smile.setImageResource(resources.get(position));
        return convertView;
    }

    public void setResources(List<Integer> resources) {
        this.resources = resources;
    }

    private class SmileHolder {
        ImageView smile;
    }
}
