package com.quickblox.q_municate.utils.helpers;

import android.app.Activity;

import com.quickblox.q_municate_core.models.InviteFriend;
import com.quickblox.q_municate_core.qb.commands.friend.QBImportFriendsCommand;

import java.util.ArrayList;
import java.util.List;

public class ImportContactsHelper {

    public Activity activity;
    private List<InviteFriend> friendsPhonesList;
    private List<InviteFriend> friendsEmailsList;
    private List<InviteFriend> friendsFacebookList;
    private List<InviteFriend> addressBookContacts;

    public ImportContactsHelper(Activity activity) {
        this.activity = activity;
        friendsPhonesList = new ArrayList<>();
        friendsEmailsList = new ArrayList<>();
        friendsFacebookList = new ArrayList<>();
        addressBookContacts = new ArrayList<>();
    }

    public void startGetFriendsListTask(boolean includeFacebookContacts) {
        List<InviteFriend> contactsForImporting = new ArrayList<>();

        contactsForImporting.addAll(getContactsFromAdressBook());

        if (includeFacebookContacts){
            //TODO VT need add code for importing contacts from FB
        }

        fiendsReceived(contactsForImporting);
    }

    private List<String> getIdsList(List<InviteFriend> friendsList) {
        if (friendsList.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> idsList = new ArrayList<>();
        for (InviteFriend friend : friendsList) {
            idsList.add(friend.getId());
        }
        return idsList;
    }

    public List<InviteFriend> getContactsFromAdressBook() {
        if (addressBookContacts.isEmpty()) {
            addressBookContacts.addAll(EmailHelper.getContactsWithPhone(activity));
            addressBookContacts.addAll(EmailHelper.getContactsWithEmail(activity));
        }

        return addressBookContacts;
    }

    public void fiendsReceived(List<InviteFriend> contactsForImporting) {
        QBImportFriendsCommand.start(activity,
                contactsForImporting);
    }
}