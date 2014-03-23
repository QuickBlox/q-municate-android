package com.quickblox.qmunicate.ui.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
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
    private Context context;

    public FriendsUtils(Context context) {
        this.context = context;
    }

    public List<InviteFriend> getContactsWithEmail() {
        List<InviteFriend> friendsContactsList = new ArrayList<InviteFriend>();
        Uri uri = null;
        String id, name, email;

        ContentResolver contentResolver = context.getContentResolver();

        String[] PROJECTION = new String[]{ContactsContract.RawContacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.PHOTO_ID,
                ContactsContract.CommonDataKinds.Email.DATA,
                ContactsContract.CommonDataKinds.Photo.CONTACT_ID};

        String order = "CASE WHEN "
                + ContactsContract.Contacts.DISPLAY_NAME
                + " NOT LIKE '%@%' THEN 1 ELSE 2 END, "
                + ContactsContract.CommonDataKinds.Phone.CONTACT_ID
                + ", "
                + ContactsContract.Contacts.DISPLAY_NAME
                + ", "
                + ContactsContract.CommonDataKinds.Email.DATA
                + " COLLATE NOCASE";

        String filter = ContactsContract.CommonDataKinds.Email.DATA + " NOT LIKE ''";

        Cursor cursor = contentResolver.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, PROJECTION, filter, null, order);

        if (cursor.moveToFirst()) {
            do {
                name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                email = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                id = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));

                if (ContactsContract.Contacts.CONTENT_URI != null) {
                    uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.parseLong(id));
                }

                friendsContactsList.add(new InviteFriend(email, name, null, InviteFriend.VIA_CONTACTS_TYPE, uri, false));
            } while (cursor.moveToNext());
        }

        cursor.close();

        return friendsContactsList;
    }

    public void sendEmail(String[] selectedFriends) {
        Resources resources = context.getResources();
        Intent intentEmail = new Intent(Intent.ACTION_SEND);
        intentEmail.putExtra(Intent.EXTRA_EMAIL, selectedFriends);
        intentEmail.putExtra(Intent.EXTRA_SUBJECT, resources.getText(R.string.inf_subject_of_invitation));
        intentEmail.putExtra(Intent.EXTRA_TEXT, resources.getText(R.string.inf_body_of_invitation));
        intentEmail.setType(Consts.INVITE_TYPE_OF_EMAIL);
        context.startActivity(Intent.createChooser(intentEmail, resources.getText(R.string.inf_choose_email_provider)));
    }
}