package com.quickblox.qmunicate.ui.chats;

import android.app.ActionBar;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.core.receiver.BroadcastActions;
import com.quickblox.qmunicate.model.ChatMessage;
import com.quickblox.qmunicate.model.SerializableKeys;
import com.quickblox.qmunicate.ui.chats.animation.HeightAnimator;
import com.quickblox.qmunicate.ui.chats.smiles.SmilesTabFragmentAdapter;
import com.quickblox.qmunicate.utils.SizeUtility;
import com.quickblox.qmunicate.ui.views.indicator.IconPageIndicator;
import com.quickblox.qmunicate.ui.views.smiles.ChatEditText;
import com.quickblox.qmunicate.ui.views.smiles.SmileClickListener;
import com.quickblox.qmunicate.ui.views.smiles.SmileysConvertor;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GroupChatActivity extends FragmentActivity implements SwitchViewListener {
    private static final float SMILES_SIZE_IN_DIPS = 220;
    private ChatEditText chatEdit;
    private ListView messagesListView;
    private List<ChatMessage> messagesArrayList;
    private ChatMessagesAdapter messagesAdapter;
    private ViewPager smilesPager;
    private View smilesLayout;
    private IconPageIndicator smilesPagerIndicator;
    private HeightAnimator smilesAnimator;
    private SmileSelectedBroadcastReceiver smileSelectedBroadcastReceiver;

    public static void start(Context context) {
        Intent intent = new Intent(context, GroupChatActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        initUI();
        initSmileWidgets();

        messagesArrayList = new ArrayList<ChatMessage>();
        messagesAdapter = new ChatMessagesAdapter(this, R.layout.list_item_chat_message, messagesArrayList);
        messagesListView.setAdapter(messagesAdapter);

        IntentFilter filter = new IntentFilter(BroadcastActions.SMILE_SELECTED);
        smileSelectedBroadcastReceiver = new SmileSelectedBroadcastReceiver();
        registerReceiver(smileSelectedBroadcastReceiver, filter);

        initListeners();
        initListView();
    }

    private void initUI() {
        smilesLayout = findViewById(R.id.smiles_layout);
        smilesPagerIndicator = (IconPageIndicator) findViewById(R.id.smiles_pager_indicator);
        smilesPager = (ViewPager) findViewById(R.id.smiles_pager);
        messagesListView = (ListView) findViewById(R.id.messagesListView);
        chatEdit = (ChatEditText) findViewById(R.id.messageEdit);
        actionBarSetup();
    }

    private void actionBarSetup() {
        ActionBar ab = getActionBar();
        ab.setTitle("Name of Chat");
        ab.setSubtitle("some information");
    }

    private void initListeners() {
        registerForContextMenu(messagesListView);
    }

    private void initListView() {
        // TODO SF temp list.
        messagesArrayList.add(new ChatMessage("", new Date(), true));
        messagesArrayList.add(new ChatMessage("", new Date(), true));
        messagesArrayList.add(new ChatMessage("", new Date(), true));
        messagesArrayList.add(new ChatMessage("", new Date(), true));
        messagesArrayList.add(new ChatMessage("", new Date(), true));
        messagesArrayList.add(new ChatMessage("", new Date(), true));
        messagesArrayList.add(new ChatMessage("", new Date(), true));
        messagesArrayList.add(new ChatMessage("", new Date(), true));
        messagesArrayList.add(new ChatMessage("", new Date(), true));
        updateFriendListAdapter();
    }

    private void updateFriendListAdapter() {
        messagesAdapter.notifyDataSetChanged();
    }

    public void initSmileWidgets() {
        chatEdit.setSmileClickListener(new OnSmileClickListener());
        chatEdit.setSwitchViewListener(this);
        FragmentStatePagerAdapter adapter = new SmilesTabFragmentAdapter(getSupportFragmentManager());
        smilesPager.setAdapter(adapter);
        smilesPagerIndicator.setViewPager(smilesPager);
        smilesAnimator = new HeightAnimator(chatEdit, smilesLayout);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.group_chat_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                return true;
            case R.id.action_group_details:
                GroupChatDetailsActivity.start(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater m = getMenuInflater();
        m.inflate(R.menu.group_chat_ctx_menu, menu);
    }

    @Override
    public void showLastListItem() {
        if (isSmilesLayoutShowing()) {
            hideView(smilesLayout);
            chatEdit.switchSmileIcon();
        }
    }

    private boolean isSmilesLayoutShowing() {
        return smilesLayout.getHeight() != 0;
    }

    private void hideView(View view) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
        params.height = 0;
        view.setLayoutParams(params);
    }

    private int getSmileLayoutSizeInPixels() {
        return SizeUtility.dipToPixels(this, SMILES_SIZE_IN_DIPS);
    }

    private class SmileSelectedBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            int resourceId = intent.getIntExtra(SerializableKeys.SMILE_ID, R.drawable.smile);
            int cursorPosition = chatEdit.getSelectionStart();

            String roundTrip = "";
            byte[] bytes = SmileysConvertor.getSymbolByResourceId(resourceId).getBytes(Charset.forName("UTF-8"));
            roundTrip = new String(bytes, Charset.forName("UTF-8"));
            String original = new String("A" + "\u00ea" + "\ue106" + "\u00f1" + "\u00fc" + "C");
            chatEdit.getText().insert(cursorPosition, roundTrip);
        }
    }

    private class OnSmileClickListener implements SmileClickListener {

        @Override
        public void onSmileClick() {
            int smilesLayoutHeight = getSmileLayoutSizeInPixels();
            if (isSmilesLayoutShowing()) {
                smilesAnimator.animateHeightFrom(smilesLayoutHeight, 0);
            } else {
                smilesAnimator.animateHeightFrom(0, smilesLayoutHeight);
            }
        }
    }
}