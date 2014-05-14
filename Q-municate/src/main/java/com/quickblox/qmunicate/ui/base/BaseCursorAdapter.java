package com.quickblox.qmunicate.ui.base;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.quickblox.internal.core.exception.BaseServiceException;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.model.LoginType;
import com.quickblox.qmunicate.utils.Consts;
import com.quickblox.qmunicate.utils.ErrorUtils;
import com.quickblox.qmunicate.utils.UriCreator;

public abstract class BaseCursorAdapter extends CursorAdapter {

    protected final Context context;
    protected final LayoutInflater layoutInflater;

    protected QBUser currentUser;
    protected LoginType currentLoginType;

    public BaseCursorAdapter(Context context, Cursor cursor, boolean autoRequery) {
        super(context, cursor, autoRequery);
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
        currentUser = App.getInstance().getUser();
        currentLoginType = App.getInstance().getUserLoginType();
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return null;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
    }

    protected void displayImage(String uri, ImageView imageView) {
        ImageLoader.getInstance().displayImage(uri, imageView, Consts.UIL_AVATAR_DISPLAY_OPTIONS);
    }

    protected String getAvatarUrlForCurrentUser() {
        try {
            if (currentLoginType == LoginType.FACEBOOK) {
                return context.getString(R.string.inf_url_to_facebook_avatar, currentUser.getFacebookId());
            } else if (currentLoginType == LoginType.EMAIL) {
                return UriCreator.getUri(UriCreator.cutUid(currentUser.getWebsite()));
            }
        } catch (BaseServiceException e) {
            ErrorUtils.showError(context, e);
        }
        return null;
    }

    protected String getAvatarUrlForFriend(Friend friend) {
        String avatarUid;
        String avatarUrl = null;

        avatarUid = friend.getAvatarUid();
        if (null != avatarUid) {
            try {
                avatarUrl = UriCreator.getUri(avatarUid);
            } catch (BaseServiceException e) {
                e.printStackTrace();
            }
        }
        return avatarUrl;
    }
}