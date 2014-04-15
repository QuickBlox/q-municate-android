package com.quickblox.qmunicate.ui.main;

import android.content.res.Resources;
import android.graphics.drawable.TransitionDrawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.ui.base.BaseActivity;
import com.quickblox.qmunicate.ui.base.BaseListAdapter;
import com.quickblox.qmunicate.utils.Consts;

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
            holder.nameTextView = (TextView) convertView.findViewById(R.id.nameTextView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.nameTextView.setText(data);

        Resources res = baseActivity.getResources();
        final TransitionDrawable transition = (TransitionDrawable) res.getDrawable(
                R.drawable.menu_item_background_longclick_transition);
        convertView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    transition.startTransition(Consts.DELAY_LONG_CLICK_ANIMATION_LONG);
                    view.setBackgroundDrawable(transition);
                }
                return false;
            }
        });

        return convertView;
    }

    private static class ViewHolder {
        TextView nameTextView;
    }
}