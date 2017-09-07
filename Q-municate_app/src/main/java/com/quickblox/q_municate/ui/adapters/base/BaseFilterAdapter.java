package com.quickblox.q_municate.ui.adapters.base;

import android.text.TextUtils;

import com.quickblox.q_municate.ui.activities.base.BaseActivity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class BaseFilterAdapter<T, VH extends BaseClickListenerViewHolder<T>> extends BaseRecyclerViewAdapter<T, VH> {

    private List<T> visibleList;
    protected String query;

    public BaseFilterAdapter(BaseActivity baseActivity) {
        super(baseActivity);
    }

    public BaseFilterAdapter(BaseActivity baseActivity, List<T> list) {
        super(baseActivity, list);
        visibleList = list;
    }

    public void setFilter(String query) {
        this.query = query;
        if (TextUtils.isEmpty(query)) {
            flushFilter();
            return;
        }
        query = query.toLowerCase();
        visibleList = new ArrayList<>();
        for (T item : super.getAllItems()) {
            if (isMatch(item, query)) {
                visibleList.add(item);
            }
        }
        notifyDataSetChanged();
    }

    public void flushFilter() {
        this.query = "";
        visibleList = new ArrayList<>(super.getAllItems());
        notifyDataSetChanged();
    }

    public void setList(List<T> list) {
        super.setList(list);
        setFilter(query);
    }

    @Override
    public void addAll(Collection<T> collection) {
        super.addAll(collection);
        setFilter(query);
    }

    @Override
    public void addItem(T item) {
        super.addItem(item);
        setFilter(query);
    }

    @Override
    public void addItem(int position, T item) {
        super.addItem(position, item);
        setFilter(query);
    }

    @Override
    public void removeItem(int position) {
        super.removeItem(position);
        setFilter(query);
    }

    @Override
    public void removeItem(T item) {
        super.removeItem(item);
        setFilter(query);
    }

    @Override
    public void addOrUpdateItem(T item) {
        super.addOrUpdateItem(item);
        setFilter(query);
    }

    @Override
    public void clear() {
        super.clear();
        setFilter(query);
    }

    @Override
    public T getItem(int position) {
        return visibleList.get(position);
    }

    @Override
    public List<T> getAllItems() {
        return visibleList;
    }

    @Override
    public boolean isEmpty() {
        return visibleList.isEmpty();
    }

    @Override
    public int getItemCount() {
        return visibleList == null ? 0 : visibleList.size();
    }

    protected abstract boolean isMatch(T item, String query);
}
