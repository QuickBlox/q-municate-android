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
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
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
import com.quickblox.q_municate.utils.ValidationUtils;
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
import com.quickblox.q_municate_db.managers.DialogDataManager;
import com.quickblox.q_municate_db.managers.DialogNotificationDataManager;
import com.quickblox.q_municate_db.managers.MessageDataManager;
import com.quickblox.q_municate_db.models.Attachment;
import com.quickblox.q_municate_db.models.DialogNotification;
import com.quickblox.q_municate_db.models.DialogOccupant;
import com.quickblox.q_municate_db.models.Message;
import com.quickblox.q_municate_db.utils.ErrorUtils;
import com.quickblox.ui.kit.chatmessage.adapter.listeners.QBChatAttachImageClickListener;
import com.quickblox.ui.kit.chatmessage.adapter.listeners.QBChatAttachLocationClickListener;
import com.quickblox.ui.kit.chatmessage.adapter.listeners.QBChatMessageLinkClickListener;
import com.quickblox.ui.kit.chatmessage.adapter.utils.QBMessageTextClickMovement;
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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
    private static final int MESSAGES_PAGE_SIZE = ConstsCore.DIALOG_MESSAGES_PER_PAGE;
    private static final int KEEP_ALIVE_TIME = 1;
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
    private static int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();

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

    private BlockingQueue<Runnable> threadQueue;
    private ThreadPoolExecutor threadPool;

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
        initThreads();

        addActions();
        addObservers();
        registerBroadcastReceivers();

        initMessagesRecyclerView();

        hideSmileLayout();

        if (isNetworkAvailable()) {
            deleteTempMessagesAsync();
        }

    }

    @OnTextChanged(R.id.message_edittext)
    void messageEditTextChanged(CharSequence charSequence) {
        setActionButtonVisibility(charSequence);

        if (currentChatDialog != null) {
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
            returnResult();
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

        loadNextPartMessagesAsync();

        checkMessageSendingPossibility();
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelTasks();
        closeCurrentChat();
        removeActions();
        deleteObservers();
        unregisterBroadcastReceivers();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void returnResult(){
        if (getCallingActivity() != null) {
            Intent intent = new Intent();
            intent.putExtra(QBServiceConsts.EXTRA_DIALOG_ID, currentChatDialog.getDialogId());
            setResult(RESULT_OK, intent);
        }
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
        if(ValidationUtils.validateAttachment(getSupportFragmentManager(), getResources().getStringArray(R.array.supported_attachment_types), type, attachment)){
            startLoadAttachFile(type, attachment, currentChatDialog.getDialogId());
        }
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
    protected void checkShowingConnectionError() {
        if (!isNetworkAvailable()) {
            setActionBarTitle(getString(R.string.dlg_internet_connection_is_missing));
            setActionBarIcon(null);
        } else {
            setActionBarTitle(title);
            updateActionBar();
        }
    }

    private void deleteTempMessagesAsync(){
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                deleteTempMessages();
            }
        });
    }

    private void loadNextPartMessagesAsync(){
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                loadNextPartMessagesFromDb(false, true);
            }
        });
    }

    private void cancelTasks(){
        threadPool.shutdownNow();
    }

    private void initThreads() {
        threadQueue = new LinkedBlockingQueue<>();
        threadPool = new ThreadPoolExecutor(NUMBER_OF_CORES, NUMBER_OF_CORES, KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, threadQueue);
        threadPool.allowCoreThreadTimeOut(true);
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

        checkMessageSendingPossibility();
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
        combinationMessagesList = new ArrayList<>();
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
                                sendMessageWithAttachment(dialogId, Attachment.Type.LOCATION, attachment, null);
                                break;
                            case IMAGE:
                                showProgress();
                                QBLoadAttachFileCommand.start(BaseDialogActivity.this, (File) attachment, dialogId);
                                break;
                        }
                    }
                });
    }

    protected void startLoadDialogMessages(QBChatDialog chatDialog, long lastDateLoad, boolean isLoadOldMessages) {
        isLoadingMessages = true;
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
        if (currentChatDialog != null) {
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

    protected void startLoadDialogMessages(final boolean isLoadOldMessages) {
        if (currentChatDialog == null || isLoadingMessages) {
            return;
        }
        showActionBarProgress();
        long messageDateSent = getMessageDateForLoadByCurrentList(isLoadOldMessages);
        startLoadDialogMessages(currentChatDialog, messageDateSent, isLoadOldMessages);

    }

    protected long getMessageDateForLoad(boolean isLoadOldMessages){
        long messageDateSent;
        List<DialogOccupant> dialogOccupantsList = dataManager.getDialogOccupantDataManager().getDialogOccupantsListByDialogId(currentChatDialog.getDialogId());
        List<Long> dialogOccupantsIdsList = ChatUtils.getIdsFromDialogOccupantsList(dialogOccupantsList);

        Message message;
        DialogNotification dialogNotification;

        message = dataManager.getMessageDataManager().getMessageByDialogId(isLoadOldMessages, dialogOccupantsIdsList);
        dialogNotification = dataManager.getDialogNotificationDataManager().getDialogNotificationByDialogId(isLoadOldMessages, dialogOccupantsIdsList);
        messageDateSent =  ChatUtils.getDialogMessageCreatedDate(!isLoadOldMessages, message, dialogNotification);

        return messageDateSent;
    }

    protected long getMessageDateForLoadByCurrentList(boolean isLoadOld){
        if (combinationMessagesList.size() == 0){
            return 0;
        }

        return !isLoadOld
                ? combinationMessagesList.get(combinationMessagesList.size() - 1).getCreatedDate()
                : combinationMessagesList.get(0).getCreatedDate();
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

    protected List<CombinationMessage> buildLimitedCombinationMessagesListByDate(long createDate, boolean moreDate, long limit){
        if (currentChatDialog == null || currentChatDialog.getDialogId() == null) {
            return new ArrayList<>();
        }

        Log.d(TAG, "selection parameters: createDate = " + createDate + " moreDate = " + moreDate + " limit = " + limit);

        String currentDialogId = currentChatDialog.getDialogId();
        List<Message> messagesList = dataManager.getMessageDataManager()
                .getMessagesByDialogIdAndDate(currentDialogId, createDate, moreDate, limit);
        Log.d(TAG, " messagesList.size() = " + messagesList.size());

        List<DialogNotification> dialogNotificationsList = dataManager.getDialogNotificationDataManager()
                .getDialogNotificationsByDialogIdAndDate(currentDialogId, createDate, moreDate, limit);
        Log.d(TAG, " dialogNotificationsList.size() = " + dialogNotificationsList.size());
        List<CombinationMessage> combinationMessages = ChatUtils.createLimitedCombinationMessagesList(messagesList, dialogNotificationsList, (int) limit);
        return combinationMessages;
    }

    protected void loadNextPartMessagesFromDb(final boolean isLoadOld, final boolean needUpdateAdapter) {
        long messageDate = getMessageDateForLoadByCurrentList(isLoadOld);

        final List<CombinationMessage> requestedMessages = buildLimitedCombinationMessagesListByDate(
                messageDate, !isLoadOld, ConstsCore.DIALOG_MESSAGES_PER_PAGE);

        if (isLoadOld) {
            combinationMessagesList.addAll(0, requestedMessages);
        } else {
            combinationMessagesList.addAll(requestedMessages);
        }

        if (needUpdateAdapter) {
            mainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    updateMessagesAdapter(isLoadOld, requestedMessages.size());
                    startLoadDialogMessages(false);
                }
            });
        }
    }

    private void updateMessagesAdapter(boolean isLoadOld, int partSize) {
        messagesAdapter.setList(combinationMessagesList, !isLoadOld);

        if (isLoadOld) {
            messagesAdapter.notifyItemRangeInserted(0, partSize);
        } else {
            scrollMessagesToBottom();
        }
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

    private void replaceMessageInCurrentList(CombinationMessage combinationMessage){
        if (combinationMessagesList.contains(combinationMessage)){
            int positionOldMessage = combinationMessagesList.indexOf(combinationMessage);
            combinationMessagesList.set(positionOldMessage, combinationMessage);
        }
    }

    private void updateMessageItemInAdapter(CombinationMessage combinationMessage){
        replaceMessageInCurrentList(combinationMessage);
        if (combinationMessagesList.contains(combinationMessage)) {
            messagesAdapter.notifyItemChanged(combinationMessagesList.indexOf(combinationMessage));
        } else {
            addMessageItemToAdapter(combinationMessage);
        }
    }

    private void addMessageItemToAdapter(CombinationMessage combinationMessage){
        combinationMessagesList.add(combinationMessage);
        messagesAdapter.setList(combinationMessagesList, true);
        scrollMessagesToBottom();
    }

    private void afterLoadingMessagesActions(){
        messageSwipeRefreshLayout.setRefreshing(false);
        hideActionBarProgress();
        isLoadingMessages = false;
    }

    protected abstract void updateActionBar();

    protected abstract void onConnectServiceLocally(QBService service);

    protected abstract Bundle generateBundleToInitDialog();

    protected abstract void updateMessagesList();

    protected void sendMessageWithAttachment(String dialogId, Attachment.Type attachmentType, Object attachmentObject, String localPath){
        if (!dialogId.equals(currentChatDialog.getDialogId())) {
            return;
        }
        try {
            chatHelper.sendMessageWithAttachment(attachmentType, attachmentObject, localPath);
        } catch (QBResponseException exc) {
            ErrorUtils.showError(this, exc);
        }
    }

    protected abstract void checkMessageSendingPossibility();

    private class MessageObserver implements Observer {

        @Override
        public void update(Observable observable, Object data) {
            Log.i(TAG, "==== MessageObserver  'update' ====");
            if (data != null) {
                Bundle observableData = (Bundle) data;
                int action = observableData.getInt(MessageDataManager.EXTRA_ACTION);
                Message message = (Message) observableData.getSerializable(MessageDataManager.EXTRA_OBJECT);
                if (message != null) {
                    CombinationMessage combinationMessage = new CombinationMessage(message);
                    if (action == MessageDataManager.UPDATE_ACTION) {
                        Log.d(TAG, "updated message = " + message);
                        updateMessageItemInAdapter(combinationMessage);
                    } else if (action == MessageDataManager.CREATE_OR_UPDATE_ACTION) {
                        Log.d(TAG, "created message = " + message);
                        addMessageItemToAdapter(combinationMessage);
                    }
                }

                if (action == MessageDataManager.DELETE_ACTION) {
                    combinationMessagesList.clear();
                    loadNextPartMessagesFromDb(false, true);
                }
            }
        }
    }

    private class DialogNotificationObserver implements Observer {

        @Override
        public void update(Observable observable, Object data) {
            Log.i(TAG, "==== DialogNotificationObserver  'update' ====");
            if (data != null) {
                Bundle observableData = (Bundle) data;
                int action = observableData.getInt(DialogNotificationDataManager.EXTRA_ACTION);
                DialogNotification dialogNotification = (DialogNotification) observableData.getSerializable(DialogNotificationDataManager.EXTRA_OBJECT);
                if (dialogNotification != null) {
                    CombinationMessage combinationMessage = new CombinationMessage(dialogNotification);
                    if (action == DialogNotificationDataManager.UPDATE_ACTION) {
                        Log.d(TAG, "updated dialogNotification = " + dialogNotification);
                        updateMessageItemInAdapter(combinationMessage);
                    } else if (action == DialogNotificationDataManager.CREATE_OR_UPDATE_ACTION) {
                        Log.d(TAG, "created dialogNotification = " + dialogNotification);
                        addMessageItemToAdapter(combinationMessage);
                        if (currentChatDialog != null && QBDialogType.PRIVATE.equals(currentChatDialog.getType())) {
                            updateMessagesList();
                            messagesAdapter.notifyDataSetChanged();
                            scrollMessagesToBottom();
                        }
                    }
                }

                if (action == DialogNotificationDataManager.DELETE_ACTION) {
                    combinationMessagesList.clear();
                    loadNextPartMessagesFromDb(false, true);
                }
            }
        }
    }

    private class DialogObserver implements Observer {

        @Override
        public void update(Observable observable, Object data) {
            Log.i(TAG, "==== DialogObserver  'update' ====");
            if (data != null) {
                String observerKey = ((Bundle) data).getString(DialogDataManager.EXTRA_OBSERVE_KEY);
                if (observerKey.equals(dataManager.getQBChatDialogDataManager().getObserverKey()) && currentChatDialog != null) {
                    currentChatDialog = dataManager.getQBChatDialogDataManager().getByDialogId(currentChatDialog.getDialogId());
                    if (currentChatDialog != null) {
                        // need init current dialog after getting from DB
                        initCurrentDialog();
                        updateActionBar();
                    } else {
                        finish();
                    }
                }
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
            String localPath = (String) bundle.getSerializable(QBServiceConsts.EXTRA_FILE_PATH);

            sendMessageWithAttachment(dialogId, StringUtils.getAttachmentTypeByFileName(file.getName()), file, localPath);
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

            String dialogId = bundle.getString(QBServiceConsts.EXTRA_DIALOG_ID);

            Log.d("BaseDialogActivity", "Laoding messages finished" + " totalEntries = " + totalEntries
                    + " lastMessageDate = " + lastMessageDate
                    + " isLoadedOldMessages = " + isLoadedOldMessages
                    + " dialogId = " + dialogId);

            if (messagesAdapter != null
                    && totalEntries != ConstsCore.ZERO_INT_VALUE
                    && dialogId.equals(currentChatDialog.getDialogId())) {

                threadPool.execute(new Runnable(){
                    @Override
                    public void run() {
                        loadNextPartMessagesFromDb(isLoadedOldMessages, false);
                        mainThreadHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                updateMessagesAdapter(isLoadedOldMessages, totalEntries);
                                if (currentChatDialog != null && QBDialogType.PRIVATE.equals(currentChatDialog.getType())) {
                                    updateMessagesList();
                                }
                                afterLoadingMessagesActions();
                            }
                        });

                    }
                });

            } else {
                afterLoadingMessagesActions();
            }
        }
    }

    public class LoadDialogMessagesFailAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            afterLoadingMessagesActions();
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
                if (combinationMessagesList.size() == 0){
                    startLoadDialogMessages(true);
                    return;
                }

                long oldestMessageInDb = getMessageDateForLoad(true);
                long oldestMessageInCurrentList = combinationMessagesList.get(0).getCreatedDate();

                if ((oldestMessageInCurrentList - oldestMessageInDb) > 0 && oldestMessageInDb != 0){
                    loadNextPartMessagesFromDb(true, true);
                    messageSwipeRefreshLayout.setRefreshing(false);
                } else {
                    startLoadDialogMessages(true);
                }
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