package com.quickblox.q_municate.ui.adapters.base;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.quickblox.q_municate.utils.listeners.OnRecycleItemClickListener;
import com.quickblox.q_municate.utils.image.ImageLoaderUtils;

import butterknife.ButterKnife;

public class BaseClickListenerViewHolder<V> extends RecyclerView.ViewHolder {

    protected BaseRecyclerViewAdapter adapter;

    public BaseClickListenerViewHolder(final View.OnClickListener onClickListener, View itemView) {
        super(itemView);

        ButterKnife.bind(this, itemView);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickListener.onClick(v);
                onClickPerformed(v, null);
            }
        });
    }

    @SuppressWarnings("unchecked")
    public BaseClickListenerViewHolder(final BaseRecyclerViewAdapter adapter, final OnRecycleItemClickListener<V> onRecycleItemClickListener, final View itemView) {
        super(itemView);

        ButterKnife.bind(this, itemView);

        this.adapter = adapter;

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = getAdapterPosition();
                Object item = adapter.getItem(position);
                if (onRecycleItemClickListener != null) {
                    onRecycleItemClickListener.onItemClicked(itemView, (V) item, position);
                }
                onClickPerformed(v, (V) item);
            }
        });
        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int position = getAdapterPosition();
                V item = (V) adapter.getItem(position);
                if (onRecycleItemClickListener != null) {
                    onRecycleItemClickListener.onItemLongClicked(itemView, item, position);
                }
                onLongClickPerformed(v, item);
                return true;
            }
        });
    }

    protected void onClickPerformed(View v, @Nullable V entity) {
        // nothing by default
    }

    protected void onLongClickPerformed(View v, @Nullable V entity) {
        // nothing by default
    }

    protected void displayAvatarImage(String uri, ImageView imageView) {
        ImageLoader.getInstance().displayImage(uri, imageView, ImageLoaderUtils.UIL_USER_AVATAR_DISPLAY_OPTIONS);
    }
}