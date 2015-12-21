package com.quickblox.q_municate_core.utils;

import com.quickblox.chat.model.QBRosterEntry;
import com.quickblox.q_municate_core.models.UserCustomData;
import com.quickblox.q_municate_db.models.DialogOccupant;
import com.quickblox.q_municate_db.models.Friend;
import com.quickblox.q_municate_db.models.User;
import com.quickblox.q_municate_db.models.UserRequest;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.packet.RosterPacket;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserFriendUtils {

    public static List<User> getUsersFromFriends(List<Friend> friendList) {
        List<User> userList = new ArrayList<>(friendList.size());
        for (Friend friend : friendList) {
            userList.add(friend.getUser());
        }
        return userList;
    }

    public static List<User> getUsersFromUserRequest(List<UserRequest> userRequestList) {
        List<User> userList = new ArrayList<>(userRequestList.size());
        for (UserRequest userRequest : userRequestList) {
            if (userRequest.getRequestStatus() == UserRequest.RequestStatus.OUTGOING) {
                userList.add(userRequest.getUser());
            }
        }
        return userList;
    }

    public static User createLocalUser(QBUser qbUser, User.Role role) {
        User user = new User();
        user.setUserId(qbUser.getId());
        user.setFullName(qbUser.getFullName());
        user.setEmail(qbUser.getEmail());
        user.setPhone(qbUser.getPhone());
        user.setLogin(qbUser.getLogin());

        if (qbUser.getLastRequestAt() != null) {
            user.setLastLogin(DateUtilsCore.getTime(qbUser.getLastRequestAt()));
        }

        user.setRole(role);

        UserCustomData userCustomData = Utils.customDataToObject(qbUser.getCustomData());

        if (userCustomData != null) {
            user.setAvatar(userCustomData.getAvatar_url());
            user.setStatus(userCustomData.getStatus());
        }

        return user;
    }

    public static User createLocalUser(QBUser qbUser) {
        return createLocalUser(qbUser, User.Role.SIMPLE_ROLE);
    }

    public static QBUser createQbUser(User user) {
        QBUser qbUser = new QBUser();
        qbUser.setId(user.getUserId());
        qbUser.setLogin(user.getLogin());
        qbUser.setFullName(user.getFullName());
        return qbUser;
    }

    public static boolean isOutgoingFriend(QBRosterEntry rosterEntry) {
        return RosterPacket.ItemStatus.subscribe.equals(rosterEntry.getStatus());
    }

    public static boolean isNoneFriend(QBRosterEntry rosterEntry) {
        return RosterPacket.ItemType.none.equals(rosterEntry.getType());
    }

    public static boolean isEmptyFriendsStatus(QBRosterEntry rosterEntry) {
        return rosterEntry.getStatus() == null;
    }

    public static List<User> createUsersList(Collection<QBUser> usersList) {
        List<User> users = new ArrayList<User>();
        for (QBUser user : usersList) {
            users.add(createLocalUser(user));
        }
        return users;
    }

    public static Map<Integer, User> createUserMap(List<QBUser> userList) {
        Map<Integer, User> userHashMap = new HashMap<Integer, User>();
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

    public static ArrayList<Integer> getFriendIds(List<User> friendList) {
        ArrayList<Integer> friendIdsList = new ArrayList<Integer>();
        for (User friend : friendList) {
            friendIdsList.add(friend.getUserId());
        }
        return friendIdsList;
    }

    public static ArrayList<Integer> getFriendIdsFromUsersList(List<User> friendList) {
        ArrayList<Integer> friendIdsList = new ArrayList<Integer>();
        for (User friend : friendList) {
            friendIdsList.add(friend.getUserId());
        }
        return friendIdsList;
    }

    public static List<Integer> getFriendIdsListFromList(List<Friend> friendsList) {
        List<Integer> friendIdsList = new ArrayList<Integer>();
        for (Friend friend : friendsList) {
            friendIdsList.add(friend.getUser().getUserId());
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

    public static User createDeletedUser(int userId) {
        User user = new User();
        user.setUserId(userId);
        user.setFullName(String.valueOf(userId));
        return user;
    }
}