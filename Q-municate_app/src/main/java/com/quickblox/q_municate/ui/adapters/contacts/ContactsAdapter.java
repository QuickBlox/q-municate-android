package com.quickblox.q_municate.ui.adapters.contacts;

import android.app.Activity;
import android.content.res.Resources;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.adapters.base.BaseClickListenerViewHolder;
import com.quickblox.q_municate.ui.adapters.base.BaseViewHolder;
import com.quickblox.q_municate.ui.adapters.base.FilterAdapter;
import com.quickblox.q_municate.core.listeners.UserOperationListener;
import com.quickblox.q_municate.ui.views.roundedimageview.RoundedImageView;
import com.quickblox.q_municate.utils.TextViewHelper;
import com.quickblox.q_municate_core.qb.helpers.QBFriendListHelper;
import com.quickblox.q_municate_db.models.User;

import java.util.List;

import butterknife.Bind;

public class ContactsAdapter extends FilterAdapter<User, BaseClickListenerViewHolder<User>> {

    private Resources resources;
    private UserOperationListener userOperationListener;
    private QBFriendListHelper friendListHelper;

    public ContactsAdapter(Activity activity, List<User> list) {
        super(activity, list);
        resources = context.getResources();
    }

    @Override
    protected boolean isMatch(User item, String query) {
        return item.getFullName() != null && item.getFullName().toLowerCase().contains(query);
    }

    @Override
    public BaseClickListenerViewHolder<User> onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(this, layoutInflater.inflate(R.layout.list_item_friend, parent, false));
    }

    @Override
    public void onBindViewHolder(BaseClickListenerViewHolder<User> baseClickListenerViewHolder, int position) {
        User user = getItem(position);
        ViewHolder holder = (ViewHolder) baseClickListenerViewHolder;

        if (user.getFullName() != null) {
            holder.fullNameTextView.setText(user.getFullName());
        } else {
            holder.fullNameTextView.setText(user.getUserId());
        }

        String avatarUrl = user.getAvatar();
        displayAvatarImage(avatarUrl, holder.avatarImageView);

        initListeners(holder, user.getUserId());

        if (!TextUtils.isEmpty(query)) {
            TextViewHelper.changeTextColorView(context, holder.fullNameTextView, query);
        }
    }

    private void initListeners(ViewHolder viewHolder, final int userId) {
        viewHolder.addFriendImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userOperationListener.onAddUserClicked(userId);
            }
        });
    }

    public void setUserOperationListener(UserOperationListener userOperationListener) {
        this.userOperationListener = userOperationListener;
    }

    public void setFriendListHelper(QBFriendListHelper friendListHelper) {
        this.friendListHelper = friendListHelper;
        notifyDataSetChanged();
    }

    protected static class ViewHolder extends BaseViewHolder<User> {

        @Bind(R.id.avatar_imageview)
        RoundedImageView avatarImageView;

        @Bind(R.id.name_textview)
        TextView fullNameTextView;

        @Bind(R.id.status_textview)
        TextView statusTextView;

        @Bind(R.id.add_friend_imagebutton)
        ImageView addFriendImageView;

        @Bind(R.id.online_imageview)
        ImageView onlineImageView;

        public ViewHolder(ContactsAdapter adapter, View view) {
            super(adapter, view);
        }
    }
}