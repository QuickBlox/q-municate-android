package com.quickblox.qmunicate.ui.invitefriends;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;

import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.RequestAsyncTask;
import com.facebook.Response;
import com.facebook.Session;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.ui.utils.DialogUtils;
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

    public InviteViaFacebook(Activity activity) {
        this.activity = activity;
    }

    public void postInviteToFacebookWall(String[] selectedFriends) {
        Session session = Session.getActiveSession();
        if (session != null) {
            Bundle postParams = new Bundle();
            postParams.putString(PARAMS_NAME, "Quickblox");
            postParams.putString(PARAMS_DESCRIPTION, "This is QuickBlox, BABY!");
            postParams.putString(PARAMS_LINK, "http://quickblox.com/");
            postParams.putString(PARAMS_PICTURE, "http://www.apps-world.net/europe/images/stories/Quickblox.jpg");
            postParams.putString(PARAMS_PLACE, "155021662189");
            postParams.putString(PARAMS_TAGS, TextUtils.join(",", selectedFriends));

            Request.Callback callback = new Request.Callback() {
                public void onCompleted(Response response) {
                    FacebookRequestError error = response.getError();
                    if (error != null) {
                        DialogUtils.show(activity, activity.getResources().getString(R.string.facebook_exception) + error);
                    } else {
                        DialogUtils.show(activity, activity.getResources().getString(R.string.dlg_success_posted_to_facebook));
                    }
                }
            };
            Request request = new Request(session, PARAMS_FEED, postParams, HttpMethod.POST, callback);
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