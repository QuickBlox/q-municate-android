package com.quickblox.q_municate.ui.chats.dialogs;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.base.BaseActivity;
import com.quickblox.q_municate.ui.base.BaseListAdapter;
import com.quickblox.q_municate.ui.views.RoundedImageView;
import com.quickblox.q_municate_core.utils.ChatUtils;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_core.utils.UserFriendUtils;
import com.quickblox.q_municate_db.managers.DatabaseManager;
import com.quickblox.q_municate_db.models.Dialog;
import com.quickblox.q_municate_db.models.DialogOccupant;
import com.quickblox.q_municate_db.models.DialogType;
import com.quickblox.q_municate_db.models.Message;
import com.quickblox.q_municate_db.models.State;
import com.quickblox.q_municate_db.models.User;

import java.util.List;

public class DialogsAdapter extends BaseListAdapter<Dialog> {

    private DatabaseManager databaseManager;

    public DialogsAdapter(BaseActivity baseActivity, List<Dialog> objectsList) {
        super(baseActivity, objectsList);
        databaseManager = DatabaseManager.getInstance();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

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

        Dialog dialog = getItem(position);
        List<DialogOccupant> dialogOccupantsList = databaseManager.getDialogOccupantManager().getDialogOccupantsListByDialog(dialog.getDialogId());

        if (DialogType.Type.PRIVATE.equals(dialog.getDialogType().getType())) {
            User opponentUser = ChatUtils.getOpponentFromPrivateDialog(UserFriendUtils.createLocalUser(currentQBUser), dialogOccupantsList);
            viewHolder.nameTextView.setText(opponentUser.getFullName());
            displayAvatarImage(opponentUser.getAvatar(), viewHolder.avatarImageView);
        } else {
            viewHolder.nameTextView.setText(dialog.getTitle());
            viewHolder.avatarImageView.setImageResource(R.drawable.placeholder_group);
            displayGroupPhotoImage(dialog.getPhoto(), viewHolder.avatarImageView);
        }

        State unreadMessageState = databaseManager.getStateManager().getByStateType(State.Type.READ);
        List<Integer> dialogOccupantsIdsList = ChatUtils.getIdsFromDialogOccupantsList(dialogOccupantsList);
        long unreadMessages = databaseManager.getMessageManager().getCountUnreadMessages(dialogOccupantsIdsList, unreadMessageState);
        if (unreadMessages > ConstsCore.ZERO_INT_VALUE) {
            viewHolder.unreadMessagesTextView.setText(unreadMessages + ConstsCore.EMPTY_STRING);
            viewHolder.unreadMessagesTextView.setVisibility(View.VISIBLE);
        } else {
            viewHolder.unreadMessagesTextView.setVisibility(View.GONE);
        }

        Message message = databaseManager.getMessageManager().getLastMessageByDialogId(dialogOccupantsIdsList);

        if (message != null) {
            viewHolder.lastMessageTextView.setText(message.getBody());
        }

        return convertView;
    }

    private static class ViewHolder {

        public RoundedImageView avatarImageView;
        public TextView nameTextView;
        public TextView lastMessageTextView;
        public TextView unreadMessagesTextView;
    }
}