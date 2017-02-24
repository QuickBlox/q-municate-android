package com.quickblox.q_municate.ui.adapters.search;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.activities.base.BaseActivity;
import com.quickblox.q_municate.ui.adapters.base.BaseClickListenerViewHolder;
import com.quickblox.q_municate.ui.adapters.base.BaseViewHolder;
import com.quickblox.q_municate.ui.adapters.base.BaseFilterAdapter;
import com.quickblox.q_municate.ui.views.roundedimageview.RoundedImageView;
import com.quickblox.q_municate.utils.ChatDialogUtils;
import com.quickblox.q_municate.utils.DateUtils;
import com.quickblox.q_municate.utils.helpers.TextViewHelper;
import com.quickblox.q_municate.utils.image.ImageLoaderUtils;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.qb.helpers.QBFriendListHelper;
import com.quickblox.q_municate_core.utils.ChatUtils;
import com.quickblox.q_municate_core.utils.OnlineStatusUtils;
import com.quickblox.q_municate_core.utils.UserFriendUtils;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.models.DialogNotification;
import com.quickblox.q_municate_db.models.DialogOccupant;
import com.quickblox.q_municate_db.models.Message;
import com.quickblox.q_municate_user_service.model.QMUser;

import java.util.List;

import butterknife.Bind;

public class LocalSearchAdapter extends BaseFilterAdapter<QBChatDialog, BaseClickListenerViewHolder<QBChatDialog>> {

    private DataManager dataManager;
    private QBFriendListHelper qbFriendListHelper;

    public LocalSearchAdapter(BaseActivity baseActivity, List<QBChatDialog> list) {
        super(baseActivity, list);
        dataManager = DataManager.getInstance();
    }

    @Override
    protected boolean isMatch(QBChatDialog item, String query) {
        String chatTitle = ChatDialogUtils.getTitleForChatDialog(item, dataManager);
        return chatTitle != null && chatTitle.toLowerCase().contains(query);
    }

    @Override
    public BaseClickListenerViewHolder<QBChatDialog> onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(this, layoutInflater.inflate(R.layout.item_local_search, parent, false));
    }

    @Override
    public void onBindViewHolder(BaseClickListenerViewHolder<QBChatDialog> baseClickListenerViewHolder, int position) {
        QBChatDialog chatDialog = getItem(position);
        ViewHolder viewHolder = (ViewHolder) baseClickListenerViewHolder;

        List<DialogOccupant> dialogOccupantsList = dataManager.getDialogOccupantDataManager()
                .getDialogOccupantsListByDialogId(chatDialog.getDialogId());

        String label;

        if (QBDialogType.PRIVATE.equals(chatDialog.getType())) {
            QMUser currentUser =  UserFriendUtils.createLocalUser(AppSession.getSession().getUser());
            QMUser opponentUser = ChatUtils.getOpponentFromPrivateDialog(currentUser, dialogOccupantsList);
            setOnlineStatus(viewHolder, opponentUser);
            displayAvatarImage(opponentUser.getAvatar(), viewHolder.avatarImageView);
            viewHolder.titleTextView.setText(opponentUser.getFullName());
        } else {
            List<Long> dialogOccupantsIdsList = ChatUtils.getIdsFromDialogOccupantsList(dialogOccupantsList);
            Message message = dataManager.getMessageDataManager().getLastMessageWithTempByDialogId(dialogOccupantsIdsList);
            DialogNotification dialogNotification = dataManager.getDialogNotificationDataManager()
                    .getLastDialogNotificationByDialogId(dialogOccupantsIdsList);
            label = ChatUtils.getDialogLastMessage(
                    resources.getString(R.string.cht_notification_message),
                    message,
                    dialogNotification);

            viewHolder.labelTextView.setText(label);
            viewHolder.labelTextView.setTextColor(resources.getColor(R.color.dark_gray));
            displayGroupPhotoImage(chatDialog.getPhoto(), viewHolder.avatarImageView);
            viewHolder.titleTextView.setText(chatDialog.getName());
        }

        if (!TextUtils.isEmpty(query)) {
            TextViewHelper.changeTextColorView(baseActivity, viewHolder.titleTextView, query);
        }
    }

    public void setFriendListHelper(QBFriendListHelper qbFriendListHelper) {
        this.qbFriendListHelper = qbFriendListHelper;
        notifyDataSetChanged();
    }

    private void setOnlineStatus(ViewHolder viewHolder, QMUser user) {
        boolean online = qbFriendListHelper != null && user.getId()!= null && qbFriendListHelper.isUserOnline(user.getId());

        if (online) {
            viewHolder.labelTextView.setText(OnlineStatusUtils.getOnlineStatus(online));
            viewHolder.labelTextView.setTextColor(resources.getColor(R.color.green));
        } else {
            viewHolder.labelTextView.setText(user.getLastRequestAt() == null ? null : resources.getString(R.string.last_seen,
                    DateUtils.toTodayYesterdayShortDateWithoutYear2(user.getLastRequestAt().getTime()),
                    DateUtils.formatDateSimpleTime(user.getLastRequestAt().getTime())));
            viewHolder.labelTextView.setTextColor(resources.getColor(R.color.dark_gray));
        }
    }

    private void displayGroupPhotoImage(String uri, ImageView imageView) {
        ImageLoader.getInstance().displayImage(uri, imageView,
                ImageLoaderUtils.UIL_GROUP_AVATAR_DISPLAY_OPTIONS);
    }

    protected static class ViewHolder extends BaseViewHolder<QBChatDialog> {

        @Bind(R.id.avatar_imageview)
        RoundedImageView avatarImageView;

        @Bind(R.id.title_textview)
        TextView titleTextView;

        @Bind(R.id.label_textview)
        TextView labelTextView;

        public ViewHolder(LocalSearchAdapter adapter, View view) {
            super(adapter, view);
        }
    }
}