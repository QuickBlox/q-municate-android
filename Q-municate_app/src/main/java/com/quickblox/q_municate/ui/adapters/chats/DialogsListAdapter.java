package com.quickblox.q_municate.ui.adapters.chats;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.activities.base.BaseActivity;
import com.quickblox.q_municate.ui.adapters.base.BaseListAdapter;
import com.quickblox.q_municate.ui.views.roundedimageview.RoundedImageView;
import com.quickblox.q_municate_core.models.DialogWrapper;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_user_service.model.QMUser;

import java.util.Iterator;
import java.util.List;

public class DialogsListAdapter extends BaseListAdapter<DialogWrapper> {

    private static final String TAG = DialogsListAdapter.class.getSimpleName();

    public DialogsListAdapter(BaseActivity baseActivity, List<DialogWrapper> objectsList) {
        super(baseActivity, objectsList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        DialogWrapper dialogWrapper = getItem(position);
        QBChatDialog currentDialog = dialogWrapper.getChatDialog();

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.item_dialog, null);

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

        if (QBDialogType.PRIVATE.equals(currentDialog.getType())) {
            QMUser opponentUser = dialogWrapper.getOpponentUser();
            if (opponentUser.getFullName() != null) {
                viewHolder.nameTextView.setText(opponentUser.getFullName());
                displayAvatarImage(opponentUser.getAvatar(), viewHolder.avatarImageView);
            } else {
                viewHolder.nameTextView.setText(resources.getString(R.string.deleted_user));
            }
        } else {
            viewHolder.nameTextView.setText(currentDialog.getName());
            viewHolder.avatarImageView.setImageResource(R.drawable.placeholder_group);
            displayGroupPhotoImage(currentDialog.getPhoto(), viewHolder.avatarImageView);
        }

        long totalCount = dialogWrapper.getTotalCount();

        if (totalCount > ConstsCore.ZERO_INT_VALUE) {
            viewHolder.unreadMessagesTextView.setText(totalCount + ConstsCore.EMPTY_STRING);
            viewHolder.unreadMessagesTextView.setVisibility(View.VISIBLE);
        } else {
            viewHolder.unreadMessagesTextView.setVisibility(View.GONE);
        }

        viewHolder.lastMessageTextView.setText(dialogWrapper.getLastMessage());

        return convertView;
    }

    public void updateItem(DialogWrapper dlgWrapper) {
        Log.i(TAG, "updateItem = " + dlgWrapper.getChatDialog().getUnreadMessageCount());
        int position = -1;
        for (int i = 0; i < objectsList.size() ; i++) {
            DialogWrapper dialogWrapper  = objectsList.get(i);
            if (dialogWrapper.getChatDialog().getDialogId().equals(dlgWrapper.getChatDialog().getDialogId())){
                position = i;
                break;
            }
        }

        if (position != -1) {
            Log.i(TAG, "find position = " + position);
            objectsList.set(position, dlgWrapper);
        } else {
            addNewItem(dlgWrapper);
        }
    }

    public void moveToFirstPosition(DialogWrapper dlgWrapper) {
        if (objectsList.size() != 0 && !objectsList.get(0).equals(dlgWrapper)) {
            objectsList.remove(dlgWrapper);
            objectsList.add(0, dlgWrapper);
            notifyDataSetChanged();
        }
    }

    public void removeItem(String dialogId) {
        Iterator<DialogWrapper> iterator = objectsList.iterator();

        while (iterator.hasNext()){
            DialogWrapper dialogWrapper = iterator.next();
            if (dialogWrapper.getChatDialog().getDialogId().equals(dialogId)){
                iterator.remove();
                notifyDataSetChanged();
                break;
            }
        }

    }

    private static class ViewHolder {

        public RoundedImageView avatarImageView;
        public TextView nameTextView;
        public TextView lastMessageTextView;
        public TextView unreadMessagesTextView;
    }
}