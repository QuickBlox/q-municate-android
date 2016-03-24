package com.quickblox.q_municate.ui.activities.chats;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.afollestad.materialdialogs.MaterialDialog;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.utils.helpers.ImagePickHelper;
import com.quickblox.q_municate.utils.listeners.ChatUIHelperListener;
import com.quickblox.q_municate.utils.listeners.OnImagePickedListener;
import com.quickblox.q_municate.ui.activities.base.BaseLoggableActivity;
import com.quickblox.q_municate.ui.adapters.base.BaseRecyclerViewAdapter;
import com.quickblox.q_municate_core.core.loader.BaseLoader;
import com.quickblox.q_municate.ui.fragments.dialogs.base.TwoButtonsDialogFragment;
import com.quickblox.q_municate.utils.image.ImageLoaderUtils;
import com.quickblox.q_municate.utils.image.ImageUtils;
import com.quickblox.q_municate.utils.KeyboardUtils;
import com.quickblox.q_municate_core.core.command.Command;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.models.CombinationMessage;
import com.quickblox.q_municate_core.qb.commands.QBLoadAttachFileCommand;
import com.quickblox.q_municate_core.qb.commands.chat.QBLoadDialogMessagesCommand;
import com.quickblox.q_municate_core.qb.helpers.QBBaseChatHelper;
import com.quickblox.q_municate_core.qb.helpers.QBGroupChatHelper;
import com.quickblox.q_municate_core.qb.helpers.QBPrivateChatHelper;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.ChatUtils;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.managers.DialogDataManager;
import com.quickblox.q_municate_db.managers.DialogNotificationDataManager;
import com.quickblox.q_municate_db.managers.MessageDataManager;
import com.quickblox.q_municate_db.models.Dialog;
import com.quickblox.q_municate_db.models.DialogNotification;
import com.quickblox.q_municate_db.models.DialogOccupant;
import com.quickblox.q_municate_db.models.Message;
import com.quickblox.q_municate_db.models.User;
import com.quickblox.q_municate_db.utils.ErrorUtils;
import com.rockerhieu.emojicon.EmojiconGridFragment;
import com.rockerhieu.emojicon.EmojiconsFragment;
import com.rockerhieu.emojicon.emoji.Emojicon;

import java.io.File;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import butterknife.OnTouch;

public abstract class BaseDialogActivity extends BaseLoggableActivity implements
        EmojiconGridFragment.OnEmojiconClickedListener, EmojiconsFragment.OnEmojiconBackspaceClickedListener,
        ChatUIHelperListener, OnImagePickedListener {

    private static final int TYPING_DELAY = 1000;
    private static final int DELAY_SCROLLING_LIST = 300;
    private static final int DELAY_SHOWING_SMILE_PANEL = 200;

    @Bind(R.id.messages_swiperefreshlayout)
    SwipeRefreshLayout messageSwipeRefreshLayout;

    @Bind(R.id.messages_recycleview)
    RecyclerView messagesRecyclerView;

    @Bind(R.id.message_edittext)
    EditText messageEditText;

    @Bind(R.id.attach_button)
    ImageButton attachButton;

    @Bind(R.id.send_button)
    ImageButton sendButton;

    @Bind(R.id.smile_panel_imagebutton)
    ImageButton smilePanelImageButton;

    protected Dialog dialog;
    protected Resources resources;
    protected DataManager dataManager;
    protected ImageUtils imageUtils;
    protected BaseRecyclerViewAdapter messagesAdapter;
    protected User opponentUser;
    protected QBBaseChatHelper baseChatHelper;
    protected List<CombinationMessage> combinationMessagesList;
    protected int chatHelperIdentifier;
    protected ImagePickHelper imagePickHelper;

    private Handler mainThreadHandler;
    private View emojiconsFragment;
    private LoadAttachFileSuccessAction loadAttachFileSuccessAction;
    private LoadDialogMessagesSuccessAction loadDialogMessagesSuccessAction;
    private LoadDialogMessagesFailAction loadDialogMessagesFailAction;
    private Timer typingTimer;
    private boolean isTypingNow;
    private Observer dialogObserver;
    private Observer messageObserver;
    private Observer dialogNotificationObserver;
    private BroadcastReceiver typingMessageBroadcastReceiver;
    private BroadcastReceiver updatingDialogBroadcastReceiver;
    private boolean loadMore;

    @Override
    protected int getContentResId() {
        return R.layout.activity_dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initFields();

        initCustomUI();
        initCustomListeners();

        addActions();
        addObservers();
        registerBroadcastReceivers();

        hideSmileLayout();
    }

    @OnTextChanged(R.id.message_edittext)
    void messageEditTextChanged(CharSequence charSequence) {
        setActionButtonVisibility(charSequence);

        // TODO: now it is possible only for Private chats
        if (dialog != null && Dialog.Type.PRIVATE.equals(dialog.getType())) {
            if (!isTypingNow) {
                isTypingNow = true;
                sendTypingStatus();
            }
            checkStopTyping();
        }
    }

    @OnTouch(R.id.message_edittext)
    boolean touchMessageEdit() {
        hideSmileLayout();
        scrollMessagesWithDelay();
        return false;
    }

    @OnClick(R.id.smile_panel_imagebutton)
    void smilePanelImageButtonClicked() {
        visibleOrHideSmilePanel();
        scrollMessagesWithDelay();
    }

    @OnClick(R.id.attach_button)
    void attachFile(View view) {
        imagePickHelper.pickAnImage(this, ImageUtils.IMAGE_REQUEST_CODE);
    }

    @Override
    public void onBackPressed() {
        if (isSmilesLayoutShowing()) {
            hideSmileLayout();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        createChatLocally();
    }

    @Override
    protected void onPause() {
        super.onPause();

        hideSmileLayout();
        checkStartTyping();
    }

    @Override
    protected void onStop() {
        super.onStop();
        readAllMessages();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeChatLocally();
        removeActions();
        deleteObservers();
        unregisterBroadcastReceivers();
    }

    @Override
    public void onConnectedToService(QBService service) {
        super.onConnectedToService(service);
        onConnectServiceLocally(service);
    }

    @Override
    public void onEmojiconClicked(Emojicon emojicon) {
        EmojiconsFragment.input(messageEditText, emojicon);
    }

    @Override
    public void onEmojiconBackspaceClicked(View v) {
        EmojiconsFragment.backspace(messageEditText);
    }

    @Override
    public void onImagePicked(int requestCode, File file) {
        canPerformLogout.set(true);
        startLoadAttachFile(file);
    }

    @Override
    public void onImagePickClosed(int requestCode) {
        canPerformLogout.set(true);
    }

    @Override
    public void onImagePickError(int requestCode, Exception e) {
        canPerformLogout.set(true);
        ErrorUtils.logError(e);
    }

    @Override
    public void onScreenResetPossibilityPerformLogout(boolean canPerformLogout) {
        this.canPerformLogout.set(canPerformLogout);
    }

    @Override
    protected void loadDialogs() {
        super.loadDialogs();
        createChatLocally();
        checkMessageSendingPossibility();
    }

    @Override
    protected void checkShowingConnectionError() {
        if (!isNetworkAvailable()) {
            setActionBarTitle(getString(R.string.dlg_internet_connection_is_missing));
            setActionBarIcon(null);
        } else {
            setActionBarTitle(title);
            updateActionBar();
        }
    }

    @Override
    protected void performLoginChatSuccessAction(Bundle bundle) {
        super.performLoginChatSuccessAction(bundle);
        if (groupChatHelper != null) {
            QBDialog qbDialog = ChatUtils.createQBDialogFromLocalDialog(dataManager, dialog);
            groupChatHelper.tryJoinRoomChat(qbDialog);
        }

        startLoadDialogMessages();
    }

    private void initFields() {
        mainThreadHandler = new Handler(Looper.getMainLooper());
        resources = getResources();
        dataManager = DataManager.getInstance();
        imageUtils = new ImageUtils(this);
        loadAttachFileSuccessAction = new LoadAttachFileSuccessAction();
        loadDialogMessagesSuccessAction = new LoadDialogMessagesSuccessAction();
        loadDialogMessagesFailAction = new LoadDialogMessagesFailAction();
        typingTimer = new Timer();
        dialogObserver = new DialogObserver();
        messageObserver = new MessageObserver();
        dialogNotificationObserver = new DialogNotificationObserver();
        typingMessageBroadcastReceiver = new TypingStatusBroadcastReceiver();
        updatingDialogBroadcastReceiver = new UpdatingDialogBroadcastReceiver();
        appSharedHelper.saveNeedToOpenDialog(false);
        imagePickHelper = new ImagePickHelper();
    }

    private void initCustomUI() {
        emojiconsFragment = _findViewById(R.id.emojicon_fragment);
    }

    private void initCustomListeners() {
        messageSwipeRefreshLayout.setOnRefreshListener(new RefreshLayoutListener());
    }

    protected void initMessagesRecyclerView() {
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        messagesRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    protected void addActions() {
        addAction(QBServiceConsts.LOAD_ATTACH_FILE_SUCCESS_ACTION, loadAttachFileSuccessAction);
        addAction(QBServiceConsts.LOAD_ATTACH_FILE_FAIL_ACTION, failAction);

        addAction(QBServiceConsts.LOAD_DIALOG_MESSAGES_SUCCESS_ACTION, loadDialogMessagesSuccessAction);
        addAction(QBServiceConsts.LOAD_DIALOG_MESSAGES_FAIL_ACTION, loadDialogMessagesFailAction);

        updateBroadcastActionList();
    }

    protected void removeActions() {
        removeAction(QBServiceConsts.LOAD_ATTACH_FILE_SUCCESS_ACTION);
        removeAction(QBServiceConsts.LOAD_ATTACH_FILE_FAIL_ACTION);

        removeAction(QBServiceConsts.LOAD_DIALOG_MESSAGES_SUCCESS_ACTION);
        removeAction(QBServiceConsts.LOAD_DIALOG_MESSAGES_FAIL_ACTION);

        removeAction(QBServiceConsts.ACCEPT_FRIEND_SUCCESS_ACTION);
        removeAction(QBServiceConsts.ACCEPT_FRIEND_FAIL_ACTION);

        removeAction(QBServiceConsts.REJECT_FRIEND_SUCCESS_ACTION);
        removeAction(QBServiceConsts.REJECT_FRIEND_FAIL_ACTION);

        updateBroadcastActionList();
    }

    private void registerBroadcastReceivers() {
        localBroadcastManager.registerReceiver(typingMessageBroadcastReceiver,
                new IntentFilter(QBServiceConsts.TYPING_MESSAGE));
        localBroadcastManager.registerReceiver(updatingDialogBroadcastReceiver,
                new IntentFilter(QBServiceConsts.UPDATE_DIALOG));
    }

    private void unregisterBroadcastReceivers() {
        localBroadcastManager.unregisterReceiver(updatingDialogBroadcastReceiver);
        localBroadcastManager.unregisterReceiver(typingMessageBroadcastReceiver);
    }

    private void addObservers() {
        dataManager.getDialogDataManager().addObserver(dialogObserver);
        dataManager.getMessageDataManager().addObserver(messageObserver);
        dataManager.getDialogNotificationDataManager().addObserver(dialogNotificationObserver);
    }

    private void deleteObservers() {
        dataManager.getDialogDataManager().deleteObserver(dialogObserver);
        dataManager.getMessageDataManager().deleteObserver(messageObserver);
        dataManager.getDialogNotificationDataManager().deleteObserver(dialogNotificationObserver);
    }

    protected void updateData() {
        dialog = dataManager.getDialogDataManager().getByDialogId(dialog.getDialogId());
        if (dialog != null) {
            updateActionBar();
        }
        updateMessagesList();
    }

    protected void onConnectServiceLocally() {
        createChatLocally();
    }

    private void showTypingStatus() {
        setActionBarSubtitle(R.string.dialog_now_typing);
    }

    private void hideTypingStatus() {
        setActionBarSubtitle(null);
    }

    protected void deleteTempMessages() {
        List<DialogOccupant> dialogOccupantsList = dataManager.getDialogOccupantDataManager()
                .getDialogOccupantsListByDialogId(dialog.getDialogId());
        List<Long> dialogOccupantsIdsList = ChatUtils.getIdsFromDialogOccupantsList(dialogOccupantsList);
        dataManager.getMessageDataManager().deleteTempMessages(dialogOccupantsIdsList);
    }

    private void hideSmileLayout() {
        emojiconsFragment.setVisibility(View.GONE);
        setSmilePanelIcon(R.drawable.ic_smile_dark);
    }

    private void showSmileLayout() {
        mainThreadHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                emojiconsFragment.setVisibility(View.VISIBLE);
                setSmilePanelIcon(R.drawable.ic_keyboard_dark);
            }
        }, DELAY_SHOWING_SMILE_PANEL);
    }

    protected void checkActionBarLogo(String url, int resId) {
        if (!TextUtils.isEmpty(url)) {
            loadActionBarLogo(url);
        } else {
            setDefaultActionBarLogo(resId);
        }
    }

    protected void loadActionBarLogo(String logoUrl) {
        ImageLoader.getInstance().loadImage(logoUrl, ImageLoaderUtils.UIL_USER_AVATAR_DISPLAY_OPTIONS,
                new SimpleImageLoadingListener() {

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedBitmap) {
                        setActionBarIcon(
                                ImageUtils.getRoundIconDrawable(BaseDialogActivity.this, loadedBitmap));
                    }
                });
    }

    protected void setDefaultActionBarLogo(int drawableResId) {
        setActionBarIcon(ImageUtils
                .getRoundIconDrawable(this, BitmapFactory.decodeResource(getResources(), drawableResId)));
    }

    protected void startLoadAttachFile(final File file) {
        TwoButtonsDialogFragment.show(getSupportFragmentManager(), R.string.dialog_confirm_sending_attach,
                new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        showProgress();
                        QBLoadAttachFileCommand.start(BaseDialogActivity.this, file);
                    }
                });
    }

    protected void startLoadDialogMessages(Dialog dialog, long lastDateLoad) {
        QBLoadDialogMessagesCommand.start(this, ChatUtils.createQBDialogFromLocalDialog(dataManager, dialog),
                lastDateLoad, loadMore);
    }

    private void setActionButtonVisibility(CharSequence charSequence) {
        if (TextUtils.isEmpty(charSequence) || TextUtils.isEmpty(charSequence.toString().trim())) {
            sendButton.setVisibility(View.GONE);
            attachButton.setVisibility(View.VISIBLE);
        } else {
            sendButton.setVisibility(View.VISIBLE);
            attachButton.setVisibility(View.GONE);
        }
    }

    private void checkStopTyping() {
        typingTimer.cancel();
        typingTimer = new Timer();
        typingTimer.schedule(new TypingTimerTask(), TYPING_DELAY);
    }

    private void checkStartTyping() {
        // TODO: now it is possible only for Private chats
        if (dialog != null && Dialog.Type.PRIVATE.equals(dialog.getType())) {
            if (isTypingNow) {
                isTypingNow = false;
                sendTypingStatus();
            }
        }
    }

    private void sendTypingStatus() {
        baseChatHelper.sendTypingStatusToServer(opponentUser.getUserId(), isTypingNow);
    }

    private void setSmilePanelIcon(int resourceId) {
        smilePanelImageButton.setImageResource(resourceId);
    }

    private boolean isSmilesLayoutShowing() {
        return emojiconsFragment.getVisibility() == View.VISIBLE;
    }

    protected void checkForScrolling(int oldMessagesCount) {
        if (oldMessagesCount != messagesAdapter.getAllItems().size()) {
            scrollMessagesToBottom();
        }
    }

    protected void scrollMessagesToBottom() {
        if (!loadMore) {
            scrollMessagesWithDelay();
        }
    }

    private void scrollMessagesWithDelay() {
        mainThreadHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                messagesRecyclerView.scrollToPosition(messagesAdapter.getItemCount() - 1);
            }
        }, DELAY_SCROLLING_LIST);
    }

    protected void sendMessage(boolean privateMessage) {
        boolean error = false;
        try {
            if (privateMessage) {
                ((QBPrivateChatHelper) baseChatHelper).sendPrivateMessage(
                        messageEditText.getText().toString(), opponentUser.getUserId());
            } else {
                ((QBGroupChatHelper) baseChatHelper).sendGroupMessage(dialog.getRoomJid(),
                        messageEditText.getText().toString());
            }
        } catch (QBResponseException e) {
            ErrorUtils.showError(this, e);
            error = true;
        } catch (IllegalStateException e) {
            ErrorUtils.showError(this, this.getString(
                    com.quickblox.q_municate_core.R.string.dlg_not_joined_room));
            error = true;
        } catch (Exception e) {
            ErrorUtils.showError(this, e);
            error = true;
        }

        if (!error) {
            messageEditText.setText(ConstsCore.EMPTY_STRING);
            scrollMessagesToBottom();
        }
    }

    protected void startLoadDialogMessages() {
        if (dialog == null) {
            return;
        }

        showActionBarProgress();

        List<DialogOccupant> dialogOccupantsList = dataManager.getDialogOccupantDataManager().getDialogOccupantsListByDialogId(dialog.getDialogId());
        List<Long> dialogOccupantsIdsList = ChatUtils.getIdsFromDialogOccupantsList(dialogOccupantsList);

        Message message;
        DialogNotification dialogNotification;

        long messageDateSent = 0;

        if (loadMore) {
            message = dataManager.getMessageDataManager().getMessageByDialogId(true, dialogOccupantsIdsList);
            dialogNotification = dataManager.getDialogNotificationDataManager().getDialogNotificationByDialogId(true, dialogOccupantsIdsList);
            messageDateSent = ChatUtils.getDialogMessageCreatedDate(false, message, dialogNotification);
        } else {
            message = dataManager.getMessageDataManager().getMessageByDialogId(false, dialogOccupantsIdsList);
            dialogNotification = dataManager.getDialogNotificationDataManager().getDialogNotificationByDialogId(
                    false, dialogOccupantsIdsList);
            messageDateSent = ChatUtils.getDialogMessageCreatedDate(true, message, dialogNotification);
        }

        startLoadDialogMessages(dialog, messageDateSent);
    }

    private void readAllMessages() {
        if (dialog != null) {
            List<Message> messagesList = dataManager.getMessageDataManager()
                    .getMessagesByDialogId(dialog.getDialogId());
            dataManager.getMessageDataManager().createOrUpdateAll(ChatUtils.readAllMessages(messagesList, AppSession.getSession().getUser()));

            List<DialogNotification> dialogNotificationsList = dataManager.getDialogNotificationDataManager()
                    .getDialogNotificationsByDialogId(dialog.getDialogId());
            dataManager.getDialogNotificationDataManager().createOrUpdateAll(ChatUtils
                    .readAllDialogNotification(dialogNotificationsList, AppSession.getSession().getUser()));
        }
    }

    private void createChatLocally() {
        if (isNetworkAvailable()) {
            if (service != null) {
                baseChatHelper = (QBBaseChatHelper) service.getHelper(chatHelperIdentifier);
                Log.d("Fix double message", "baseChatHelper = " + baseChatHelper + "\n dialog = " + dialog);
                if (baseChatHelper != null && dialog != null) {
                    try {
                        baseChatHelper.createChatLocally(ChatUtils.createQBDialogFromLocalDialog(dataManager, dialog),
                                generateBundleToInitDialog());
                    } catch (QBResponseException e) {
                        ErrorUtils.showError(this, e.getMessage());
                        finish();
                    }
                }
            }
        }
    }

    private void closeChatLocally() {
        if (baseChatHelper != null && dialog != null) {
            baseChatHelper.closeChat(ChatUtils.createQBDialogFromLocalDialog(dataManager, dialog),
                    generateBundleToInitDialog());
        }
        dialog = null;
    }

    protected List<CombinationMessage> createCombinationMessagesList() {
        if (dialog == null) {
            Log.d("PrivateDialogActivity", "dialog = " + dialog);
            return null;
        }

        List<Message> messagesList = dataManager.getMessageDataManager().getMessagesByDialogId(dialog.getDialogId());
        List<DialogNotification> dialogNotificationsList = dataManager.getDialogNotificationDataManager()
                .getDialogNotificationsByDialogId(dialog.getDialogId());
        return ChatUtils.createCombinationMessagesList(messagesList, dialogNotificationsList);
    }

    private void visibleOrHideSmilePanel() {
        if (isSmilesLayoutShowing()) {
            hideSmileLayout();
            KeyboardUtils.showKeyboard(BaseDialogActivity.this);
        } else {
            KeyboardUtils.hideKeyboard(BaseDialogActivity.this);
            showSmileLayout();
        }
    }

    protected void checkMessageSendingPossibility(boolean enable) {
        messageEditText.setEnabled(enable);
        smilePanelImageButton.setEnabled(enable);
        attachButton.setEnabled(enable);
    }

    protected abstract void updateActionBar();

    protected abstract void onConnectServiceLocally(QBService service);

    protected abstract Bundle generateBundleToInitDialog();

    protected abstract void updateMessagesList();

    protected abstract void onFileLoaded(QBFile file);

    protected abstract void checkMessageSendingPossibility();

    public static class CombinationMessageLoader extends BaseLoader<List<CombinationMessage>> {

        private String dialogId;

        public CombinationMessageLoader(Context context, DataManager dataManager, String dialogId) {
            super(context, dataManager);
            this.dialogId = dialogId;
        }

        @Override
        protected List<CombinationMessage> getItems() {
            return createCombinationMessagesList();
        }

        private List<CombinationMessage> createCombinationMessagesList() {
            List<Message> messagesList = dataManager.getMessageDataManager().getMessagesByDialogId(dialogId);
            List<DialogNotification> dialogNotificationsList = dataManager.getDialogNotificationDataManager()
                    .getDialogNotificationsByDialogId(dialogId);
            return ChatUtils.createCombinationMessagesList(messagesList, dialogNotificationsList);
        }
    }

    private class MessageObserver implements Observer {

        @Override
        public void update(Observable observable, Object data) {
            Log.d("Fix double message", "MessageObserver update(Observable observable, Object data) from " + BaseDialogActivity.class.getSimpleName());
            if (data != null && data.equals(MessageDataManager.OBSERVE_KEY)) {
                updateMessagesList();
            }
        }
    }

    private class DialogNotificationObserver implements Observer {

        @Override
        public void update(Observable observable, Object data) {
            Log.d("Fix double message", "DialogNotificationObserver update(Observable observable, Object data) from " + BaseDialogActivity.class.getSimpleName());
            if (data != null && data.equals(DialogNotificationDataManager.OBSERVE_KEY)) {
                updateMessagesList();
            }
        }
    }

    private class DialogObserver implements Observer {

        @Override
        public void update(Observable observable, Object data) {
            Log.d("Fix double message", "DialogObserver update(Observable observable, Object data) from " + BaseDialogActivity.class.getSimpleName());
            if (data != null && data.equals(DialogDataManager.OBSERVE_KEY) && dialog != null) {
                dialog = dataManager.getDialogDataManager().getByDialogId(dialog.getDialogId());
                updateActionBar();
            }
        }
    }

    private class TypingTimerTask extends TimerTask {

        @Override
        public void run() {
            isTypingNow = false;
            sendTypingStatus();
        }
    }

    public class LoadAttachFileSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            QBFile file = (QBFile) bundle.getSerializable(QBServiceConsts.EXTRA_ATTACH_FILE);
            onFileLoaded(file);
            hideProgress();
        }
    }

    public class LoadDialogMessagesSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            messageSwipeRefreshLayout.setRefreshing(false);
            int totalEntries = bundle.getInt(QBServiceConsts.EXTRA_TOTAL_ENTRIES, ConstsCore.ZERO_INT_VALUE);


            if (messagesAdapter != null && !messagesAdapter.isEmpty() && totalEntries != ConstsCore.ZERO_INT_VALUE) {
                scrollMessagesToBottom();
            }

            loadMore = false;

            hideActionBarProgress();
        }
    }

    public class LoadDialogMessagesFailAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            messageSwipeRefreshLayout.setRefreshing(false);

            loadMore = false;

            hideActionBarProgress();
        }
    }

    private class TypingStatusBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            int userId = extras.getInt(QBServiceConsts.EXTRA_USER_ID);
            // TODO: now it is possible only for Private chats
            if (dialog != null && opponentUser != null && userId == opponentUser.getUserId()) {
                if (Dialog.Type.PRIVATE.equals(dialog.getType())) {
                    boolean isTyping = extras.getBoolean(QBServiceConsts.EXTRA_IS_TYPING);
                    if (isTyping) {
                        showTypingStatus();
                    } else {
                        hideTypingStatus();
                    }
                }
            }
        }
    }

    private class UpdatingDialogBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(QBServiceConsts.UPDATE_DIALOG)) {
                updateData();
            }
        }
    }

    private class RefreshLayoutListener implements SwipeRefreshLayout.OnRefreshListener {

        @Override
        public void onRefresh() {
            if (!isNetworkAvailable()) {
                messageSwipeRefreshLayout.setRefreshing(false);
                return;
            }

            if (!loadMore) {
                loadMore = true;
                startLoadDialogMessages();
            }
        }
    }
}