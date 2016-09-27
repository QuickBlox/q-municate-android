package com.quickblox.q_municate.utils.helpers;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import com.quickblox.q_municate.R;
import com.quickblox.q_municate.utils.DeviceInfoUtils;
import com.quickblox.q_municate_core.models.InviteFriend;
import com.quickblox.q_municate_core.utils.ConstsCore;

import java.util.ArrayList;
import java.util.List;

public class EmailHelper {

    public static void sendInviteEmail(Context context, String[] selectedFriends) {
        Resources resources = context.getResources();
        Intent intentEmail = new Intent(Intent.ACTION_SEND);
        intentEmail.putExtra(Intent.EXTRA_EMAIL, selectedFriends);
        intentEmail.putExtra(Intent.EXTRA_SUBJECT, resources.getText(R.string.invite_friends_subject_of_invitation));
        intentEmail.putExtra(Intent.EXTRA_TEXT, resources.getText(R.string.invite_friends_body_of_invitation));
        intentEmail.setType(ConstsCore.TYPE_OF_EMAIL);
        context.startActivity(Intent.createChooser(intentEmail, resources.getText(
                R.string.invite_friends_choose_email_provider)));
    }

    public static void sendFeedbackEmail(Context context, String feedbackType) {
        Resources resources = context.getResources();
        Intent intentEmail = new Intent(Intent.ACTION_SEND);
        intentEmail.putExtra(Intent.EXTRA_EMAIL, new String[]{resources.getText(R.string.feedback_support_email)
                .toString()});
        intentEmail.putExtra(Intent.EXTRA_SUBJECT, feedbackType);
        intentEmail.putExtra(Intent.EXTRA_TEXT,
                (java.io.Serializable) DeviceInfoUtils.getDeviseInfoForFeedback());
        intentEmail.setType(ConstsCore.TYPE_OF_EMAIL);
        context.startActivity(Intent.createChooser(intentEmail, resources.getText(
                R.string.feedback_choose_email_provider)));
    }

    public static List<InviteFriend> getContactsWithEmail(Context context) {
        List<InviteFriend> friendsContactsList = new ArrayList<InviteFriend>();
        Uri uri = null;

        ContentResolver contentResolver = context.getContentResolver();

        String[] PROJECTION = new String[]{ContactsContract.RawContacts._ID, ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts.PHOTO_ID, ContactsContract.CommonDataKinds.Email.DATA, ContactsContract.CommonDataKinds.Photo.CONTACT_ID};

        String order = "upper("+ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + ") ASC";

        String filter = ContactsContract.CommonDataKinds.Email.DATA + " NOT LIKE ''";

        Cursor cursor = contentResolver.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, PROJECTION,
                filter, null, order);

        if (cursor != null && cursor.moveToFirst()) {
            String id;
            String name;
            String email;
            do {
                name = cursor.getString(cursor.getColumnIndex(
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                email = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                id = cursor.getString(cursor.getColumnIndex(
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
                if (ContactsContract.Contacts.CONTENT_URI != null) {
                    uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.parseLong(
                            id));
                    uri = Uri.withAppendedPath(uri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
                }
                friendsContactsList.add(new InviteFriend(email, name, null, InviteFriend.VIA_CONTACTS_TYPE,
                        uri, false));
            } while (cursor.moveToNext());
        }
        if (cursor != null) {
            cursor.close();
        }

        return friendsContactsList;
    }
}