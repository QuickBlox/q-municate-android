package com.quickblox.qmunicate.ui.chats;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.model.SerializableKeys;
import com.quickblox.qmunicate.service.QBServiceConsts;
import com.quickblox.qmunicate.ui.base.BaseFragmentActivity;
import com.quickblox.qmunicate.ui.chats.animation.HeightAnimator;
import com.quickblox.qmunicate.ui.chats.smiles.SmilesTabFragmentAdapter;
import com.quickblox.qmunicate.ui.views.indicator.IconPageIndicator;
import com.quickblox.qmunicate.ui.views.smiles.ChatEditText;
import com.quickblox.qmunicate.ui.views.smiles.SmileClickListener;
import com.quickblox.qmunicate.ui.views.smiles.SmileysConvertor;
import com.quickblox.qmunicate.utils.Consts;
import com.quickblox.qmunicate.utils.SizeUtility;

import java.nio.charset.Charset;

public class BaseChatActivity extends BaseFragmentActivity implements SwitchViewListener {

    protected static final float SMILES_SIZE_IN_DIPS = 220;

    protected ChatEditText chatEditText;
    protected ListView messagesListView;

    protected ViewPager smilesViewPager;
    protected View smilesLayout;
    protected IconPageIndicator smilesPagerIndicator;
    protected HeightAnimator smilesAnimator;
    protected SmileSelectedBroadcastReceiver smileSelectedBroadcastReceiver;
    protected int layoutResID;

    public BaseChatActivity(int layoutResID) {
        this.layoutResID = layoutResID;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(layoutResID);

        initUI();
        initSmileWidgets();

        IntentFilter filter = new IntentFilter(QBServiceConsts.SMILE_SELECTED);
        smileSelectedBroadcastReceiver = new SmileSelectedBroadcastReceiver();
        registerReceiver(smileSelectedBroadcastReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(smileSelectedBroadcastReceiver);
    }

    private void initUI() {
        smilesLayout = findViewById(R.id.smiles_linearlayout);
        smilesPagerIndicator = (IconPageIndicator) findViewById(R.id.smiles_pager_indicator);
        smilesViewPager = (ViewPager) findViewById(R.id.smiles_viewpager);
        chatEditText = (ChatEditText) findViewById(R.id.message_edittext);
        messagesListView = (ListView) findViewById(R.id.messages_listview);
    }

    public void initSmileWidgets() {
        chatEditText.setSmileClickListener(new OnSmileClickListener());
        chatEditText.setSwitchViewListener(this);
        FragmentStatePagerAdapter adapter = new SmilesTabFragmentAdapter(getSupportFragmentManager());
        smilesViewPager.setAdapter(adapter);
        smilesPagerIndicator.setViewPager(smilesViewPager);
        smilesAnimator = new HeightAnimator(chatEditText, smilesLayout);
    }

    @Override
    public void showLastListItem() {
        if (isSmilesLayoutShowing()) {
            hideView(smilesLayout);
            chatEditText.switchSmileIcon();
        }
    }

    private boolean isSmilesLayoutShowing() {
        return smilesLayout.getHeight() != Consts.ZERO_VALUE;
    }

    private void hideView(View view) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
        params.height = Consts.ZERO_VALUE;
        view.setLayoutParams(params);
    }

    private int getSmileLayoutSizeInPixels() {
        return SizeUtility.dipToPixels(this, SMILES_SIZE_IN_DIPS);
    }

    private class SmileSelectedBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            int resourceId = intent.getIntExtra(SerializableKeys.SMILE_ID, R.drawable.smile);
            int cursorPosition = chatEditText.getSelectionStart();

            String roundTrip;
            byte[] bytes = SmileysConvertor.getSymbolByResourceId(resourceId).getBytes(Charset.forName(Consts.ENCODING_UTF8));
            roundTrip = new String(bytes, Charset.forName(Consts.ENCODING_UTF8));
            chatEditText.getText().insert(cursorPosition, roundTrip);
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