package com.quickblox.q_municate.ui.activities.chats;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.quickblox.chat.model.QBDialog;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.activities.others.BaseFriendsListActivity;
import com.quickblox.q_municate.ui.adapters.friends.FriendsAdapter;
import com.quickblox.q_municate.ui.views.roundedimageview.RoundedImageView;
import com.quickblox.q_municate_core.core.command.Command;
import com.quickblox.q_municate_core.qb.commands.QBCreateGroupDialogCommand;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.ChatUtils;
import com.quickblox.q_municate_core.utils.ErrorUtils;
import com.quickblox.q_municate_db.models.User;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;

public class CreateGroupDialogActivity extends BaseFriendsListActivity {

    private static final String EXTRA_FRIENDS_LIST = "friends_list";

    @Bind(R.id.photo_imageview)
    RoundedImageView photoImageView;

    @Bind(R.id.group_name_edittext)
    EditText groupNameEditText;

    private List<User> friendsList;

    public static void start(Context context, List<User> selectedFriendsList) {
        Intent intent = new Intent(context, CreateGroupDialogActivity.class);
        intent.putExtra(EXTRA_FRIENDS_LIST, (Serializable) selectedFriendsList);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group_dailog);

        activateButterKnife();

        initFields();

        addActions();
    }

    private void initFields() {
        friendsList = (List<User>) getIntent().getExtras().getSerializable(EXTRA_FRIENDS_LIST);
    }

    @Override
    protected FriendsAdapter getFriendsAdapter() {
        return new FriendsAdapter(this, friendsList);
    }

    @OnClick(R.id.photo_imageview)
    void selectPhoto(View view) {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeActions();
    }

    @Override
    protected void performDone() {
        groupNameEditText.setError(null);

        if (!TextUtils.isEmpty(groupNameEditText.getText())) {
            createGroupChat();
        } else {
            groupNameEditText.setError(getString(R.string.create_group_empty_group_name));
        }
    }

    protected void removeActions() {
        removeAction(QBServiceConsts.CREATE_GROUP_CHAT_SUCCESS_ACTION);
        removeAction(QBServiceConsts.CREATE_GROUP_CHAT_FAIL_ACTION);

        updateBroadcastActionList();
    }

    protected void addActions() {
        addAction(QBServiceConsts.CREATE_GROUP_CHAT_SUCCESS_ACTION, new CreateGroupChatSuccessAction());
        addAction(QBServiceConsts.CREATE_GROUP_CHAT_FAIL_ACTION, failAction);

        updateBroadcastActionList();
    }

    private void createGroupChat() {
        if (friendsList != null && !friendsList.isEmpty()) {
            showProgress();
            QBCreateGroupDialogCommand.start(this, groupNameEditText.getText().toString(), (ArrayList<User>) friendsList);
        }
    }

    private class CreateGroupChatSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            hideProgress();
            QBDialog dialog = (QBDialog) bundle.getSerializable(QBServiceConsts.EXTRA_DIALOG);

            if (dialog != null) {
                GroupDialogActivity.start(CreateGroupDialogActivity.this, ChatUtils.createLocalDialog(dialog));
                finish();
            } else {
                ErrorUtils.showError(CreateGroupDialogActivity.this, getString(R.string.dlg_fail_create_groupchat));
            }
        }
    }
}