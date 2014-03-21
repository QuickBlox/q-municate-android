package com.quickblox.qmunicate.ui.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.model.InviteFriend;

import java.util.ArrayList;
import java.util.List;

public class FriendsUtils {
    private Activity activity;
    private Resources resources;

    public FriendsUtils(Activity activity) {
        this.activity = activity;
        resources = activity.getResources();
    }

    public List<InviteFriend> getContactsWithEmail() {
        List<InviteFriend> friendsContactsList = new ArrayList<InviteFriend>();

        String id, name, email;
        Uri uri = null;

        ContentResolver contentResolver = activity.getContentResolver();
        Cursor cur = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));

                Cursor cursor = contentResolver.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null, ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?", new String[]{id}, null);
                while (cursor.moveToNext()) {
                    name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    email = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));

                    if (ContactsContract.Contacts.CONTENT_URI != null) {
                        uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.parseLong(id));
                    }

                    if (email != null) {
                        friendsContactsList.add(new InviteFriend(email, name, null, InviteFriend.VIA_CONTACTS_TYPE, uri, false));
                    }
                }
                cursor.close();
            }
        }
        return friendsContactsList;
    }

    public void sendEmail(String[] selectedFriends) {
        Intent intentEmail = new Intent(Intent.ACTION_SEND);
        intentEmail.putExtra(Intent.EXTRA_EMAIL, selectedFriends);
        intentEmail.putExtra(Intent.EXTRA_SUBJECT, resources.getText(R.string.inf_subject_of_invitation));
        intentEmail.putExtra(Intent.EXTRA_TEXT, resources.getText(R.string.inf_body_of_invitation));
        intentEmail.setType(Consts.INVITE_TYPE_OF_EMAIL);
        activity.startActivity(Intent.createChooser(intentEmail, resources.getText(R.string.inf_choose_email_provider)));
    }
}