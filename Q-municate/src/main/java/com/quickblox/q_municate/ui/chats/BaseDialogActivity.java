package com.quickblox.q_municate.ui.chats;

import android.app.ActionBar;
import android.content.Intent;
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
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.quickblox.internal.core.exception.QBResponseException;
import com.quickblox.module.chat.model.QBDialog;
import com.quickblox.module.content.model.QBFile;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.db.DatabaseManager;
import com.quickblox.q_municate.core.command.Command;
import com.quickblox.q_municate.model.MessageCache;
import com.quickblox.q_municate.qb.commands.QBLoadAttachFileCommand;
import com.quickblox.q_municate.qb.commands.QBLoadDialogMessagesCommand;
import com.quickblox.q_municate.qb.helpers.BaseChatHelper;
import com.quickblox.q_municate.service.QBService;
import com.quickblox.q_municate.service.QBServiceConsts;
import com.quickblox.q_municate.ui.base.BaseCursorAdapter;
import com.quickblox.q_municate.ui.base.BaseFragmentActivity;
import com.quickblox.q_municate.ui.chats.emoji.EmojiFragment;
import com.quickblox.q_municate.ui.chats.emoji.EmojiGridFragment;
import com.quickblox.q_municate.ui.chats.emoji.emojiTypes.EmojiObject;
import com.quickblox.q_municate.ui.uihelper.SimpleTextWatcher;
import com.quickblox.q_municate.utils.Consts;
import com.quickblox.q_municate.utils.ErrorUtils;
import com.quickblox.q_municate.utils.ImageUtils;
import com.quickblox.q_municate.utils.KeyboardUtils;

import java.io.File;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public abstract class BaseDialogActivity extends BaseFragmentActivity implements SwitchViewListener, ScrollMessagesListener, EmojiGridFragment.OnEmojiconClickedListener, EmojiFragment.OnEmojiBackspaceClickedListener {

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
    protected BaseChatHelper chatHelper;

    private int keyboardHeight;
    private View rootView;
    private boolean needToShowSmileLayout;
    private ImageView messageTypingBoxImageView;
    private LoadAttachFileSuccessAction loadAttachFileSuccessAction;
    private LoadDialogMessagesSuccessAction loadDialogMessagesSuccessAction;
    private int chatHelperIdentifier;
    private AnimationDrawable messageTypingAnimationDrawable;

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

        imageUtils = new ImageUtils(this);
        loadAttachFileSuccessAction = new LoadAttachFileSuccessAction();
        loadDialogMessagesSuccessAction = new LoadDialogMessagesSuccessAction();

        initUI();
        initListeners();
        initActionBar();

        addActions();

        isNeedToScrollMessages = true;

        hideSmileLayout();
    }

    @Override
    protected void onPause() {
        super.onPause();
        onUpdateChatDialog();
        hideSmileLayout();
    }

    @Override
    protected void onConnectedToService(QBService service) {
        if (chatHelper == null) {
            chatHelper = (BaseChatHelper) service.getHelper(chatHelperIdentifier);
            try {
                chatHelper.createChatLocally(dialog, generateBundleToInitDialog());
            } catch (QBResponseException e) {
                ErrorUtils.showError(this, e.getMessage());
                finish();
            }
        }
    }

    private void startMessageTypingAnimation() {
        messageTypingAnimationDrawable.start();
    }

    protected void attachButtonOnClick() {
        canPerformLogout.set(false);
        imageUtils.getImage();
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
        if (keyboardHeight != Consts.ZERO_INT_VALUE) {
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
        addAction(QBServiceConsts.LOAD_DIALOG_MESSAGES_FAIL_ACTION, failAction);
        updateBroadcastActionList();
    }

    protected abstract void onUpdateChatDialog();

    protected Cursor getAllDialogMessagesByDialogId() {
        return DatabaseManager.getAllDialogMessagesByDialogId(this, dialogId);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        canPerformLogout.set(true);
        if (isGalleryCalled(requestCode) && resultCode == RESULT_OK) {
            isNeedToScrollMessages = true;
            onFileSelected(data.getData());
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (chatHelper != null) {
            chatHelper.closeChat(dialog, generateBundleToInitDialog());
        }
        removeActions();
    }

    private boolean isGalleryCalled(int requestCode) {
        return ImageUtils.GALLERY_INTENT_CALLED == requestCode;
    }

    private void initActionBar() {
        actionBar.setDisplayOptions(
                ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_USE_LOGO);
        initColorsActionBar();
    }

    private void initColorsActionBar() {
        int actionBarTitleId = Resources.getSystem().getIdentifier("action_bar_title", "id", "android");
        int actionBarSubTitleId = Resources.getSystem().getIdentifier("action_bar_subtitle", "id", "android");
        if (actionBarTitleId > Consts.ZERO_INT_VALUE) {
            TextView title = (TextView) findViewById(actionBarTitleId);
            if (title != null) {
                title.setTextColor(Color.WHITE);
            }
        }
        if (actionBarSubTitleId > Consts.ZERO_INT_VALUE) {
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
    }

    protected abstract void onFileSelected(Uri originalUri);

    protected void startLoadAttachFile(File file) {
        showProgress();
        QBLoadAttachFileCommand.start(this, file);
    }

    protected abstract void onFileLoaded(QBFile file);

    protected void startLoadDialogMessages(QBDialog dialog, long lastDateLoad) {
        QBLoadDialogMessagesCommand.start(this, dialog, lastDateLoad);
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
        messageTypingBoxImageView = (ImageView) findViewById(R.id.message_typing_box_imageview);
        messageTypingAnimationDrawable = (AnimationDrawable) messageTypingBoxImageView.getDrawable();
    }

    private void initListeners() {
        messageEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                hideSmileLayout();
                return false;
            }
        });

        messageEditText.addTextChangedListener(new SimpleTextWatcher() {

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                super.onTextChanged(charSequence, start, before, count);
                if (TextUtils.isEmpty(charSequence) || TextUtils.isEmpty(charSequence.toString().trim())) {
                    sendButton.setEnabled(false);
                } else {
                    sendButton.setEnabled(true);
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
                    if (keyboardHeight == Consts.ZERO_INT_VALUE) {
                        showSmileLayout(Consts.ZERO_INT_VALUE);
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

    private void initKeyboardHeight() {
        final int EXPECTED_HEIGHT = 100;
        Rect r = new Rect();
        rootView.getWindowVisibleDisplayFrame(r);
        int screenHeight = rootView.getRootView().getHeight();
        int heightDifference = screenHeight - (r.bottom - r.top);
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > Consts.ZERO_INT_VALUE) {
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
    public void onScrollToBottom() {
        scrollListView();
    }

    protected void scrollListView() {
        if (isNeedToScrollMessages) {
            isNeedToScrollMessages = false;
            messagesListView.setSelection(messagesAdapter.getCount() - 1);
        }
    }

    protected void startLoadDialogMessages() {
        if (dialog == null) {
            return;
        }

        showProgress();

        MessageCache lastReadMessage = DatabaseManager.getLastReadMessage(this, dialog);
        if (lastReadMessage == null) {
            startLoadDialogMessages(dialog, Consts.ZERO_LONG_VALUE);
        } else {
            long lastMessageDateSent = lastReadMessage.getTime();
            startLoadDialogMessages(dialog, lastMessageDateSent);
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
            hideProgress();
        }
    }
}