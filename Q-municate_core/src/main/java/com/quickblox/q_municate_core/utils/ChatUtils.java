package com.quickblox.q_municate_core.utils;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.quickblox.chat.model.QBAttachment;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.q_municate_core.db.managers.ChatDatabaseManager;
import com.quickblox.q_municate_core.db.managers.UsersDatabaseManager;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.models.User;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ChatUtils {

    public static final String OCCUPANT_IDS_DIVIDER = ",";
    public static final int NOT_RESET_COUNTER = -1;

    public static int getOccupantIdFromList(ArrayList<Integer> occupantsIdsList) {
        QBUser user = AppSession.getSession().getUser();
        int resultId = ConstsCore.ZERO_INT_VALUE;
        for (Integer id : occupantsIdsList) {
            if (!id.equals(user.getId())) {
                resultId = id;
                break;
            }
        }
        return resultId;
    }

    public static String getAttachUrlFromMessage(Collection<QBAttachment> attachmentsCollection) {
        if (attachmentsCollection != null) {
            ArrayList<QBAttachment> attachmentsList = new ArrayList<QBAttachment>(attachmentsCollection);
            if (!attachmentsList.isEmpty()) {
                return attachmentsList.get(0).getUrl();
            }
        }
        return ConstsCore.EMPTY_STRING;
    }

    public static ArrayList<Integer> createOccupantsIdsFromPrivateMessage(int currentUserId, int senderId) {
        ArrayList<Integer> occupantsIdsList = new ArrayList<Integer>(2);
        occupantsIdsList.add(currentUserId);
        occupantsIdsList.add(senderId);
        return occupantsIdsList;
    }

    public static ArrayList<Integer> getOccupantsIdsListFromString(String occupantIds) {
        ArrayList<Integer> occupantIdsList = new ArrayList<Integer>();
        String[] occupantIdsArray = occupantIds.split(OCCUPANT_IDS_DIVIDER);
        for (String occupantId : occupantIdsArray) {
            occupantIdsList.add(Integer.valueOf(occupantId));
        }
        return occupantIdsList;
    }

    public static ArrayList<Integer> getOccupantIdsWithUser(List<Integer> friendIdsList) {
        QBUser user = AppSession.getSession().getUser();
        ArrayList<Integer> occupantIdsList = new ArrayList<Integer>(friendIdsList);
        occupantIdsList.add(user.getId());
        return occupantIdsList;
    }

    public static ArrayList<Integer> getOccupantIdsWithoutUser(Collection<Integer> friendIdsList) {
        QBUser user = AppSession.getSession().getUser();
        ArrayList<Integer> occupantIdsList = new ArrayList<Integer>(friendIdsList);
        occupantIdsList.remove(user.getId());
        return occupantIdsList;
    }


    public static String getOccupantsIdsStringFromList(Collection<Integer> occupantIdsList) {
        return TextUtils.join(OCCUPANT_IDS_DIVIDER, occupantIdsList);
    }

    public static ArrayList<Integer> getOccupantsIdsListForCreatePrivateDialog(int opponentId) {
        QBUser user = AppSession.getSession().getUser();
        ArrayList<Integer> occupantsIdsList = new ArrayList<Integer>();
        occupantsIdsList.add(user.getId());
        occupantsIdsList.add(opponentId);
        return occupantsIdsList;
    }

    public static String getAttachUrlIfExists(QBChatMessage chatMessage) {
        String attachURL = ConstsCore.EMPTY_STRING;
        Collection<QBAttachment> attachmentCollection = chatMessage.getAttachments();
        if (attachmentCollection != null && attachmentCollection.size() > 0) {
            attachURL = getAttachUrlFromMessage(attachmentCollection);
        }
        return attachURL;
    }

    public static User getTempUserFromChatMessage(QBChatMessage chatMessage) {
        User user = new User();
        user.setUserId(chatMessage.getSenderId());
        user.setFullName(chatMessage.getSenderId() + ConstsCore.EMPTY_STRING);
        return user;
    }

    public static Bundle getBundleForCreatePrivateChat(int userId) {
        Bundle bundle = new Bundle();
        bundle.putInt(QBServiceConsts.EXTRA_OPPONENT_ID, userId);
        return bundle;
    }

    public static QBDialog getExistPrivateDialog(Context context, int opponentId) {
        List<QBDialog> dialogList = ChatDatabaseManager.getDialogsByOpponent(context, opponentId,
                QBDialogType.PRIVATE);
        if (!dialogList.isEmpty()) {
            return dialogList.get(0);
        } else {
            return null;
        }
    }

    public static String getFullNameById(Context context, int userId) {
        User user = UsersDatabaseManager.getUserById(context, userId);
        if (user == null) {
            try {
                QBUser qbUser = QBUsers.getUser(userId);
                user = FriendUtils.createUser(qbUser);
                UsersDatabaseManager.saveUser(context, user);
                return qbUser.getFullName();
            } catch (QBResponseException e) {
                ErrorUtils.logError(e);
            }
        }
        return user.getFullName();
    }

    public static String getFullNamesFromOpponentIds(Context context, String occupantsIdsString) {
        List<Integer> occupantsIdsList = getOccupantsIdsListFromString(occupantsIdsString);
        return getFullNamesFromOpponentIdsList(context, occupantsIdsList);
    }

    public static String getFullNamesFromOpponentId(Context context, Integer userId, String occupantsIdsString) {
        List<Integer> occupantsIdsList = getOccupantsIdsListFromString(occupantsIdsString);
        occupantsIdsList.remove(userId);
        return getFullNamesFromOpponentIdsList(context, occupantsIdsList);
    }

    private static String getFullNamesFromOpponentIdsList(Context context, List<Integer> occupantsIdsList) {
        StringBuilder stringBuilder = new StringBuilder(occupantsIdsList.size());
        for (Integer id : occupantsIdsList) {
            stringBuilder.append(getFullNameById(context, id)).append(OCCUPANT_IDS_DIVIDER).append(" ");
        }
        return stringBuilder.toString().substring(ConstsCore.ZERO_INT_VALUE, stringBuilder.length() - 2);
    }
}