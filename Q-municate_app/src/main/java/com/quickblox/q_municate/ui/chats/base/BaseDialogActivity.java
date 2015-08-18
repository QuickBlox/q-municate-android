package com.quickblox.q_municate.ui.chats.base;

import android.app.ActionBar;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.core.listeners.ChatUIHelperListener;
import com.quickblox.q_municate.ui.base.BaseFragmentActivity;
import com.quickblox.q_municate.ui.base.BaseListAdapter;
import com.quickblox.q_municate.ui.chats.emoji.EmojiFragment;
import com.quickblox.q_municate.ui.chats.emoji.EmojiGridFragment;
import com.quickblox.q_municate.ui.chats.emoji.emojiTypes.EmojiObject;
import com.quickblox.q_municate.ui.chats.privatedialog.PrivateDialogMessagesAdapter;
import com.quickblox.q_municate.ui.dialogs.AlertDialog;
import com.quickblox.q_municate.ui.uihelper.SimpleTextWatcher;
import com.quickblox.q_municate.utils.ImageLoaderUtils;
import com.quickblox.q_municate.utils.ImageUtils;
import com.quickblox.q_municate.utils.KeyboardUtils;
import com.quickblox.q_municate_core.core.command.Command;
import com.quickblox.q_municate_core.models.CombinationMessage;
import com.quickblox.q_municate_core.qb.commands.QBLoadAttachFileCommand;
import com.quickblox.q_municate_core.qb.commands.QBLoadDialogMessagesCommand;
import com.quickblox.q_municate_core.qb.helpers.QBBaseChatHelper;
import com.quickblox.q_municate_core.qb.helpers.QBGroupChatHelper;
import com.quickblox.q_municate_core.qb.helpers.QBPrivateChatHelper;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.ChatUtils;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_core.utils.DialogUtils;
import com.quickblox.q_municate_core.utils.ErrorUtils;
import com.quickblox.q_municate_core.utils.PrefsHelper;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.managers.DialogNotificationDataManager;
import com.quickblox.q_municate_db.managers.MessageDataManager;
import com.quickblox.q_municate_db.models.Dialog;
import com.quickblox.q_municate_db.models.DialogNotification;
import com.quickblox.q_municate_db.models.DialogOccupant;
import com.quickblox.q_municate_db.models.Message;
import com.quickblox.q_municate_db.models.State;
import com.quickblox.q_municate_db.models.User;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public abstract class BaseDialogActivity extends BaseFragmentActivity implements AbsListView.OnScrollListener, ChatUIHelperListener, EmojiGridFragment.OnEmojiconClickedListener, EmojiFragment.OnEmojiBackspaceClickedListener {

    private static final int TYPING_DELAY = 1000;

    protected Resources resources;
    protected DataManager dataManager;
    protected EditText chatEditText;
    protected StickyListHeadersListView messagesListView;
    protected EditText messageEditText;
    protected TextView messageTextView;
    protected ImageButton sendButton;
    protected ImageButton smilePanelImageButton;
    protected String currentOpponent;
    protected String dialogId;
    protected View emojisFragment;
    protected ImageUtils imageUtils;
    protected int layoutResID;
    protected BaseListAdapter messagesAdapter;
    protected Dialog dialog;
    protected boolean isNeedToScrollMessages;
    protected QBBaseChatHelper baseChatHelper;
    protected User opponentFriend;

    private int keyboardHeight;
    private View rootView;
    private boolean needToShowSmileLayout;
    private View messageTypingView;
    private ImageView messageTypingBoxImageView;
    private LoadAttachFileSuccessAction loadAttachFileSuccessAction;
    private LoadDialogMessagesSuccessAction loadDialogMessagesSuccessAction;
    private LoadDialogMessagesFailAction loadDialogMessagesFailAction;
    private int chatHelperIdentifier;
    private AnimationDrawable messageTypingAnimationDrawable;
    private Timer typingTimer;
    private boolean isTypingNow;
    private int firstVisiblePositionList;
    private View loadMoreView;
    private boolean loadingMore;
    private int skipMessages;
    private boolean firstItemInList;
    private int totalItemCountInList;
    private Observer messageObserver;
    private Observer dialogNotificationObserver;

    public BaseDialogActivity(int layoutResID, int chatHelperIdentifier) {
        this.chatHelperIdentifier = chatHelperIdentifier;
        this.layoutResID = layoutResID;
    }

    @Override
    public void onEmojiconClicked(EmojiObject emojiObject) {
        EmojiFragment.input(messageEditText, emojiObject);
    }

    @Override
    public void onEmojiBackspaceClicked(View v) {
        EmojiFragment.backspace(messageEditText);
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rootView = getLayoutInflater().inflate(layoutResID, null);
        setContentView(rootView);

        resources = getResources();
        dataManager = DataManager.getInstance();
        imageUtils = new ImageUtils(this);
        loadAttachFileSuccessAction = new LoadAttachFileSuccessAction();
        loadDialogMessagesSuccessAction = new LoadDialogMessagesSuccessAction();
        loadDialogMessagesFailAction = new LoadDialogMessagesFailAction();
        typingTimer = new Timer();
        messageObserver = new MessageObserver();
        dialogNotificationObserver = new DialogNotificationObserver();

        initUI();
        initListeners();
        initActionBar();

        addActions();

        isNeedToScrollMessages = true;

        initLocalBroadcastManagers();
        addObservers();
        hideSmileLayout();
    }

    @Override
    protected void onPause() {
        super.onPause();
        onUpdateChatDialog();
        hideSmileLayout();

        // TODO: now it is possible only for Private chats
        if (Dialog.Type.PRIVATE.equals(dialog.getType())) {
            if (isTypingNow) {
                isTypingNow = false;
                sendTypingStatus();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean isNeedToOpenDialog = PrefsHelper.getPrefsHelper().getPref(
                PrefsHelper.PREF_PUSH_MESSAGE_NEED_TO_OPEN_DIALOG, false);
        if (isNeedToOpenDialog) {
            finish();
        }
    }

    @Override
    public void onConnectedToService(QBService service) {
        super.onConnectedToService(service);
        onConnectServiceLocally(service);
    }

    private void addObservers() {
        dataManager.getMessageDataManager().addObserver(messageObserver);
        dataManager.getDialogNotificationDataManager().addObserver(dialogNotificationObserver);
    }

    private void deleteObservers() {
        dataManager.getMessageDataManager().deleteObserver(messageObserver);
        dataManager.getDialogNotificationDataManager().deleteObserver(dialogNotificationObserver);
    }

    protected void updateData() {
        dialog = dataManager.getDialogDataManager().getByDialogId(dialogId);
        if (dialog != null) {
            updateActionBar();
        }
        updateMessagesList();
    }

    protected abstract void updateActionBar();

    private void initLocalBroadcastManagers() {
        BroadcastReceiver typingMessageBroadcastReceiver = new TypingStatusBroadcastReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(typingMessageBroadcastReceiver,
                new IntentFilter(QBServiceConsts.TYPING_MESSAGE));

        BroadcastReceiver updatingDialogBroadcastReceiver = new UpdatingDialogBroadcastReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(updatingDialogBroadcastReceiver,
                new IntentFilter(QBServiceConsts.UPDATE_DIALOG));
    }

    protected abstract void onConnectServiceLocally(QBService service);

    protected void onConnectServiceLocally() {
        if (baseChatHelper == null) {
            baseChatHelper = (QBBaseChatHelper) getService().getHelper(chatHelperIdentifier);
            try {
                baseChatHelper.createChatLocally(ChatUtils.createQBDialogFromLocalDialog(dialog),
                        generateBundleToInitDialog());
            } catch (QBResponseException e) {
                ErrorUtils.showError(this, e.getMessage());
                finish();
            }
        }
    }

    protected void startMessageTypingAnimation() {
        messageTypingView.setVisibility(View.VISIBLE);
        messageTypingAnimationDrawable.start();
    }

    protected void stopMessageTypingAnimation() {
        messageTypingView.setVisibility(View.GONE);
        messageTypingAnimationDrawable.stop();
    }

    protected void attachButtonOnClick() {
        canPerformLogout.set(false);

        CharSequence[] itemsArray = resources.getStringArray(R.array.dlg_attach_types_array);

        final android.app.Dialog alertDialog = DialogUtils.createSingleChoiceItemsDialog(this,
                resources.getString(R.string.dlg_select_attach_type), itemsArray,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        switch (item) {
                            case 0:
                                imageUtils.getCaptureImage();
                                break;
                            case 1:
                                imageUtils.getImage();
                                break;
                        }

                        //                alertDialog.dismiss();
                    }
                });

        alertDialog.show();
    }

    private void hideSmileLayout() {
        emojisFragment.setVisibility(View.GONE);
        setSmilePanelIcon(R.drawable.ic_smile);
    }

    private void showSmileLayout(int keyboardHeight) {
        needToShowSmileLayout = false;
        emojisFragment.setVisibility(View.VISIBLE);
        if (keyboardHeight != ConstsCore.ZERO_INT_VALUE) {
            ViewGroup.LayoutParams params = emojisFragment.getLayoutParams();
            params.height = keyboardHeight;
            emojisFragment.setLayoutParams(params);
        }
        setSmilePanelIcon(R.drawable.ic_keyboard);
    }

    protected void addActions() {
        addAction(QBServiceConsts.LOAD_ATTACH_FILE_SUCCESS_ACTION, loadAttachFileSuccessAction);
        addAction(QBServiceConsts.LOAD_ATTACH_FILE_FAIL_ACTION, failAction);
        addAction(QBServiceConsts.LOAD_DIALOG_MESSAGES_SUCCESS_ACTION, loadDialogMessagesSuccessAction);
        addAction(QBServiceConsts.LOAD_DIALOG_MESSAGES_FAIL_ACTION, loadDialogMessagesFailAction);
        addAction(QBServiceConsts.ACCEPT_FRIEND_SUCCESS_ACTION, new AcceptFriendSuccessAction());
        addAction(QBServiceConsts.ACCEPT_FRIEND_FAIL_ACTION, failAction);
        addAction(QBServiceConsts.REJECT_FRIEND_SUCCESS_ACTION, new RejectFriendSuccessAction());
        addAction(QBServiceConsts.REJECT_FRIEND_FAIL_ACTION, failAction);
        updateBroadcastActionList();
    }

    protected abstract void onUpdateChatDialog();

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        canPerformLogout.set(true);
        if ((isGalleryCalled(requestCode) || isCaptureCalled(requestCode)) && resultCode == RESULT_OK) {
            isNeedToScrollMessages = true;
            if (data.getData() == null) {
                onFileSelected((Bitmap) data.getExtras().get("data"));
            } else {
                onFileSelected(data.getData());
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onStop() {
        super.onStop();
        readAllMessages();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (baseChatHelper != null) {
            baseChatHelper.closeChat(ChatUtils.createQBDialogFromLocalDialog(dialog),
                    generateBundleToInitDialog());
        }
        removeActions();
        deleteObservers();
    }

    private boolean isGalleryCalled(int requestCode) {
        return ImageUtils.GALLERY_INTENT_CALLED == requestCode;
    }

    private boolean isCaptureCalled(int requestCode) {
        return ImageUtils.CAPTURE_CALLED == requestCode;
    }

    private void initActionBar() {
        actionBar.setDisplayOptions(
                ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_USE_LOGO);
        initColorsActionBar();
    }

    private void initColorsActionBar() {
        int actionBarTitleId = Resources.getSystem().getIdentifier("action_bar_title", "id", "android");
        int actionBarSubTitleId = Resources.getSystem().getIdentifier("action_bar_subtitle", "id", "android");
        if (actionBarTitleId > ConstsCore.ZERO_INT_VALUE) {
            TextView title = (TextView) findViewById(actionBarTitleId);
            if (title != null) {
                title.setTextColor(Color.WHITE);
            }
        }
        if (actionBarSubTitleId > ConstsCore.ZERO_INT_VALUE) {
            TextView subTitle = (TextView) findViewById(actionBarSubTitleId);
            if (subTitle != null) {
                float alpha = 0.5f;
                subTitle.setTextColor(Color.WHITE);
                subTitle.setAlpha(alpha);
            }
        }
    }

    protected void loadLogoActionBar(String logoUrl) {
        ImageLoader.getInstance().loadImage(logoUrl, ImageLoaderUtils.UIL_USER_AVATAR_DISPLAY_OPTIONS,
                new SimpleImageLoadingListener() {

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedBitmap) {
                        startUpdatingActionBarLogo(loadedBitmap);
                    }
                });
    }

    private void startUpdatingActionBarLogo(Bitmap loadedBitmap) {
        new ReceiveRoundedBitmapPathTask().execute(loadedBitmap);
    }

    private void updateActionBarLogo(Bitmap roundedBitmap) {
        actionBar.setLogo(new BitmapDrawable(getResources(), roundedBitmap));
    }

    protected void removeActions() {
        removeAction(QBServiceConsts.LOAD_ATTACH_FILE_SUCCESS_ACTION);
        removeAction(QBServiceConsts.LOAD_ATTACH_FILE_FAIL_ACTION);
        removeAction(QBServiceConsts.LOAD_DIALOG_MESSAGES_SUCCESS_ACTION);
        removeAction(QBServiceConsts.LOAD_DIALOG_MESSAGES_FAIL_ACTION);
    }

    protected abstract void onFileSelected(Uri originalUri);

    protected abstract void onFileSelected(Bitmap bitmap);

    protected void startLoadAttachFile(final File file) {
        final AlertDialog alertDialog = AlertDialog.newInstance(getResources().getString(
                R.string.dlg_confirm_sending_attach));
        alertDialog.setPositiveButton(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showProgress();
                QBLoadAttachFileCommand.start(BaseDialogActivity.this, file);
            }
        });
        alertDialog.setNegativeButton(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
            }
        });
        alertDialog.show(getFragmentManager(), null);
    }

    protected abstract void onFileLoaded(QBFile file);

    protected void startLoadDialogMessages(Dialog dialog, long lastDateLoad) {
        loadingMore = true;
        QBLoadDialogMessagesCommand.start(this, ChatUtils.createQBDialogFromLocalDialog(dialog),
                lastDateLoad, skipMessages);
        skipMessages += ConstsCore.DIALOG_MESSAGES_PER_PAGE;
    }

    protected abstract Bundle generateBundleToInitDialog();

    private void initUI() {
        emojisFragment = _findViewById(R.id.emojicons_fragment);
        chatEditText = _findViewById(R.id.message_edittext);
        messagesListView = _findViewById(R.id.messages_listview);
        messageEditText = _findViewById(R.id.message_edittext);
        sendButton = _findViewById(R.id.send_button);
        smilePanelImageButton = _findViewById(R.id.smile_panel_imagebutton);
        sendButton.setEnabled(false);
        messageTextView = _findViewById(R.id.message_textview);
        messageTypingView = _findViewById(R.id.message_typing_view);
        messageTypingBoxImageView = _findViewById(R.id.message_typing_box_imageview);
        messageTypingAnimationDrawable = (AnimationDrawable) messageTypingBoxImageView.getDrawable();
        loadMoreView = _findViewById(R.id.load_more_linearlayout);
        loadMoreView.setVisibility(View.GONE);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == SCROLL_STATE_IDLE) {
            if (firstItemInList && !loadingMore) {
                firstVisiblePositionList = totalItemCountInList - 1;
                loadMoreItems();
            }
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        firstItemInList = (firstVisibleItem + totalItemCount) == totalItemCount;
        totalItemCountInList = totalItemCount;
    }

    private void loadMoreItems() {
        startLoadDialogMessages();
    }

    protected abstract void initListView();

    private void setSendButtonVisibility(CharSequence charSequence) {
        if (TextUtils.isEmpty(charSequence) || TextUtils.isEmpty(charSequence.toString().trim())) {
            sendButton.setEnabled(false);
        } else {
            sendButton.setEnabled(true);
        }
    }

    private void initListeners() {
        messageEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                hideSmileLayout();

                // TODO: now it is possible only for Private chats
                if (Dialog.Type.PRIVATE.equals(dialog.getType())) {
                    if (!isTypingNow) {
                        isTypingNow = true;
                        sendTypingStatus();
                    }
                }

                return false;
            }
        });

        messageEditText.addTextChangedListener(new SimpleTextWatcher() {

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                super.onTextChanged(charSequence, start, before, count);
                setSendButtonVisibility(charSequence);

                // TODO: now it is possible only for Private chats
                if (Dialog.Type.PRIVATE.equals(dialog.getType())) {
                    if (!isTypingNow) {
                        isTypingNow = true;
                        sendTypingStatus();
                    }
                    checkStopTyping();
                }
            }
        });

        smilePanelImageButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (isSmilesLayoutShowing()) {
                    hideSmileLayout();
                    KeyboardUtils.showKeyboard(BaseDialogActivity.this);
                } else {
                    KeyboardUtils.hideKeyboard(BaseDialogActivity.this);
                    needToShowSmileLayout = true;
                    if (keyboardHeight == ConstsCore.ZERO_INT_VALUE) {
                        showSmileLayout(ConstsCore.ZERO_INT_VALUE);
                    }
                }
            }
        });

        rootView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {

                    @Override
                    public void onGlobalLayout() {
                        initKeyboardHeight();
                        if (needToShowSmileLayout) {
                            showSmileLayout(keyboardHeight);
                        }
                    }
                });
    }

    private void checkStopTyping() {
        typingTimer.cancel();
        typingTimer = new Timer();
        typingTimer.schedule(new TypingTimerTask(), TYPING_DELAY);
    }

    private void sendTypingStatus() {
        baseChatHelper.sendTypingStatusToServer(opponentFriend.getUserId(), isTypingNow);
    }

    private void initKeyboardHeight() {
        final int EXPECTED_HEIGHT = 100;
        Rect r = new Rect();
        rootView.getWindowVisibleDisplayFrame(r);
        int screenHeight = rootView.getRootView().getHeight();
        int heightDifference = screenHeight - (r.bottom - r.top);
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > ConstsCore.ZERO_INT_VALUE) {
            heightDifference -= getResources().getDimensionPixelSize(resourceId);
        }
        if (heightDifference > EXPECTED_HEIGHT) {
            keyboardHeight = heightDifference;
        }
    }

    private void setSmilePanelIcon(int resourceId) {
        smilePanelImageButton.setImageResource(resourceId);
    }

    protected boolean isSmilesLayoutShowing() {
        return emojisFragment.getVisibility() == View.VISIBLE;
    }

    @Override
    public void onScrollMessagesToBottom() {
        scrollListView();
    }

    @Override
    public void onScreenResetPossibilityPerformLogout(boolean canPerformLogout) {
        this.canPerformLogout.set(canPerformLogout);
    }

    protected void scrollListView() {
        if (isNeedToScrollMessages) {
            isNeedToScrollMessages = false;
            messagesListView.setSelection(messagesAdapter.getCount() - 1);
        }
    }

    protected void sendMessage(boolean privateMessage) {
        boolean error = false;
        try {
            if (privateMessage) {
                ((QBPrivateChatHelper) baseChatHelper).sendPrivateMessage(
                        messageEditText.getText().toString(), opponentFriend.getUserId());
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
        }

        if (!error) {
            messageEditText.setText(ConstsCore.EMPTY_STRING);
            isNeedToScrollMessages = true;
            scrollListView();
        }
    }

    protected List<CombinationMessage> createCombinationMessagesList() {
        List<CombinationMessage> combinationMessagesList = new ArrayList<>();
        List<Message> messagesList = dataManager.getMessageDataManager().getMessagesByDialogId(dialogId);
        List<DialogNotification> dialogNotificationsList = dataManager.getDialogNotificationDataManager()
                .getDialogNotificationsByDialogId(dialogId);
        combinationMessagesList.addAll(ChatUtils.getCombinationMessagesListFromMessagesList(messagesList));
        combinationMessagesList.addAll(ChatUtils.getCombinationMessagesListFromDialogNotificationsList(
                dialogNotificationsList));
        Collections.sort(combinationMessagesList, new CombinationMessage.DateComparator());
        return combinationMessagesList;
    }

    //    abstract QBDialog getQBDialog();

    protected void startUpdateChatDialog() {
        //        QBDialog dialog = getQBDialog();
        //        if (dialog != null) {
        //            QBUpdateDialogLocalCommand.start(this, dialog);
        //        }
    }

    protected void startLoadDialogMessages() {
        if (dialog == null) {
            return;
        }

        showActionBarProgress();

        List<DialogOccupant> dialogOccupantsList = DataManager.getInstance().getDialogOccupantDataManager()
                .getDialogOccupantsListByDialogId(dialog.getDialogId());
        List<Integer> dialogOccupantsIdsList = ChatUtils.getIdsFromDialogOccupantsList(dialogOccupantsList);
        Message lastReadMessage = DataManager.getInstance().getMessageDataManager().getLastMessageByDialogId(
                dialogOccupantsIdsList);
        if (lastReadMessage == null) {
            startLoadDialogMessages(dialog, ConstsCore.ZERO_LONG_VALUE);
        } else {
            long lastMessageDateSent = lastReadMessage.getCreatedDate();
            startLoadDialogMessages(dialog, lastMessageDateSent);
        }
    }

    protected abstract void updateMessagesList();

    private void readAllMessages() {
        List<Message> messagesList = dataManager.getMessageDataManager().getMessagesByDialogId(dialogId);
        List<Message> updateMessagesList = new ArrayList<>();
        for (Message message : messagesList) {
            if (message.getState().equals(State.DELIVERED)) {
                message.setState(State.READ);
                updateMessagesList.add(message);
            }
        }
        dataManager.getMessageDataManager().createOrUpdate(updateMessagesList);

        List<DialogNotification> dialogNotificationsList = dataManager.getDialogNotificationDataManager()
                .getDialogNotificationsByDialogId(dialogId);
        List<DialogNotification> updateDialogNotificationsList = new ArrayList<>();
        for (DialogNotification dialogNotification : dialogNotificationsList) {
            if (dialogNotification.getState().equals(State.DELIVERED)) {
                dialogNotification.setState(State.READ);
                updateDialogNotificationsList.add(dialogNotification);
            }
        }
        dataManager.getDialogNotificationDataManager().createOrUpdate(updateDialogNotificationsList);
    }

    private class MessageObserver implements Observer {

        @Override
        public void update(Observable observable, Object data) {
            if (data != null && data.equals(MessageDataManager.OBSERVE_KEY)) {
                updateMessagesList();
            }
        }
    }

    private class DialogNotificationObserver implements Observer {

        @Override
        public void update(Observable observable, Object data) {
            if (data != null && data.equals(DialogNotificationDataManager.OBSERVE_KEY)) {
                updateMessagesList();
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

    private class ReceiveRoundedBitmapPathTask extends AsyncTask<Object, Bitmap, Bitmap> {

        @Override
        protected Bitmap doInBackground(Object[] params) {
            Bitmap bitmap = (Bitmap) params[0];
            Bitmap roundedBitmap = imageUtils.getRoundedBitmap(bitmap);
            return roundedBitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            updateActionBarLogo(bitmap);
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
            //            totalEntries = bundle.getInt(QBServiceConsts.EXTRA_TOTAL_ENTRIES);
            loadingMore = false;

            if (skipMessages != 0) {
                messagesListView.setSelection(0);
            }

            hideActionBarProgress();
        }
    }

    public class LoadDialogMessagesFailAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            loadingMore = false;

            if (skipMessages != 0) {
                messagesListView.setSelection(0);
            }

            hideActionBarProgress();
        }
    }

    private class AcceptFriendSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            ((PrivateDialogMessagesAdapter) messagesAdapter).clearLastRequestMessagePosition();
            hideProgress();
            startLoadDialogMessages();
        }
    }

    private class RejectFriendSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            ((PrivateDialogMessagesAdapter) messagesAdapter).clearLastRequestMessagePosition();
            hideProgress();
            startLoadDialogMessages();
        }
    }

    private class TypingStatusBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            // TODO: now it is possible only for Private chats
            if (Dialog.Type.PRIVATE.equals(dialog.getType())) {
                boolean isTyping = extras.getBoolean(QBServiceConsts.EXTRA_IS_TYPING);
                if (isTyping) {
                    startMessageTypingAnimation();
                } else {
                    stopMessageTypingAnimation();
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
}