package com.quickblox.q_municate_core.utils;

import com.quickblox.chat.model.QBRosterEntry;
import com.quickblox.q_municate_core.models.UserCustomData;
import com.quickblox.q_municate_db.models.DialogOccupant;
import com.quickblox.q_municate_db.models.Friend;
import com.quickblox.q_municate_db.models.UserRequest;
import com.quickblox.q_municate_user_service.model.QMUser;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.roster.packet.RosterPacket;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserFriendUtils {

    public static List<QMUser> getUsersFromFriends(List<Friend> friendList) {
        List<QMUser> userList = new ArrayList<>(friendList.size());
        for (Friend friend : friendList) {
            userList.add(friend.getUser());
        }
        return userList;
    }

    public static List<QMUser> getUsersFromUserRequest(List<UserRequest> userRequestList) {
        List<QMUser> userList = new ArrayList<>(userRequestList.size());
        for (UserRequest userRequest : userRequestList) {
            if (userRequest.getRequestStatus() == UserRequest.RequestStatus.OUTGOING) {
                userList.add(userRequest.getUser());
            }
        }
        return userList;
    }

    public static QMUser createLocalUser(QBUser qbUser) {
        QMUser user = new QMUser();
        user.setId(qbUser.getId());
        user.setFullName(qbUser.getFullName());
        user.setEmail(qbUser.getEmail());
        user.setPhone(qbUser.getPhone());
        user.setLogin(qbUser.getLogin());

        if (qbUser.getLastRequestAt() != null) {
            user.setLastRequestAt(qbUser.getLastRequestAt());
        }


        UserCustomData userCustomData = Utils.customDataToObject(qbUser.getCustomData());

        if (userCustomData != null) {
            user.setAvatar(userCustomData.getAvatarUrl());
            user.setStatus(userCustomData.getStatus());
        }

        return user;
    }


    public static QBUser createQbUser(QMUser user) {
        QBUser qbUser = new QBUser();
        qbUser.setId(user.getId());
        qbUser.setLogin(user.getLogin());
        qbUser.setFullName(user.getFullName());
        return qbUser;
    }

    public static boolean isOutgoingFriend(QBRosterEntry rosterEntry) {
        return RosterPacket.ItemStatus.subscribe.equals(rosterEntry.getStatus());
    }

    public static boolean isNoneFriend(QBRosterEntry rosterEntry) {
        return RosterPacket.ItemType.none.equals(rosterEntry.getType())
                || RosterPacket.ItemType.from.equals(rosterEntry.getType());
    }

    public static boolean isEmptyFriendsStatus(QBRosterEntry rosterEntry) {
        return rosterEntry.getStatus() == null;
    }

    public static List<QMUser> createUsersList(Collection<QBUser> usersList) {
        List<QMUser> users = new ArrayList<QMUser>();
        for (QBUser user : usersList) {
            users.add(createLocalUser(user));
        }
        return users;
    }

    public static List<QMUser> createUsers(Collection<QMUser> usersList) {
        List<QMUser> users = new ArrayList<QMUser>();
        for (QBUser user : usersList) {
            users.add(createLocalUser(user));
        }
        return users;
    }

    public static Map<Integer, QMUser> createUserMap(List<QBUser> userList) {
        Map<Integer, QMUser> userHashMap = new HashMap<Integer, QMUser>();
        for (QBUser user : userList) {
            userHashMap.put(user.getId(), createLocalUser(user));
        }
        return userHashMap;
    }

    public static ArrayList<Integer> getFriendIdsList(List<QBUser> friendList) {
        ArrayList<Integer> friendIdsList = new ArrayList<Integer>();
        for (QBUser friend : friendList) {
            friendIdsList.add(friend.getId());
        }
        return friendIdsList;
    }

    public static ArrayList<Integer> getFriendIds(List<QMUser> friendList) {
        ArrayList<Integer> friendIdsList = new ArrayList<Integer>();
        for (QMUser friend : friendList) {
            friendIdsList.add(friend.getId());
        }
        return friendIdsList;
    }

    public static ArrayList<Integer> getFriendIdsFromUsersList(List<QMUser> friendList) {
        ArrayList<Integer> friendIdsList = new ArrayList<Integer>();
        for (QMUser friend : friendList) {
            friendIdsList.add(friend.getId());
        }
        return friendIdsList;
    }

    public static List<Integer> getFriendIdsListFromList(List<Friend> friendsList) {
        List<Integer> friendIdsList = new ArrayList<Integer>();
        for (Friend friend : friendsList) {
            friendIdsList.add(friend.getUser().getId());
        }
        return friendIdsList;
    }

    public static List<Friend> getFriendsListFromDialogOccupantsList(List<DialogOccupant> dialogOccupantsList) {
        List<Friend> friendsList = new ArrayList<>(dialogOccupantsList.size());
        for (DialogOccupant dialogOccupant : dialogOccupantsList) {
            friendsList.add(new Friend(dialogOccupant.getUser()));
        }
        return friendsList;
    }

    public static String getUserNameByID(Integer userId, List<QBUser> usersList) {
        for (QBUser user : usersList) {
            if (user.getId().equals(userId)) {
                return user.getFullName();
            }
        }
        return userId.toString();
    }

    public static List<QBUser> getUsersByIDs(Integer[] ids, List<QBUser> usersList) {
        ArrayList<QBUser> result = new ArrayList<>();
        for (Integer userId : ids) {
            for (QBUser user : usersList) {
                if (userId.equals(user.getId())){
                    result.add(user);
                }
            }
        }
        return result;
    }

    public static QMUser createDeletedUser(int userId) {
        QMUser user = new QMUser();
        user.setId(userId);
        user.setFullName(String.valueOf(userId));
        return user;
    }
}