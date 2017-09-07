package com.quickblox.q_municate.ui.adapters.base;

import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.quickblox.q_municate.ui.activities.base.BaseActivity;
import com.quickblox.q_municate.utils.listeners.OnRecycleItemClickListener;
import com.quickblox.q_municate.utils.image.ImageLoaderUtils;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class BaseRecyclerViewAdapter<T, VH extends BaseClickListenerViewHolder> extends RecyclerView.Adapter<VH> {

    private List<T> objectsList;
    protected final BaseActivity baseActivity;
    protected final LayoutInflater layoutInflater;
    protected final Resources resources;
    protected QBUser currentQbUser;

    // Package private because we need access in BaseViewHolder but not in child classes
    OnRecycleItemClickListener<T> onRecycleItemClickListener;

    public BaseRecyclerViewAdapter(BaseActivity baseActivity) {
        this.baseActivity = baseActivity;
        this.layoutInflater = LayoutInflater.from(baseActivity);
        resources = baseActivity.getResources();
        objectsList = new ArrayList<>();
    }

    public BaseRecyclerViewAdapter(BaseActivity baseActivity, List<T> objectsList) {
        this(baseActivity);
        this.objectsList = objectsList;
        currentQbUser = AppSession.getSession().getUser();
    }

    public void setList(List<T> items) {
        objectsList = items;
        notifyDataSetChanged();
    }

    public void addItem(T item) {
        objectsList.add(item);
        notifyItemInserted(objectsList.size() - 1);
    }

    public void addItem(int position, T item) {
        objectsList.add(position, item);
        notifyItemInserted(position);
    }

    public void addAll(Collection<T> collection) {
        objectsList.addAll(collection);
        notifyItemRangeChanged(objectsList.size() - collection.size(), collection.size());
    }

    public void addAllInBegin(List <T> collection) {
        objectsList.addAll(0, collection);

        notifyItemRangeInserted(0, collection.size());
    }

    public void addOrUpdateItem(T item){
        if (!objectsList.contains(item)){
            addItem(0, item);
        } else {
            int existItemPosition = objectsList.indexOf(item);
            objectsList.remove(existItemPosition);
            addItem(existItemPosition, item);
        }
    }

    public void removeItem(int position) {
        objectsList.remove(position);
        notifyItemRemoved(position);
    }

    public void removeItem(T item) {
        int position = objectsList.indexOf(item);
        if (position != -1) {
            objectsList.remove(item);
            notifyItemRemoved(position);
        }
    }

    public void clear() {
        objectsList.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return objectsList.size();
    }

    public T getItem(int position) {
        return objectsList.get(position);
    }

    public List<T> getAllItems() {
        return objectsList;
    }

    public boolean isEmpty() {
        return objectsList.size() == 0;
    }

    public void setOnRecycleItemClickListener(OnRecycleItemClickListener<T> onRecycleItemClickListener) {
        this.onRecycleItemClickListener = onRecycleItemClickListener;
    }

    protected void displayAvatarImage(String uri, ImageView imageView) {
        ImageLoader.getInstance().displayImage(uri, imageView, ImageLoaderUtils.UIL_USER_AVATAR_DISPLAY_OPTIONS);
    }
}