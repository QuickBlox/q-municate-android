package com.quickblox.qmunicate.ui.invitefriends;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.RequestAsyncTask;
import com.facebook.Session;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.ui.utils.FacebookHelper;

import java.util.Collection;
import java.util.List;

public class InviteViaFacebook {
    private Activity activity;

    private final String PARAMS_NAME = "name";
    private final String PARAMS_DESCRIPTION = "description";
    private final String PARAMS_LINK = "link";
    private final String PARAMS_PICTURE = "picture";
    private final String PARAMS_PLACE = "place";
    private final String PARAMS_TAGS = "tags";
    private final String PARAMS_FEED = "me/feed";

    private Request.Callback requestCallback;

    public InviteViaFacebook(Activity activity, Request.Callback requestCallback) {
        this.activity = activity;
        this.requestCallback = requestCallback;
    }

    public void postInviteToFacebookWall(String[] selectedFriends) {
        Session session = Session.getActiveSession();
        if (session != null) {
            Resources resources = activity.getResources();
            Bundle postParams = new Bundle();
            postParams.putString(PARAMS_NAME, resources.getString(R.string.inf_fb_wall_param_name));
            postParams.putString(PARAMS_DESCRIPTION, resources.getString(R.string.inf_fb_wall_param_description));
            postParams.putString(PARAMS_LINK, resources.getString(R.string.inf_fb_wall_param_link));
            postParams.putString(PARAMS_PICTURE, resources.getString(R.string.inf_fb_wall_param_picture));
            postParams.putString(PARAMS_PLACE, resources.getString(R.string.inf_fb_wall_param_place));
            postParams.putString(PARAMS_TAGS, TextUtils.join(",", selectedFriends));
            Request request = new Request(session, PARAMS_FEED, postParams, HttpMethod.POST, requestCallback);
            RequestAsyncTask task = new RequestAsyncTask(request);
            task.execute();
        }
    }

    private boolean isSubsetOf(Collection<String> subset, Collection<String> superset) {
        for (String string : subset) {
            if (!superset.contains(string)) {
                return false;
            }
        }
        return true;
    }

    public boolean checkPermissions() {
        Session session = Session.getActiveSession();
        List<String> permissions = session.getPermissions();
        if (!isSubsetOf(FacebookHelper.PERMISSIONS, permissions)) {
            Session.NewPermissionsRequest newPermissionsRequest = new Session
                    .NewPermissionsRequest(activity, FacebookHelper.PERMISSIONS);
            session.requestNewPublishPermissions(newPermissionsRequest);
            return false;
        }
        return true;
    }
}