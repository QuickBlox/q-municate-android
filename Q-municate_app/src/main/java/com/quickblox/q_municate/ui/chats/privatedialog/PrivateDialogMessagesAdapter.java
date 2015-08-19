package com.quickblox.q_municate.ui.chats.privatedialog;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.chats.base.BaseDialogMessagesAdapter;
import com.quickblox.q_municate.core.listeners.ChatUIHelperListener;
import com.quickblox.q_municate.ui.chats.emoji.EmojiTextView;
import com.quickblox.q_municate.ui.views.MaskedImageView;
import com.quickblox.q_municate.utils.DateUtils;
import com.quickblox.q_municate_core.models.CombinationMessage;
import com.quickblox.q_municate_core.qb.commands.QBUpdateStatusMessageCommand;
import com.quickblox.q_municate_core.utils.ChatUtils;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.models.Dialog;
import com.quickblox.q_municate_db.models.DialogNotification;
import com.quickblox.q_municate_db.models.State;

import java.util.List;

public class PrivateDialogMessagesAdapter extends BaseDialogMessagesAdapter {

    private static int EMPTY_POSITION = -1;

    private int lastRequestPosition = EMPTY_POSITION;
    private int lastInfoRequestPosition = EMPTY_POSITION;
    private PrivateDialogActivity.FriendOperationListener friendOperationListener;

    public PrivateDialogMessagesAdapter(Context context,
            PrivateDialogActivity.FriendOperationListener friendOperationListener,
            List<CombinationMessage> objectsList, ChatUIHelperListener chatUIHelperListener, Dialog dialog) {
        super(context, objectsList);
        this.friendOperationListener = friendOperationListener;
        this.chatUIHelperListener = chatUIHelperListener;
        this.dialog = dialog;
    }

    private int getItemViewType(CombinationMessage combinationMessage) {
        boolean ownMessage = !combinationMessage.isIncoming(currentUser.getId());
        if (combinationMessage.getNotificationType() == null) {
            if (ownMessage) {
                return TYPE_OWN_MESSAGE;
            } else {
                return TYPE_OPPONENT_MESSAGE;
            }
        } else {
            return TYPE_REQUEST_MESSAGE;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return getItemViewType(getItem(position));
    }

    @Override
    public int getViewTypeCount() {
        return COMMON_TYPE_COUNT;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder viewHolder;

        CombinationMessage combinationMessage = getItem(position);
        boolean ownMessage = !combinationMessage.isIncoming(currentUser.getId());

        if (view == null) {
            viewHolder = new ViewHolder();

            if (combinationMessage.getNotificationType() == null) {
                if (ownMessage) {
                    view = layoutInflater.inflate(R.layout.list_item_message_own, null, true);
                } else {
                    view = layoutInflater.inflate(R.layout.list_item_private_message_opponent, null, true);
            }

            viewHolder.attachMessageRelativeLayout = (RelativeLayout) view.findViewById(
                    R.id.attach_message_relativelayout);
            viewHolder.timeAttachMessageTextView = (TextView) view.findViewById(
                    R.id.time_attach_message_textview);
            viewHolder.attachDeliveryStatusImageView = (ImageView) view.findViewById(R.id.attach_message_delivery_status_imageview);
            viewHolder.progressRelativeLayout = (RelativeLayout) view.findViewById(
                    R.id.progress_relativelayout);
            viewHolder.textMessageView = view.findViewById(R.id.text_message_view);
            viewHolder.messageTextView = (EmojiTextView) view.findViewById(R.id.message_textview);
            viewHolder.attachImageView = (MaskedImageView) view.findViewById(R.id.attach_imageview);
            viewHolder.timeTextMessageTextView = (TextView) view.findViewById(
                    R.id.time_text_message_textview);
            viewHolder.verticalProgressBar = (ProgressBar) view.findViewById(R.id.vertical_progressbar);
            viewHolder.verticalProgressBar.setProgressDrawable(context.getResources().getDrawable(
                    R.drawable.vertical_progressbar));
            viewHolder.centeredProgressBar = (ProgressBar) view.findViewById(R.id.centered_progressbar);
            viewHolder.messageDeliveryStatusImageView = (ImageView) view.findViewById(R.id.text_message_delivery_status_imageview);

            } else {
                view = layoutInflater.inflate(R.layout.list_item_friends_notification_message, null, true);

                viewHolder.messageTextView = (EmojiTextView) view.findViewById(R.id.message_textview);
                viewHolder.timeTextMessageTextView = (TextView) view.findViewById(
                        R.id.time_text_message_textview);
                viewHolder.acceptFriendImageView = (ImageView) view.findViewById(
                        R.id.accept_friend_imagebutton);
                viewHolder.dividerView = view.findViewById(R.id.divider_view);
                viewHolder.rejectFriendImageView = (ImageView) view.findViewById(
                        R.id.reject_friend_imagebutton);
            }

            view.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        boolean friendsRequestMessage = DialogNotification.Type.FRIENDS_REQUEST.equals(
                combinationMessage.getNotificationType());
        boolean friendsInfoRequestMessage = combinationMessage
                .getNotificationType() != null && !friendsRequestMessage;

        if (friendsRequestMessage) {
            viewHolder.messageTextView.setText(combinationMessage.getBody());
            viewHolder.timeTextMessageTextView.setText(DateUtils.longToMessageDate(
                    combinationMessage.getCreatedDate()));

            setVisibilityFriendsActions(viewHolder, View.GONE);
        } else if (friendsInfoRequestMessage) {
            viewHolder.messageTextView.setText(combinationMessage.getBody());
            viewHolder.timeTextMessageTextView.setText(DateUtils.longToMessageDate(
                    combinationMessage.getCreatedDate()));

            setVisibilityFriendsActions(viewHolder, View.GONE);

            lastInfoRequestPosition = position;
        } else if (combinationMessage.getAttachment() != null) {
            resetUI(viewHolder);

            setViewVisibility(viewHolder.progressRelativeLayout, View.VISIBLE);
            viewHolder.timeAttachMessageTextView.setText(DateUtils.longToMessageDate(
                    combinationMessage.getCreatedDate()));

            if (ownMessage) {
                setMessageStatus(viewHolder.attachDeliveryStatusImageView, State.DELIVERED.equals(
                        combinationMessage.getState()), State.READ.equals(combinationMessage.getState()));
            }

            displayAttachImage(combinationMessage.getAttachment().getRemoteUrl(), viewHolder);
        } else {
            resetUI(viewHolder);

            setViewVisibility(viewHolder.textMessageView, View.VISIBLE);
            viewHolder.messageTextView.setText(combinationMessage.getBody());
            viewHolder.timeTextMessageTextView.setText(DateUtils.longToMessageDate(
                    combinationMessage.getCreatedDate()));

            if (ownMessage) {
                setMessageStatus(viewHolder.messageDeliveryStatusImageView, State.DELIVERED.equals(
                        combinationMessage.getState()), State.READ.equals(combinationMessage.getState()));
            }
        }

        if (!State.READ.equals(combinationMessage.getState()) && !ownMessage) {
            combinationMessage.setState(State.READ);
            QBUpdateStatusMessageCommand.start(context, ChatUtils.createQBDialogFromLocalDialog(dialog),
                    combinationMessage.toMessage(), true);
        }

        // check if last messageCombination is request messageCombination
        boolean lastRequestMessage = (position == objectsList.size() - 1 && friendsRequestMessage);
        if (lastRequestMessage) {
            findLastFriendsRequest();
        }

        // check if friend was rejected/deleted.
        if (lastRequestPosition != EMPTY_POSITION && lastRequestPosition < lastInfoRequestPosition) {
            lastRequestPosition = EMPTY_POSITION;
        } else if ((lastRequestPosition != EMPTY_POSITION && lastRequestPosition == position)) { // set visible friends actions
            setVisibilityFriendsActions(viewHolder, View.VISIBLE);
            initListeners(viewHolder, combinationMessage.getDialogOccupant().getUser().getUserId());
        }

        return view;
    }

    public void clearLastRequestMessagePosition() {
        lastRequestPosition = EMPTY_POSITION;
    }

    public void findLastFriendsRequestMessagesPosition() {
        new FindLastFriendsRequestThread().run();
    }

    private void setVisibilityFriendsActions(ViewHolder viewHolder, int visibility) {
        setViewVisibility(viewHolder.acceptFriendImageView, visibility);
        setViewVisibility(viewHolder.dividerView, visibility);
        setViewVisibility(viewHolder.rejectFriendImageView, visibility);
    }

    private void initListeners(ViewHolder viewHolder, final int userId) {
        viewHolder.acceptFriendImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                friendOperationListener.onAcceptUserClicked(userId);
            }
        });

        viewHolder.rejectFriendImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                friendOperationListener.onRejectUserClicked(userId);
            }
        });
    }

    private void findLastFriendsRequest() {
        for (int i = 0; i < objectsList.size(); i++) {
            findLastFriendsRequest(i, objectsList.get(i));
        }
    }

    private void findLastFriendsRequest(int position, CombinationMessage combinationMessage) {
        boolean ownMessage;
        boolean friendsRequestMessage;
        boolean isFriend;

        if (combinationMessage.getNotificationType() != null) {
            ownMessage = isOwnMessage(combinationMessage.getDialogOccupant().getUser().getUserId());
            friendsRequestMessage = DialogNotification.Type.FRIENDS_REQUEST.equals(
                    combinationMessage.getNotificationType());

            if (friendsRequestMessage && !ownMessage) {
                isFriend = DataManager.getInstance().getFriendDataManager().
                        getByUserId(combinationMessage.getDialogOccupant().getUser().getUserId()) != null;
                if (!isFriend) {
                    lastRequestPosition = position;
                }
            }
        }
    }

    private class FindLastFriendsRequestThread extends Thread {

        @Override
        public void run() {
            findLastFriendsRequest();
        }
    }
}