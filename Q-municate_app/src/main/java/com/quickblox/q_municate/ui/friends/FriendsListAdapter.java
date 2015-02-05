package com.quickblox.q_municate.ui.friends;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.views.RoundedImageView;
import com.quickblox.q_municate.utils.Consts;
import com.quickblox.q_municate.utils.TextViewHelper;
import com.quickblox.q_municate_core.db.managers.UsersDatabaseManager;
import com.quickblox.q_municate_core.models.Friend;
import com.quickblox.q_municate_core.models.FriendGroup;
import com.quickblox.q_municate_core.models.User;
import com.quickblox.q_municate_core.qb.helpers.QBFriendListHelper;
import com.quickblox.q_municate_core.utils.ConstsCore;

import java.util.ArrayList;
import java.util.List;

public class FriendsListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<FriendGroup> friendGroupList;
    private List<FriendGroup> originalList;
    private FriendOperationListener friendOperationListener;
    private LayoutInflater layoutInflater;
    private Resources resources;
    private String searchCharacters;

    public FriendsListAdapter(Context context, FriendOperationListener friendOperationListener,
            List<FriendGroup> friendGroupList) {
        this.context = context;
        this.friendOperationListener = friendOperationListener;
        this.friendGroupList = new ArrayList<FriendGroup>();
        this.friendGroupList.addAll(friendGroupList);
        this.originalList = new ArrayList<FriendGroup>();
        this.originalList.addAll(friendGroupList);
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        resources = context.getResources();
    }

    private void initListeners(ViewHolder viewHolder, final int userId) {
        viewHolder.addFriendImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                friendOperationListener.onAddUserClicked(userId);
            }
        });
    }

    public void setSearchCharacters(String searchCharacters) {
        this.searchCharacters = searchCharacters;
    }

    private void displayAvatarImage(String uri, ImageView imageView) {
        ImageLoader.getInstance().displayImage(uri, imageView, Consts.UIL_USER_AVATAR_DISPLAY_OPTIONS);
    }

    private String getAvatarUrlForUser(User user) {
        return user.getAvatarUrl();
    }

    @Override
    public int getGroupCount() {
        return friendGroupList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        List<User> userList = friendGroupList.get(groupPosition).getUserList();
        return userList.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return friendGroupList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        List<User> userList = friendGroupList.get(groupPosition).getUserList();
        return userList.get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isLastChild, View view, ViewGroup parent) {
        FriendGroup friendGroup = (FriendGroup) getGroup(groupPosition);

        //By default the group is hidden
        View hiddenView = new FrameLayout(context);

        if (friendGroup.getUserList().isEmpty()) {
            return hiddenView;
        } else {
            view = layoutInflater.inflate(R.layout.view_section_title_friends_list, null);
        }

        TextView headerName = (TextView) view.findViewById(R.id.list_title_textview);
        headerName.setText(friendGroup.getHeaderName());

        return view;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View view,
            ViewGroup parent) {
        ViewHolder viewHolder;
        User user = (User) getChild(groupPosition, childPosition);

        if (view == null) {
            view = layoutInflater.inflate(R.layout.list_item_friend, null);

            viewHolder = new ViewHolder();

            viewHolder.avatarImageView = (RoundedImageView) view.findViewById(R.id.avatar_imageview);
            viewHolder.fullNameTextView = (TextView) view.findViewById(R.id.name_textview);
            viewHolder.statusTextView = (TextView) view.findViewById(R.id.status_textview);
            viewHolder.addFriendImageView = (ImageView) view.findViewById(R.id.add_friend_imagebutton);
            viewHolder.onlineImageView = (ImageView) view.findViewById(R.id.online_imageview);

            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        if (user.getFullName() != null) {
            viewHolder.fullNameTextView.setText(user.getFullName());
        } else {
            viewHolder.fullNameTextView.setText(user.getUserId());
        }

        String avatarUrl = getAvatarUrlForUser(user);
        displayAvatarImage(avatarUrl, viewHolder.avatarImageView);

        FriendGroup friendGroup = (FriendGroup) getGroup(groupPosition);
        checkVisibilityItems(viewHolder, user, friendGroup);

        initListeners(viewHolder, user.getUserId());

        if (!TextUtils.isEmpty(searchCharacters)) {
            TextViewHelper.changeTextColorView(context, viewHolder.fullNameTextView, searchCharacters);
        }

        return view;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    private void checkVisibilityItems(ViewHolder viewHolder, User user, FriendGroup friendGroup) {
        if (FriendGroup.GROUP_POSITION_ALL_USERS == friendGroup.getHeaderId()) {
            checkVisibilityItemsAllUsers(viewHolder);
        } else if (FriendGroup.GROUP_POSITION_MY_CONTACTS == friendGroup.getHeaderId()) {
            checkVisibilityItemsMyContacts(viewHolder, user);
        }
    }

    private void checkVisibilityItemsAllUsers(ViewHolder viewHolder) {
        viewHolder.addFriendImageView.setVisibility(View.VISIBLE);
        viewHolder.onlineImageView.setVisibility(View.GONE);
        viewHolder.statusTextView.setVisibility(View.GONE);
    }

    private void checkVisibilityItemsMyContacts(ViewHolder viewHolder, User user) {
        String status;

        Friend friend = UsersDatabaseManager.getFriendById(context, user.getUserId());

        if (friend == null) {
            return;
        }

        String relationStatus = friend.getRelationStatus();

        boolean isAddedFriend;

        isAddedFriend = relationStatus.equals(QBFriendListHelper.RELATION_STATUS_NONE) && friend
                .isPendingStatus();
        if (isAddedFriend) {
            viewHolder.onlineImageView.setVisibility(View.GONE);
            status = resources.getString(R.string.frl_pending_request_status);
        } else {
            status = user.getOnlineStatus(context);
        }

        viewHolder.addFriendImageView.setVisibility(View.GONE);
        viewHolder.statusTextView.setText(status);

        viewHolder.statusTextView.setVisibility(View.VISIBLE);

        setStatusVisibility(viewHolder, user.isOnline());
    }

    private void setStatusVisibility(ViewHolder viewHolder, boolean status) {
        if (status) {
            viewHolder.onlineImageView.setVisibility(View.VISIBLE);
        } else {
            viewHolder.onlineImageView.setVisibility(View.INVISIBLE);
        }
    }

    public void filterData(String query) {
        query = query.toLowerCase();

        friendGroupList.clear();

        if (query.isEmpty()) {
            friendGroupList.addAll(originalList);
        } else {
            for (FriendGroup friendGroup : originalList) {
                List<User> userList = friendGroup.getUserList();
                List<User> newUserList = new ArrayList<User>();
                for (User user : userList) {
                    if (user.getFullName().toLowerCase().contains(query) || user.getFullName()
                            .toLowerCase().contains(query)) {
                        newUserList.add(user);
                    }
                }
                if (newUserList.size() > ConstsCore.ZERO_INT_VALUE) {
                    FriendGroup nContinent = new FriendGroup(friendGroup.getHeaderId(),
                            friendGroup.getHeaderName(), newUserList);
                    friendGroupList.add(nContinent);
                }
            }
        }

        notifyDataSetChanged();
    }

    private static class ViewHolder {

        public RoundedImageView avatarImageView;
        public TextView fullNameTextView;
        public TextView statusTextView;
        public ImageView addFriendImageView;
        public ImageView onlineImageView;
    }
}