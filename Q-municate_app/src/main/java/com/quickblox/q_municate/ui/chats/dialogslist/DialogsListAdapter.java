package com.quickblox.q_municate.ui.chats.dialogslist;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.base.BaseActivity;
import com.quickblox.q_municate.ui.adapters.base.BaseListAdapter;
import com.quickblox.q_municate.ui.views.RoundedImageView;
import com.quickblox.q_municate_core.utils.ChatUtils;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_core.utils.UserFriendUtils;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.models.Dialog;
import com.quickblox.q_municate_db.models.DialogNotification;
import com.quickblox.q_municate_db.models.DialogOccupant;
import com.quickblox.q_municate_db.models.Message;
import com.quickblox.q_municate_db.models.User;

import java.util.List;

public class DialogsListAdapter extends BaseListAdapter<Dialog> {

    private DataManager dataManager;

    public DialogsListAdapter(BaseActivity baseActivity, List<Dialog> objectsList) {
        super(baseActivity, objectsList);
        dataManager = DataManager.getInstance();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        Dialog dialog = getItem(position);

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.list_item_dialog, null);

            viewHolder = new ViewHolder();

            viewHolder.avatarImageView = (RoundedImageView) convertView.findViewById(R.id.avatar_imageview);
            viewHolder.nameTextView = (TextView) convertView.findViewById(R.id.name_textview);
            viewHolder.lastMessageTextView = (TextView) convertView.findViewById(R.id.last_message_textview);
            viewHolder.unreadMessagesTextView = (TextView) convertView.findViewById(
                    R.id.unread_messages_textview);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        List<DialogOccupant> dialogOccupantsList = dataManager.getDialogOccupantDataManager().getDialogOccupantsListByDialogId(
                dialog.getDialogId());

        if (Dialog.Type.PRIVATE.equals(dialog.getType())) {
            User opponentUser = ChatUtils.getOpponentFromPrivateDialog(
                    UserFriendUtils.createLocalUser(currentUser), dialogOccupantsList);
            if (opponentUser.getFullName() != null) {
                viewHolder.nameTextView.setText(opponentUser.getFullName());
                displayAvatarImage(opponentUser.getAvatar(), viewHolder.avatarImageView);
            } else {
                viewHolder.nameTextView.setText("");
            }
        } else {
            viewHolder.nameTextView.setText(dialog.getTitle());
            viewHolder.avatarImageView.setImageResource(R.drawable.placeholder_group);
            displayGroupPhotoImage(dialog.getPhoto(), viewHolder.avatarImageView);
        }

        List<Integer> dialogOccupantsIdsList = ChatUtils.getIdsFromDialogOccupantsList(dialogOccupantsList);
        long unreadMessages = dataManager.getMessageDataManager().getCountUnreadMessages(
                dialogOccupantsIdsList, currentUser.getId());
        if (unreadMessages > ConstsCore.ZERO_INT_VALUE) {
            viewHolder.unreadMessagesTextView.setText(unreadMessages + ConstsCore.EMPTY_STRING);
            viewHolder.unreadMessagesTextView.setVisibility(View.VISIBLE);
        } else {
            viewHolder.unreadMessagesTextView.setVisibility(View.GONE);
        }

        Message message = dataManager.getMessageDataManager().getLastMessageWithTempByDialogId(
                dialogOccupantsIdsList);
        DialogNotification dialogNotification = dataManager.getDialogNotificationDataManager()
                .getLastDialogNotificationByDialogId(dialogOccupantsIdsList);

        viewHolder.lastMessageTextView.setText(getLastMessage(message, dialogNotification));

        return convertView;
    }

    private String getLastMessage(Message message, DialogNotification dialogNotification) {
        String lastMessage = "";

        if (message == null && dialogNotification != null) {
            lastMessage = dialogNotification.getBody();
        } else if (dialogNotification == null && message != null) {
            lastMessage = message.getBody();
        } else if (message != null && dialogNotification != null) {
            lastMessage = message.getCreatedDate() > dialogNotification.getCreatedDate() ? message.getBody() : dialogNotification.getBody();
        }

        return lastMessage;
    }

    private static class ViewHolder {

        public RoundedImageView avatarImageView;
        public TextView nameTextView;
        public TextView lastMessageTextView;
        public TextView unreadMessagesTextView;
    }
}