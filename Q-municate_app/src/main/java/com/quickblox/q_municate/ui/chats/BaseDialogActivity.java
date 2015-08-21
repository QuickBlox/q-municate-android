package com.quickblox.q_municate.ui.chats;

import android.app.ActionBar;
import android.app.Dialog;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.res.Resources;
import android.database.Cursor;
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
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.base.BaseCursorAdapter;
import com.quickblox.q_municate.ui.base.BaseFragmentActivity;
import com.quickblox.q_municate.ui.chats.emoji.EmojiFragment;
import com.quickblox.q_municate.ui.chats.emoji.EmojiGridFragment;
import com.quickblox.q_municate.ui.chats.emoji.emojiTypes.EmojiObject;
import com.quickblox.q_municate.ui.dialogs.AlertDialog;
import com.quickblox.q_municate.ui.uihelper.SimpleTextWatcher;
import com.quickblox.q_municate.utils.Consts;
import com.quickblox.q_municate.utils.ImageUtils;
import com.quickblox.q_municate.utils.KeyboardUtils;
import com.quickblox.q_municate_core.core.command.Command;
import com.quickblox.q_municate_core.db.managers.ChatDatabaseManager;
import com.quickblox.q_municate_core.db.tables.MessageTable;
import com.quickblox.q_municate_core.models.MessageCache;
import com.quickblox.q_municate_core.models.MessagesNotificationType;
import com.quickblox.q_municate_core.models.User;
import com.quickblox.q_municate_core.qb.commands.QBLoadAttachFileCommand;
import com.quickblox.q_municate_core.qb.commands.QBLoadDialogMessagesCommand;
import com.quickblox.q_municate_core.qb.commands.QBUpdateDialogCommand;
import com.quickblox.q_municate_core.qb.helpers.QBBaseChatHelper;
import com.quickblox.q_municate_core.qb.helpers.QBMultiChatHelper;
import com.quickblox.q_municate_core.qb.helpers.QBPrivateChatHelper;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_core.utils.DialogUtils;
import com.quickblox.q_municate_core.utils.ErrorUtils;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public abstract class BaseDialogActivity extends BaseFragmentActivity implements AbsListView.OnScrollListener,
        LoaderManager.LoaderCallbacks<Cursor>, SwitchViewListener, ChatUIHelperListener, EmojiGridFragment.OnEmojiconClickedListener,
        EmojiFragment.OnEmojiBackspaceClickedListener {

    private static final String TAG = BaseDialogActivity.class.getSimpleName();

    private static final int TYPING_DELAY = 1000;
    private static final int MESSAGES_LOADER_ID = 0;

    protected Resources resources;
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
    protected BaseCursorAdapter messagesAdapter;
    protected QBDialog dialog;
    protected boolean isNeedToScrollMessages;
    protected QBBaseChatHelper baseChatHelper;
    protected volatile User opponentFriend;

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
    private BroadcastReceiver typingMessageBroadcastReceiver;
    private BroadcastReceiver updatingDialogBroadcastReceiver;

    private int firstVisiblePositionList;
    private View loadMoreView;
    private boolean loadingMore = true;
    protected volatile int skipMessages;
    private int totalEntries;
    private int loadedItems;
    private boolean firstItemInList;
    private int totalItemCountInList;
    private int firstVisiblePosition;
    private int lastVisiblePosition;
    private int visibleItemCount;
    private boolean isFirstUpdateListView = true;
    private UpdateMessagesReason updateMessagesReason;
    private boolean isListInBottomNow;
    private boolean isTypingAnimationShown;
    private int lastMessagesCountInDB;
    private boolean isNeedShowTostAboutDisconnected;



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
        imageUtils = new ImageUtils(this);
        loadAttachFileSuccessAction = new LoadAttachFileSuccessAction();
        loadDialogMessagesSuccessAction = new LoadDialogMessagesSuccessAction();
        loadDialogMessagesFailAction = new LoadDialogMessagesFailAction();
        typingTimer = new Timer();
        updateMessagesReason = UpdateMessagesReason.NONE;
        isNeedToScrollMessages = true;

        initUI();
        initListeners();
        initActionBar();
        addActions();
        initLocalBroadcastManagers();
        hideSmileLayout();
    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.d("Fixes CHAT", "BaseDialogActivity onStart");
        // Set Default update reason to start loading of new messages
        updateMessagesReason = UpdateMessagesReason.DEFAULT;

        if (TextUtils.isEmpty(messageEditText.getText().toString().trim())) {
            String messageBody = ChatDatabaseManager.getNotSendMessageBodyByDialogId(getApplicationContext(), dialogId);
            if (messageBody != null){
                messageEditText.setText(messageBody);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("Fixes CHAT", "BaseDialogActivity onPause");
        onUpdateChatDialog();
        hideSmileLayout();

        // TODO: now it is possible only for Private chats
        if (QBDialogType.PRIVATE.equals(dialog.getType())) {
            if (isTypingNow) {
                isTypingNow = false;
                sendTypingStatus();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isNeedShowTostAboutDisconnected = true;
        Log.d("Fixes CHAT", "BaseDialogActivity onResume");
        startLoadDialogMessages();
    }

    @Override
    protected void onStop() {
        isNeedShowTostAboutDisconnected = false;
        if (!TextUtils.isEmpty(messageEditText.getText().toString().trim())){
           ChatDatabaseManager.saveNotSendMessage(getApplicationContext(), messageEditText.getText().toString(), dialogId, null);
        } else {
            ChatDatabaseManager.deleteNotSentMessagesByDialogId(getApplicationContext(), dialogId);
        }

        super.onStop();
    }

    @Override
    public void onConnectedToService(QBService service) {
        super.onConnectedToService(service);
        createChatLocally();
    }

    protected void updateDialogData() {
        dialog = ChatDatabaseManager.getDialogByDialogId(this, dialogId);
        if (dialog != null) {
            updateActionBar();
        }
    }

    protected abstract void updateActionBar();

    private void initLocalBroadcastManagers() {
        typingMessageBroadcastReceiver = new TypingStatusBroadcastReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(typingMessageBroadcastReceiver,
                new IntentFilter(QBServiceConsts.TYPING_MESSAGE));

        updatingDialogBroadcastReceiver = new UpdatingDialogBroadcastReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(updatingDialogBroadcastReceiver,
                new IntentFilter(QBServiceConsts.UPDATE_DIALOG));
    }

    protected void createChatLocally() {
        if (baseChatHelper == null) {
            baseChatHelper = (QBBaseChatHelper) getService().getHelper(chatHelperIdentifier);
            try {
                baseChatHelper.createChatLocally(dialog, generateBundleToInitDialog());
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

        Dialog dialog = DialogUtils.createSingleChoiceItemsDialog(this, resources.getString(
                R.string.dlg_select_attach_type), itemsArray, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                switch (item) {
                    case 0:
                        imageUtils.getCaptureImage();
                        break;
                    case 1:
                        imageUtils.getImage();
                        break;
                }

                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    public void showLastListItem() {
        if (isSmilesLayoutShowing()) {
            hideSmileLayout();
        }
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
        addAction(QBServiceConsts.RE_LOGIN_IN_CHAT_SUCCESS_ACTION, new ReloginSuccessAction());
        addAction(QBServiceConsts.RE_LOGIN_IN_CHAT_FAIL_ACTION, new ReloginFailAction());
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
    protected void onDestroy() {
        super.onDestroy();
        if (baseChatHelper != null) {
            baseChatHelper.closeChat(dialog, generateBundleToInitDialog());
        }
        removeActions();
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
        ImageLoader.getInstance().loadImage(logoUrl, Consts.UIL_USER_AVATAR_DISPLAY_OPTIONS,
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
        removeAction(QBServiceConsts.RE_LOGIN_IN_CHAT_SUCCESS_ACTION);
        removeAction(QBServiceConsts.RE_LOGIN_IN_CHAT_FAIL_ACTION);
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

    protected void startLoadDialogMessages(QBDialog dialog, long lastDateLoad) {
        if (loadingMore) {
            Log.d("Fixes CHAT", "start Load Dialog Messages for dialog " + dialog + " where last date load " + lastDateLoad + " skip messages " + skipMessages);
            QBLoadDialogMessagesCommand.start(this, dialog, lastDateLoad,
                    skipMessages = lastDateLoad == ConstsCore.ZERO_LONG_VALUE ?
                            ConstsCore.ZERO_INT_VALUE : skipMessages);
            loadingMore = false;
        }
    }


    protected void startNewMessagesLoadDialogMessages(QBDialog dialog, long lastDateLoad, String lastReadMessageID) {
        Log.d("Fixes CHAT", "loadingMore = " + loadingMore);
        if (loadingMore) {
            Log.d("Fixes CHAT", "start Load Dialog Messages for dialog " + dialog + " where last date load " + lastDateLoad + " skip messages " + skipMessages);
            QBLoadDialogMessagesCommand.start(this, dialog, lastDateLoad, lastReadMessageID, ConstsCore.NOT_INITIALIZED_VALUE);
            loadingMore = false;
        }
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
        messagesListView.setOnScrollListener(this);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == SCROLL_STATE_IDLE || scrollState == SCROLL_STATE_TOUCH_SCROLL) {
            if (firstVisiblePosition == 0){
                updateMessagesReason = UpdateMessagesReason.ON_USER_REQUEST;
                loadMoreItems();
            }
        }

        if(isTypingAnimationShown) {
            isTypingAnimationShown = false;
            stopMessageTypingAnimation();
        }

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                         int totalItemCount) {

        firstVisiblePosition = firstVisibleItem;
        lastVisiblePosition = firstVisibleItem + visibleItemCount;
        this.visibleItemCount = visibleItemCount;
        this.isListInBottomNow = (visibleItemCount + firstVisibleItem) == totalItemCount;


        if (firstVisiblePosition > 1){
            updateMessagesReason = UpdateMessagesReason.NONE;
        }
    }

    private void loadMoreItems() {
        startLoadDialogMessages();
    }

    protected void initCursorLoaders() {
        getLoaderManager().initLoader(MESSAGES_LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return ChatDatabaseManager.getAllDialogMessagesLoaderByDialogId(this, dialogId);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor messagesCursor) {
        Log.d("Fixes CHAT", "onLoadFinished");
        if (messagesAdapter == null) {
            Log.d("Fixes CHAT", "onLoadFinished Adapter absent");
            initListView(messagesCursor);
        } else {
            Log.d("Fixes CHAT", "onLoadFinished Adapter exist");
            Log.d("Fixes CHAT", "onLoadFinished update reason " + updateMessagesReason.name());
            messagesAdapter.swapCursor(messagesCursor);


            // Set position depends on first load
            // We use two points in class which signaling us about loading messages
            // It is onLoadFinished method and inner class LoadDialogMessagesSuccessAction
            //
            // LoadDialogMessagesSuccessAction contains execute method which called when loading messages and
            // saving them in base completely finished. Also intent parameter contains value of loaded messages
            // which could be used as final value of loaded messages
            //
            // At same time onLoadFinished called each time when database data was changed, it means
            // that QBLoadDialogMessagesCommand causes onLoadFinished dialog retrieving.
            //
            // Both of points of loading dialogs depends from each other and we have no warranty about order of their execution
            // To prevent incorrect work of dialog positioning we use difference of messages in base
            // before and after onLoadFinished method was called to determ last visible message position in
            // updated list
            //

            int currentMessageCountInBase = ChatDatabaseManager.getAllDialogMessagesByDialogId(this, dialogId).getCount();


            if (currentMessageCountInBase > lastMessagesCountInDB) {
                totalEntries = currentMessageCountInBase - lastMessagesCountInDB;
                lastMessagesCountInDB = currentMessageCountInBase;
            }


            if (totalEntries > 0) {
                if (UpdateMessagesReason.ON_USER_REQUEST == updateMessagesReason) {
                    int loadMessages = ConstsCore.DIALOG_MESSAGES_PER_PAGE < totalEntries ?
                            ConstsCore.DIALOG_MESSAGES_PER_PAGE : totalEntries;
                    Log.d("Fixes CHAT", "onLoadFinished Set selection  " + loadMessages);
                    messagesListView.setSelection(loadMessages - 1);
                    resetTotalEntries();
                } else if (UpdateMessagesReason.DEFAULT == updateMessagesReason) {
                    Log.d("Fixes CHAT", "onLoadFinished load messages by default reason");
                    scrollListView();
                }
            }

//            // Set state to none to prevent scrolling to the bottom of the list on each data update
//            // Update list position only for first load
//            updateMessagesReason = UpdateMessagesReason.NONE;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    protected abstract void initListView(Cursor messagesCursor);

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
                if (QBDialogType.PRIVATE.equals(dialog.getType())) {
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
                if (QBDialogType.PRIVATE.equals(dialog.getType())) {
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
        if (baseChatHelper != null) {
            baseChatHelper.sendTypingStatusToServer(opponentFriend.getUserId(), isTypingNow);
        }
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
        isNeedToScrollMessages = false;
        messagesListView.setSelection(messagesAdapter.getCount() - 1);
    }

    abstract QBDialog getQBDialog();

    protected void sendMessage(boolean privateMessage) {
        boolean error = false;
        try {
            if (privateMessage) {
                ((QBPrivateChatHelper) baseChatHelper).sendPrivateMessage(
                        messageEditText.getText().toString(), opponentFriend.getUserId());
            } else {
                ((QBMultiChatHelper) baseChatHelper).sendGroupMessage(dialog.getRoomJid(),
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

    protected void startUpdateChatDialog() {
        QBDialog dialog = getQBDialog();
        if (dialog != null) {
            QBUpdateDialogCommand.start(this, dialog);
        }
    }

    protected void startLoadDialogMessages() {
        Log.d("Fixes CHAT", "startLoadDialogMessages");
        Log.d("Fixes CHAT", "updateMessagesReason = " + String.valueOf(updateMessagesReason));
        if (dialog == null) {
            return;
        }

        lastMessagesCountInDB = ChatDatabaseManager.getAllDialogMessagesByDialogId(this, dialogId).getCount();
        showActionBarProgress();

        MessageCache lastReadMessage = ChatDatabaseManager.getLastSyncMessage(this, dialog);
        Log.d("Fixes CHAT", "Last synch mesasge is " + lastReadMessage);
        if (lastReadMessage == null) {
            Log.d("Fixes CHAT", "Last synch mesasge is null");
            startLoadDialogMessages(dialog, ConstsCore.ZERO_LONG_VALUE);
            updateMessagesReason = UpdateMessagesReason.DEFAULT;
        } else if (UpdateMessagesReason.DEFAULT == updateMessagesReason){
            Log.d("Fixes CHAT", "startLoadDialogMessages by default reason");
            startNewMessagesLoadDialogMessages(dialog, lastReadMessage.getTime(), lastReadMessage.getId());
        } else if (UpdateMessagesReason.ON_USER_REQUEST == updateMessagesReason) {
            Log.d("Fixes CHAT", "startLoadDialogMessages by user request or none reason");
            startLoadDialogMessages(dialog, lastReadMessage.getTime());
        }
    }

    public boolean isFirstDialogLaunch() {
        Cursor messagesCursor = ChatDatabaseManager.getAllDialogMessagesByDialogId(this, dialog.getDialogId());
        boolean result = true;
        if(messagesCursor.moveToFirst()){
            try {
                do {
                    if (!isMessageTypeContactRequest(messagesCursor.getString(messagesCursor.getColumnIndex(MessageTable.Cols.FRIENDS_NOTIFICATION_TYPE)))) {
                        result =  false;
                        break;
                    }
                } while (messagesCursor.moveToNext());
            } finally {
                messagesCursor.close();
            }
        }
        return result;
    }

    private boolean isMessageTypeContactRequest(String messageNotificationType) {
        try{
            Integer messageType = Integer.valueOf(messageNotificationType);

            if(MessagesNotificationType.FRIENDS_REQUEST.getCode() == messageType
                    || MessagesNotificationType.FRIENDS_ACCEPT.getCode() == messageType
                    || MessagesNotificationType.FRIENDS_REJECT.getCode() == messageType){
                return true;
            } else {
                return false;
            }
        } catch (NumberFormatException e){
            return false;
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

            Log.d("Fixes CHAT", "LoadDialogMessagesSuccessAction");
            skipMessages += bundle.getInt(QBServiceConsts.EXTRA_TOTAL_ENTRIES);
            Log.d("Fixes CHAT", "LoadDialogMessagesSuccessAction Next skip messages " + skipMessages);

            // Set totalEntries value only if download messages command have been executed by user request.
            // TotalEntries value used for setting position of list after it was updated.
            // If loading of messages command has executed by default reason (user returns to dialog
            // or dialog was opened before) or at first time we shouldn't initiate totalEntries value
            // also we should erase it if it different from zero

//            if (UpdateMessagesReason.ON_USER_REQUEST == updateMessagesReason){
//                totalEntries = bundle.getInt(QBServiceConsts.EXTRA_TOTAL_ENTRIES);
//            } else {
//                resetTotalEntries();
//            }

            // Set update

            loadingMore = true;
            hideActionBarProgress();
        }
    }

    private void resetTotalEntries() {
        if(totalEntries > 0){
            totalEntries = ConstsCore.ZERO_INT_VALUE;
        }
    }

    public class LoadDialogMessagesFailAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            Log.d("Fixes CHAT", "LoadDialogMessagesFailAction");
            loadingMore = true;
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
            if(dialog != null) {
                if (QBDialogType.PRIVATE.equals(dialog.getType())) {
                    boolean isTyping = extras.getBoolean(QBServiceConsts.EXTRA_IS_TYPING);
                    if (isTyping && isListInBottomNow) {
                        isTypingAnimationShown = true;
                        scrollListView();
                        startMessageTypingAnimation();
                    } else {
                        isTypingAnimationShown = false;
                        stopMessageTypingAnimation();
                    }
                }
            }
        }
    }

    private class UpdatingDialogBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(QBServiceConsts.UPDATE_DIALOG)) {
                updateDialogData();
            }
        }
    }


    public class ReloginSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            Log.d("Fixes STATUS", "Relogin success action");
            Log.d("Fixes CHAT", "ChatService is init " + QBChatService.isInitialized());
            if (QBChatService.isInitialized()) {
                Log.d("Fixes CHAT", "ChatService is logged in " + QBChatService.getInstance().isLoggedIn());
            }
            Toast.makeText(BaseDialogActivity.this, getString(R.string.relgn_success), Toast.LENGTH_LONG).show();

            /*
            Set update reason to default for loading new messages if operations is success
             */
            updateMessagesReason = UpdateMessagesReason.DEFAULT;

            // Update adapter to cause message state updating
            messagesListView.invalidateViews();

            startLoadDialogMessages();
        }
    }

    public class ReloginFailAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            Toast.makeText(BaseDialogActivity.this, getString(R.string.relgn_fail), Toast.LENGTH_LONG).show();
            AlertDialog dialog = AlertDialog.newInstance(getString(R.string.relgn_fail));
            dialog.setPositiveButton(new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    getService().forceRelogin();
                }
            });
            dialog.setNegativeButton(new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(BaseDialogActivity.this, getString(R.string.dont_forget_relogin), Toast.LENGTH_LONG).show();
                }
            });
            dialog.show(getFragmentManager(), null);
        }
    }

    @Override
    public void onConnectionChange(boolean isConnected) {
        super.onConnectionChange(isConnected);
        /**
         * If network was turned off set #updateMessagesReason
         * to {@link com.quickblox.q_municate.ui.chats.BaseDialogActivity.UpdateMessagesReason.NONE}
         * for preventing loading data in #onResume method before async command of relogining was executed
         */
        if(!isConnected) {
            updateMessagesReason = UpdateMessagesReason.NONE;
            showToastAboutDisconnectedIfNeed();
        }
    }

    private void showToastAboutDisconnectedIfNeed() {
        if (isNeedShowTostAboutDisconnected){
            Toast.makeText(this, this.getString(com.quickblox.q_municate_core.R.string.connection_lost), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Enumeration are using for marking reason on loading messages
     */
    private enum UpdateMessagesReason {

        /**
         * Loader updates data. Used to reset last update reason to unspecified mode
         * to prevent scrolling to bottom or setting some position in list
         */
        NONE,

        /**
         * Messages are loading after activity recreating
         */
        DEFAULT,

        /**
         * User scrolled list till oldest uploaded message
         */
        ON_USER_REQUEST,

        /**
         * After relogin
         */
        AFTER_RELOGIN;
    }
}