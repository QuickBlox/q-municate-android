package com.quickblox.q_municate.ui.adapters.search;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.activities.base.BaseActivity;
import com.quickblox.q_municate.ui.adapters.base.BaseClickListenerViewHolder;
import com.quickblox.q_municate.ui.adapters.base.BaseViewHolder;
import com.quickblox.q_municate.ui.adapters.base.BaseFilterAdapter;
import com.quickblox.q_municate.ui.views.roundedimageview.RoundedImageView;
import com.quickblox.q_municate.utils.DateUtils;
import com.quickblox.q_municate.utils.helpers.TextViewHelper;
import com.quickblox.q_municate.utils.image.ImageLoaderUtils;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.qb.helpers.QBFriendListHelper;
import com.quickblox.q_municate_core.utils.ChatUtils;
import com.quickblox.q_municate_core.utils.OnlineStatusUtils;
import com.quickblox.q_municate_core.utils.UserFriendUtils;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.models.Dialog;
import com.quickblox.q_municate_db.models.DialogNotification;
import com.quickblox.q_municate_db.models.DialogOccupant;
import com.quickblox.q_municate_db.models.Message;
import com.quickblox.q_municate_db.models.User;

import java.util.List;

import butterknife.Bind;

public class LocalSearchAdapter extends BaseFilterAdapter<Dialog, BaseClickListenerViewHolder<Dialog>> {

    private DataManager dataManager;
    private QBFriendListHelper qbFriendListHelper;

    public LocalSearchAdapter(BaseActivity baseActivity, List<Dialog> list) {
        super(baseActivity, list);
        dataManager = DataManager.getInstance();
    }

    @Override
    protected boolean isMatch(Dialog item, String query) {
        return item.getTitle() != null && item.getTitle().toLowerCase().contains(query);
    }

    @Override
    public BaseClickListenerViewHolder<Dialog> onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(this, layoutInflater.inflate(R.layout.item_local_search, parent, false));
    }

    @Override
    public void onBindViewHolder(BaseClickListenerViewHolder<Dialog> baseClickListenerViewHolder, int position) {
        Dialog dialog = getItem(position);
        ViewHolder viewHolder = (ViewHolder) baseClickListenerViewHolder;

        List<DialogOccupant> dialogOccupantsList = dataManager.getDialogOccupantDataManager()
                .getDialogOccupantsListByDialogId(dialog.getDialogId());

        String label;

        if (Dialog.Type.PRIVATE.equals(dialog.getType())) {
            User currentUser =  UserFriendUtils.createLocalUser(AppSession.getSession().getUser());
            User opponentUser = ChatUtils.getOpponentFromPrivateDialog(currentUser, dialogOccupantsList);
            setOnlineStatus(viewHolder, opponentUser);
            displayAvatarImage(opponentUser.getAvatar(), viewHolder.avatarImageView);
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
            displayGroupPhotoImage(dialog.getPhoto(), viewHolder.avatarImageView);
        }

        viewHolder.titleTextView.setText(dialog.getTitle());

        if (!TextUtils.isEmpty(query)) {
            TextViewHelper.changeTextColorView(baseActivity, viewHolder.titleTextView, query);
        }
    }

    public void setFriendListHelper(QBFriendListHelper qbFriendListHelper) {
        this.qbFriendListHelper = qbFriendListHelper;
        notifyDataSetChanged();
    }

    private void setOnlineStatus(ViewHolder viewHolder, User user) {
        boolean online = qbFriendListHelper != null && qbFriendListHelper.isUserOnline(user.getUserId());

        if (online) {
            viewHolder.labelTextView.setText(OnlineStatusUtils.getOnlineStatus(online));
            viewHolder.labelTextView.setTextColor(resources.getColor(R.color.green));
        } else {
            viewHolder.labelTextView.setText(resources.getString(R.string.last_seen,
                    DateUtils.toTodayYesterdayShortDateWithoutYear2(user.getLastLogin()),
                    DateUtils.formatDateSimpleTime(user.getLastLogin())));
            viewHolder.labelTextView.setTextColor(resources.getColor(R.color.dark_gray));
        }
    }

    private void displayGroupPhotoImage(String uri, ImageView imageView) {
        ImageLoader.getInstance().displayImage(uri, imageView,
                ImageLoaderUtils.UIL_GROUP_AVATAR_DISPLAY_OPTIONS);
    }

    protected static class ViewHolder extends BaseViewHolder<Dialog> {

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