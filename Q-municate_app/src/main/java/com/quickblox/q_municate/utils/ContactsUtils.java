package com.quickblox.q_municate.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import com.quickblox.q_municate_core.models.InviteContact;

import java.util.ArrayList;
import java.util.List;

public class ContactsUtils {

    public static List<InviteContact> getContactsWithEmail(Context context) {
        List<InviteContact> friendsContactsList = new ArrayList<InviteContact>();
        Uri uri = null;

        ContentResolver contentResolver = context.getContentResolver();

        String[] PROJECTION = new String[]{ContactsContract.RawContacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.PHOTO_ID,
                ContactsContract.CommonDataKinds.Email.DATA,
                ContactsContract.CommonDataKinds.Photo.CONTACT_ID};

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

                if (ValidationUtils.isEmailValid(email)) {
                    friendsContactsList.add(new InviteContact(email, name, null, InviteContact.VIA_EMAIL_TYPE,
                            uri, false));
                }
            } while (cursor.moveToNext());
        }
        if (cursor != null) {
            cursor.close();
        }

        return friendsContactsList;
    }

    public static List<InviteContact> getContactsWithPhone(Context context) {
        List<InviteContact> friendsContactsList = new ArrayList<InviteContact>();
        Uri uri = null;

        ContentResolver contentResolver = context.getContentResolver();

        String[] PROJECTION = new String[]{ContactsContract.RawContacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.PHOTO_ID,
                ContactsContract.CommonDataKinds.Phone.DATA,
                ContactsContract.CommonDataKinds.Photo.CONTACT_ID};

        String order = "upper("+ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + ") ASC";

        String filter = ContactsContract.CommonDataKinds.Phone.DATA + " NOT LIKE ''";

        Cursor cursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, PROJECTION,
                filter, null, order);

        if (cursor != null && cursor.moveToFirst()) {
            String id;
            String name;
            String phone;
            do {
                name = cursor.getString(cursor.getColumnIndex(
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                phone = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA));
                id = cursor.getString(cursor.getColumnIndex(
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
                if (ContactsContract.Contacts.CONTENT_URI != null) {
                    uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.parseLong(
                            id));
                    uri = Uri.withAppendedPath(uri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
                }

                if (PhoneNumbersUtils.isPhoneNumberValid(context, phone)) {
                    friendsContactsList.add(new InviteContact(PhoneNumbersUtils.getCorrectPhoneNumber(context, phone), name, null, InviteContact.VIA_PHONE_TYPE,
                            uri, false));
                }
            } while (cursor.moveToNext());
        }
        if (cursor != null) {
            cursor.close();
        }

        return friendsContactsList;
    }

    public static List<InviteContact> removeInviteContactsByQbIds(List<InviteContact> targetList, List<Integer> qbIdsForRemove){
        List<InviteContact> resultList = new ArrayList<>(targetList);

        for (int i = 0; i < resultList.size(); i++){
            for (Integer invitedContact : qbIdsForRemove){
                if (resultList.get(i).getQbId().equals(invitedContact)){
                    resultList.remove(i);
                    break;
                }
            }
        }
        return resultList;
    }

    public static InviteContact getInviteContactById(String inviteFriendId, List<InviteContact> sourceList) {
        for (InviteContact inviteContact : sourceList){
            if (inviteContact.getId().equals(inviteFriendId)){
                return inviteContact;
            }
        }
        return null;
    }
}
