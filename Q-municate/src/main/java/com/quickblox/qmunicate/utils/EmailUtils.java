package com.quickblox.qmunicate.utils;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;

import com.quickblox.qmunicate.R;

public class EmailUtils {

    public static void sendInviteEmail(Context context, String[] selectedFriends) {
        Resources resources = context.getResources();
        Intent intentEmail = new Intent(Intent.ACTION_SEND);
        intentEmail.putExtra(Intent.EXTRA_EMAIL, selectedFriends);
        intentEmail.putExtra(Intent.EXTRA_SUBJECT, resources.getText(R.string.inf_subject_of_invitation));
        intentEmail.putExtra(Intent.EXTRA_TEXT, resources.getText(R.string.inf_body_of_invitation));
        intentEmail.setType(Consts.TYPE_OF_EMAIL);
        context.startActivity(Intent.createChooser(intentEmail, resources.getText(R.string.inf_choose_email_provider)));
    }

    public static void sendFeedbackEmail(Context context, String feedbackType, String feedbackDescription) {
        Resources resources = context.getResources();
        Intent intentEmail = new Intent(Intent.ACTION_SEND);
        intentEmail.putExtra(Intent.EXTRA_EMAIL, new String[]{resources.getText(R.string.fdb_support_email).toString()});
        intentEmail.putExtra(Intent.EXTRA_SUBJECT, feedbackType);
        intentEmail.putExtra(Intent.EXTRA_TEXT, feedbackDescription);
        intentEmail.setType(Consts.TYPE_OF_EMAIL);
        context.startActivity(Intent.createChooser(intentEmail, resources.getText(R.string.fdb_choose_email_provider)));
    }
}