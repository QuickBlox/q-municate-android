package com.quickblox.q_municate_contact_list_service.model;

import com.quickblox.chat.model.QBContactEntry;

import java.util.List;

public class QBContactEntryList {

    /**
     Current contacts
     */
    private List<QBContactEntry> contacts;

    /**
     Your requests which pending approval
     */
    private List<QBContactEntry> pendingApproval;


    public List<QBContactEntry> getContacts() {
        return contacts;
    }

    public void setContacts(List<QBContactEntry> contacts) {
        this.contacts = contacts;
    }

    public List<QBContactEntry> getPendingApproval() {
        return pendingApproval;
    }

    public void setPendingApproval(List<QBContactEntry> pendingApproval) {
        this.pendingApproval = pendingApproval;
    }
}
