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
import com.quickblox.chat.model.QBAttachment;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.activities.base.BaseLoggableActivity;
import com.quickblox.q_municate.ui.activities.location.MapsActivity;
import com.quickblox.q_municate.ui.activities.others.PreviewImageActivity;
import com.quickblox.q_municate.ui.views.recyclerview.WrapContentLinearLayoutManager;
import com.quickblox.q_municate.utils.StringUtils;
import com.quickblox.q_municate_core.core.concurrency.BaseAsyncTask;
import com.quickblox.q_municate_core.core.loader.BaseLoader;
import com.quickblox.q_municate.ui.adapters.chats.BaseChatMessagesAdapter;
import com.quickblox.q_municate.ui.fragments.dialogs.base.TwoButtonsDialogFragment;
import com.quickblox.q_municate.utils.KeyboardUtils;
import com.quickblox.q_municate.utils.helpers.ImagePickHelper;
import com.quickblox.q_municate.utils.helpers.SystemPermissionHelper;
import com.quickblox.q_municate.utils.image.ImageLoaderUtils;
import com.quickblox.q_municate.utils.image.ImageUtils;
import com.quickblox.q_municate.utils.listeners.ChatUIHelperListener;
import com.quickblox.q_municate.utils.listeners.OnImagePickedListener;
import com.quickblox.q_municate_core.core.command.Command;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.models.CombinationMessage;
import com.quickblox.q_municate_core.qb.commands.QBLoadAttachFileCommand;
import com.quickblox.q_municate_core.qb.commands.chat.QBLoadDialogMessagesCommand;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.ChatUtils;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.models.Attachment;
import com.quickblox.q_municate_db.models.DialogNotification;
import com.quickblox.q_municate_db.models.DialogOccupant;
import com.quickblox.q_municate_db.models.Message;
import com.quickblox.q_municate_db.utils.ErrorUtils;
import com.quickblox.ui.kit.chatmessage.adapter.listeners.QBChatAttachImageClickListener;
import com.quickblox.ui.kit.chatmessage.adapter.listeners.QBChatAttachLocationClickListener;
import com.quickblox.ui.kit.chatmessage.adapter.listeners.QBChatMessageLinkClickListener;
import com.quickblox.ui.kit.chatmessage.adapter.utils.QBMessageTextClickMovement;
import com.quickblox.q_municate_user_service.model.QMUser;
import com.rockerhieu.emojicon.EmojiconGridFragment;
import com.rockerhieu.emojicon.EmojiconsFragment;
import com.rockerhieu.emojicon.emoji.Emojicon;

import java.io.File;
import java.util.ArrayList;
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

    private static final String TAG = BaseDialogActivity.class.getSimpleName();
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

    protected QBChatDialog currentChatDialog;
    protected Resources resources;
    protected DataManager dataManager;
    protected ImageUtils imageUtils;
    protected BaseChatMessagesAdapter messagesAdapter;
    protected List<CombinationMessage> combinationMessagesList;
    protected ImagePickHelper imagePickHelper;

    private MessagesTextViewLinkClickListener messagesTextViewLinkClickListener;
    private LocationAttachClickListener locationAttachClickListener;
    private ImageAttachClickListener imageAttachClickListener;
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
    private BroadcastReceiver updatingDialogBroadcastReceiver;
    private SystemPermissionHelper systemPermissionHelper;
    private boolean isLoadingMessages;

    @Override
    protected int getContentResId() {
        return R.layout.activity_dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initFields();

        if (currentChatDialog == null) {
            finish();
        }

        setUpActionBarWithUpButton();

        initCustomUI();
        initCustomListeners();

        addActions();
        addObservers();
        registerBroadcastReceivers();

        if (isNetworkAvailable()) {
            deleteTempMessages();
        }

        hideSmileLayout();

        initMessagesRecyclerView();
    }

    @OnTextChanged(R.id.message_edittext)
    void messageEditTextChanged(CharSequence charSequence) {
        setActionButtonVisibility(charSequence);

        // TODO: now it is possible only for Private chats
        if (currentChatDialog != null && QBDialogType.PRIVATE.equals(currentChatDialog.getType())) {
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
        imagePickHelper.pickAnImage(this, ImageUtils.IMAGE_LOCATION_REQUEST_CODE);
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
        addChatMessagesAdapterListeners();
        checkPermissionSaveFiles();
    }

    @Override
    protected void onResume() {
        super.onResume();
        restoreDefaultCanPerformLogout();
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
        removeChatMessagesAdapterListeners();
        readAllMessages();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeCurrentChat();
        removeActions();
        deleteObservers();
        unregisterBroadcastReceivers();
    }

    @Override
    public void onConnectedToService(QBService service) {
        super.onConnectedToService(service);
        onConnectServiceLocally(service);
        initCurrentDialog();
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
    public void onImagePicked(int requestCode, Attachment.Type type, Object attachment) {
        canPerformLogout.set(true);
        startLoadAttachFile(type, attachment, currentChatDialog.getDialogId());
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

    private void restoreDefaultCanPerformLogout() {
        if (!canPerformLogout.get()) {
            canPerformLogout.set(true);
        }
    }

    private void addChatMessagesAdapterListeners() {
        messagesAdapter.setMessageTextViewLinkClickListener(messagesTextViewLinkClickListener, false);
        messagesAdapter.setAttachLocationClickListener(locationAttachClickListener);
        messagesAdapter.setAttachImageClickListener(imageAttachClickListener);
    }

    private void removeChatMessagesAdapterListeners() {
        if (messagesAdapter != null) {
            messagesAdapter.removeMessageTextViewLinkClickListener();
            messagesAdapter.removeLocationImageClickListener(locationAttachClickListener);
            messagesAdapter.removeAttachImageClickListener(imageAttachClickListener);
        }
    }

    @Override
    protected void performLoginChatSuccessAction(Bundle bundle) {
        super.performLoginChatSuccessAction(bundle);

        startLoadDialogMessages(false);
    }

    protected void initFields() {
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
        updatingDialogBroadcastReceiver = new UpdatingDialogBroadcastReceiver();
        appSharedHelper.saveNeedToOpenDialog(false);
        imagePickHelper = new ImagePickHelper();
        systemPermissionHelper = new SystemPermissionHelper(this);
        messagesTextViewLinkClickListener = new MessagesTextViewLinkClickListener();
        locationAttachClickListener = new LocationAttachClickListener();
        imageAttachClickListener = new ImageAttachClickListener();
        currentChatDialog = (QBChatDialog) getIntent().getExtras().getSerializable(QBServiceConsts.EXTRA_DIALOG);
        combinationMessagesList = createCombinationMessagesList();
    }

    private void initCustomUI() {
        emojiconsFragment = _findViewById(R.id.emojicon_fragment);
    }

    private void initCustomListeners() {
        messageSwipeRefreshLayout.setOnRefreshListener(new RefreshLayoutListener());
    }

    protected void initMessagesRecyclerView() {
        messagesRecyclerView.setLayoutManager(new WrapContentLinearLayoutManager(this));
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

    protected void registerBroadcastReceivers() {
        localBroadcastManager.registerReceiver(updatingDialogBroadcastReceiver,
                new IntentFilter(QBServiceConsts.UPDATE_DIALOG));
    }

    protected void unregisterBroadcastReceivers() {
        localBroadcastManager.unregisterReceiver(updatingDialogBroadcastReceiver);
    }

    protected void addObservers() {
        dataManager.getQBChatDialogDataManager().addObserver(dialogObserver);
        dataManager.getMessageDataManager().addObserver(messageObserver);
        dataManager.getDialogNotificationDataManager().addObserver(dialogNotificationObserver);
    }

    protected void deleteObservers() {
        dataManager.getQBChatDialogDataManager().deleteObserver(dialogObserver);
        dataManager.getMessageDataManager().deleteObserver(messageObserver);
        dataManager.getDialogNotificationDataManager().deleteObserver(dialogNotificationObserver);
    }

    protected void updateData() {
        updateActionBar();
        updateMessagesList();
    }

    protected void onConnectServiceLocally() {
    }

    protected void deleteTempMessages() {
        List<DialogOccupant> dialogOccupantsList = dataManager.getDialogOccupantDataManager()
                .getDialogOccupantsListByDialogId(currentChatDialog.getDialogId());
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

    private void checkPermissionSaveFiles() {
        boolean permissionSaveFileWasRequested = appSharedHelper.isPermissionsSaveFileWasRequested();
        if (!systemPermissionHelper.isAllPermissionsGrantedForSaveFile() && !permissionSaveFileWasRequested) {
            systemPermissionHelper.requestPermissionsForSaveFile();
            appSharedHelper.savePermissionsSaveFileWasRequested(true);
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

    protected void startLoadAttachFile(final Attachment.Type type, final Object attachment, final String dialogId) {
        TwoButtonsDialogFragment.show(getSupportFragmentManager(), getString(R.string.dialog_confirm_sending_attach,
                StringUtils.getAttachmentNameByType(this, type)), false,
                new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        switch (type){
                            case LOCATION:
                                sendMessageWithAttachment(dialogId, Attachment.Type.LOCATION, attachment);
                                break;
                            case PICTURE:
                                showProgress();
                                QBLoadAttachFileCommand.start(BaseDialogActivity.this, (File) attachment, dialogId);
                                break;
                        }
                    }
                });
    }

    protected void startLoadDialogMessages(QBChatDialog chatDialog, long lastDateLoad, boolean isLoadOldMessages) {
        QBLoadDialogMessagesCommand.start(this, chatDialog, lastDateLoad, isLoadOldMessages);
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
        if (currentChatDialog != null && QBDialogType.PRIVATE.equals(currentChatDialog.getType())) {
            if (isTypingNow) {
                isTypingNow = false;
                sendTypingStatus();
            }
        }
    }

    private void sendTypingStatus() {
        chatHelper.sendTypingStatusToServer(isTypingNow);
    }

    private void setSmilePanelIcon(int resourceId) {
        smilePanelImageButton.setImageResource(resourceId);
    }

    private boolean isSmilesLayoutShowing() {
        return emojiconsFragment.getVisibility() == View.VISIBLE;
    }

    protected void checkForScrolling(int oldMessagesCount) {
        if (oldMessagesCount != messagesAdapter.getItemCount()) {
            scrollMessagesToBottom();
        }
    }

    protected void scrollMessagesToBottom() {
        scrollMessagesWithDelay();
    }

    private void scrollMessagesWithDelay() {
        mainThreadHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                messagesRecyclerView.scrollToPosition(messagesAdapter.getItemCount() - 1);
            }
        }, DELAY_SCROLLING_LIST);
    }

    protected void sendMessage() {
        boolean error = false;
        try {
            chatHelper.sendChatMessage(messageEditText.getText().toString());
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
        }
    }

    protected void startLoadDialogMessages(boolean isLoadOldMessages) {
        if (currentChatDialog == null) {
            return;
        }

        showActionBarProgress();

        List<DialogOccupant> dialogOccupantsList = dataManager.getDialogOccupantDataManager().getDialogOccupantsListByDialogId(currentChatDialog.getDialogId());
        List<Long> dialogOccupantsIdsList = ChatUtils.getIdsFromDialogOccupantsList(dialogOccupantsList);

        Message message;
        DialogNotification dialogNotification;

        message = dataManager.getMessageDataManager().getMessageByDialogId(isLoadOldMessages, dialogOccupantsIdsList);
        dialogNotification = dataManager.getDialogNotificationDataManager().getDialogNotificationByDialogId(isLoadOldMessages, dialogOccupantsIdsList);
        long messageDateSent = ChatUtils.getDialogMessageCreatedDate(!isLoadOldMessages, message, dialogNotification);

        startLoadDialogMessages(currentChatDialog, messageDateSent, isLoadOldMessages);
    }

    private void readAllMessages() {
        if (currentChatDialog != null) {
            List<Message> messagesList = dataManager.getMessageDataManager()
                    .getMessagesByDialogId(currentChatDialog.getDialogId());
            dataManager.getMessageDataManager().createOrUpdateAll(ChatUtils.readAllMessages(messagesList, AppSession.getSession().getUser()));

            List<DialogNotification> dialogNotificationsList = dataManager.getDialogNotificationDataManager()
                    .getDialogNotificationsByDialogId(currentChatDialog.getDialogId());
            dataManager.getDialogNotificationDataManager().createOrUpdateAll(ChatUtils
                    .readAllDialogNotification(dialogNotificationsList, AppSession.getSession().getUser()));
        }
    }

    protected void initCurrentDialog() {
        if (service != null) {
            Log.v(TAG, "chatHelper = " + chatHelper + "\n dialog = " + currentChatDialog);
            if (chatHelper != null && currentChatDialog != null) {
                try {
                    chatHelper.initCurrentChatDialog(currentChatDialog, generateBundleToInitDialog());
                } catch (QBResponseException e) {
                    ErrorUtils.showError(this, e.getMessage());
                    finish();
                }
            }
        } else {
            Log.v(TAG, "service == null");
        }
    }

    private void closeCurrentChat() {
        if (chatHelper != null && currentChatDialog != null) {
            chatHelper.closeChat(currentChatDialog, generateBundleToInitDialog());
        }
        currentChatDialog = null;
    }

    protected List<CombinationMessage> createCombinationMessagesList() {
        if (currentChatDialog == null) {
            Log.d("BaseDialogActivity", "dialog = " + currentChatDialog);
            return new ArrayList<>();
        }

        List<Message> messagesList = dataManager.getMessageDataManager().getMessagesByDialogId(currentChatDialog.getDialogId());
        List<DialogNotification> dialogNotificationsList = dataManager.getDialogNotificationDataManager()
                .getDialogNotificationsByDialogId(currentChatDialog.getDialogId());

        List<CombinationMessage> combinationMessages = ChatUtils.createCombinationMessagesList(messagesList, dialogNotificationsList);
        Log.d(TAG, "combinationMessages= " + combinationMessages);
        return combinationMessages;
    }

    protected List<CombinationMessage> buildCombinationMessagesListByDate(long createDate, boolean moreDate) {
        if (currentChatDialog == null) {
            return new ArrayList<>();
        }

        List<Message> messagesList = dataManager.getMessageDataManager()
                .getMessagesByDialogIdAndDate(currentChatDialog.getDialogId(), createDate, moreDate);
        List<DialogNotification> dialogNotificationsList = dataManager.getDialogNotificationDataManager()
                .getDialogNotificationsByDialogIdAndDate(currentChatDialog.getDialogId(), createDate, moreDate);
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

    protected void sendMessageWithAttachment(String dialogId, Attachment.Type attachmentType, Object attachmentObject){
        if (!dialogId.equals(currentChatDialog.getDialogId())) {
            return;
        }
        try {
            chatHelper.sendMessageWithAttachment(attachmentType, attachmentObject);
        } catch (QBResponseException exc) {
            ErrorUtils.showError(this, exc);
        }
    }

    protected abstract void checkMessageSendingPossibility();

    protected abstract void additionalActionsAfterLoadMessages();

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
            Log.i(TAG, "==== MessageObserver  'update' ====");
            if (data != null && data.equals(dataManager.getMessageDataManager().getObserverKey())) {
                updateMessagesList();
            }
        }
    }

    private class DialogNotificationObserver implements Observer {

        @Override
        public void update(Observable observable, Object data) {
            Log.i(TAG, "==== DialogNotificationObserver  'update' ====");
            if (data != null && data.equals(dataManager.getDialogNotificationDataManager().getObserverKey())) {
                updateMessagesList();
            }
        }
    }

    private class DialogObserver implements Observer {

        @Override
        public void update(Observable observable, Object data) {
            Log.i(TAG, "==== DialogObserver  'update' ====");
            if (data != null && data.equals(dataManager.getQBChatDialogDataManager().getObserverKey()) && currentChatDialog != null) {
                currentChatDialog = dataManager.getQBChatDialogDataManager().getByDialogId(currentChatDialog.getDialogId());
                if (currentChatDialog != null) {
                    // need init current dialog after getting from DB
                    initCurrentDialog();
                }

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
            String dialogId = (String) bundle.getSerializable(QBServiceConsts.EXTRA_DIALOG_ID);
            sendMessageWithAttachment(dialogId, StringUtils.getAttachmentTypeByFileName(file.getName()), file);
            hideProgress();
        }
    }

    public class LoadDialogMessagesSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            final int totalEntries = bundle.getInt(QBServiceConsts.EXTRA_TOTAL_ENTRIES,
                    ConstsCore.ZERO_INT_VALUE);
            final long lastMessageDate = bundle.getLong(QBServiceConsts.EXTRA_LAST_DATE_LOAD_MESSAGES,
                    ConstsCore.ZERO_INT_VALUE);
            final boolean isLoadedOldMessages = bundle.getBoolean(QBServiceConsts.EXTRA_IS_LOAD_OLD_MESSAGES);

            Log.d("BaseDialogActivity", "Laoding messages finished" + " totalEntries = " + totalEntries
                    + " lastMessageDate = " + lastMessageDate
                    + " isLoadedOldMessages = " + isLoadedOldMessages);

            if (messagesAdapter != null && totalEntries != ConstsCore.ZERO_INT_VALUE) {

                (new BaseAsyncTask<Void, Void, Boolean>() {
                    @Override
                    public Boolean performInBackground(Void... params) throws Exception {
                        combinationMessagesList = createCombinationMessagesList();
                        additionalActionsAfterLoadMessages();
                        return true;
                    }

                    @Override
                    public void onResult(Boolean aBoolean) {
                        if (isLoadedOldMessages) {
                            messagesAdapter.setList(combinationMessagesList, false);
                            messagesAdapter.notifyItemRangeInserted(0, totalEntries);
                        } else {
                            messagesAdapter.setList(combinationMessagesList, true);
                            scrollMessagesToBottom();
                        }

                        messageSwipeRefreshLayout.setRefreshing(false);
                        hideActionBarProgress();
                        isLoadingMessages = false;
                    }

                    @Override
                    public void onException(Exception e) {
                        ErrorUtils.showError(BaseDialogActivity.this, e);
                        isLoadingMessages = false;
                    }

                }).execute();

            } else {
                messageSwipeRefreshLayout.setRefreshing(false);
                hideActionBarProgress();
                isLoadingMessages = false;
            }
        }
    }

    public class LoadDialogMessagesFailAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            messageSwipeRefreshLayout.setRefreshing(false);

            isLoadingMessages = false;

            hideActionBarProgress();
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

            if (!isLoadingMessages) {
                isLoadingMessages = true;
                startLoadDialogMessages(true);
            }
        }
    }

    protected class MessagesTextViewLinkClickListener implements QBChatMessageLinkClickListener {

        @Override
        public void onLinkClicked(String linkText, QBMessageTextClickMovement.QBLinkType qbLinkType, int position) {

            if (!QBMessageTextClickMovement.QBLinkType.NONE.equals(qbLinkType)) {
                canPerformLogout.set(false);
            }
        }

        @Override
        public void onLongClick(String text, int positionInAdapter) {

        }

    }

    protected class LocationAttachClickListener implements QBChatAttachLocationClickListener{

        @Override
        public void onLinkClicked(QBAttachment qbAttachment, int i) {
            MapsActivity.startMapForResult(BaseDialogActivity.this, qbAttachment.getData());
        }
    }

    protected class ImageAttachClickListener implements QBChatAttachImageClickListener{

        @Override
        public void onLinkClicked(QBAttachment qbAttachment, int i) {
            PreviewImageActivity.start(BaseDialogActivity.this, qbAttachment.getUrl());
        }
    }
}