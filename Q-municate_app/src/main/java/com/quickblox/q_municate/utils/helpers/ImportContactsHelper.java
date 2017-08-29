package com.quickblox.q_municate.utils.helpers;

import android.app.Activity;

import com.quickblox.q_municate.utils.ContactsUtils;
import com.quickblox.q_municate_core.models.InviteContact;

import java.util.ArrayList;
import java.util.List;

public class ImportContactsHelper {

    public Activity activity;
    private List<InviteContact> facebookContacts;
    private List<InviteContact> addressBookContacts;

    public ImportContactsHelper(Activity activity) {
        this.activity = activity;
        facebookContacts = new ArrayList<>();
        addressBookContacts = new ArrayList<>();
    }

    public void startGetFriendsListTask(boolean includeFacebookContacts) {
        List<InviteContact> contactsForImporting = new ArrayList<>();

        contactsForImporting.addAll(getContactsFromAddressBook());

        if (includeFacebookContacts){
            //TODO VT need add code for importing contacts from FB
        }
    }

    private List<String> getIdsList(List<InviteContact> friendsList) {
        if (friendsList.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> idsList = new ArrayList<>();
        for (InviteContact friend : friendsList) {
            idsList.add(friend.getId());
        }
        return idsList;
    }

    public List<InviteContact> getContactsFromAddressBook() {
        List<InviteContact> addressBookContacts = new ArrayList<>();

        addressBookContacts.addAll(ContactsUtils.getContactsWithPhone(activity));
        addressBookContacts.addAll(ContactsUtils.getContactsWithEmail(activity));

        return addressBookContacts;
    }
}