package com.quickblox.q_municate.ui.chats.emoji;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;
//import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.chats.emoji.emojiTypes.Emoji;
import com.quickblox.q_municate.ui.chats.emoji.emojiTypes.EmojiObject;
import com.quickblox.q_municate.utils.Consts;

import java.util.Arrays;
import java.util.List;

public class EmojiContainer extends LinearLayout implements ViewPager.OnPageChangeListener  {

    private final int COUNT_OF_EMOJI_TABS = 5;
    private final int EMOJI_TAB_PEOPLE = 0;
    private final int EMOJI_TAB_NATURE = 1;
    private final int EMOJI_TAB_OBJECTS = 2;
    private final int EMOJI_TAB_CARS = 3;
    private final int EMOJI_TAB_PUNCTUATION = 4;

    private OnEmojiBackspaceClickedListener mOnEmojiconBackspaceClickedListener;

    // keeps track of the last selected tab
    private int mEmojiTabLastSelectedIndex = -1;

    // Views
    private View[] emojiTabsView;

    public EmojiContainer(Context context) {
        super(context);
        setup(context);
    }

    public EmojiContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup(context);
    }

    public EmojiContainer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setup(context);
    }

/* SETUP */

    private void setup(Context context){

        // initialization, permitted in edit mode
        setOrientation(VERTICAL);
        LayoutInflater inflater = LayoutInflater.from(context);

        // inflate and find views
        View view = inflater.inflate(R.layout.fragment_emoji, this, true);
        final ViewPager emojisPager = (ViewPager) view.findViewById(R.id.emojis_viewpager);
        emojisPager.setOnPageChangeListener(this);

        // more initialization
        Activity activity = (Activity) context;
        FragmentManager fragmentManager = activity.getFragmentManager();
        if (!(activity instanceof OnEmojiBackspaceClickedListener)) {
            throw new IllegalAccessError("Activity must implement OnEmojiconBackspaceClickedListener");
        }

        mOnEmojiconBackspaceClickedListener = (OnEmojiBackspaceClickedListener) activity;

        // tabs in the ViewPager
//        EmojisPagerAdapter emojisAdapter = new EmojisPagerAdapter(fragmentManager, Arrays.asList(
//                EmojiGridFragment.newInstance(Emoji.DATA_PEOPLE),
//                EmojiGridFragment.newInstance(Emoji.DATA_NATURE),
//                EmojiGridFragment.newInstance(Emoji.DATA_OBJECTS),
//                EmojiGridFragment.newInstance(Emoji.DATA_PLACES),
//                EmojiGridFragment.newInstance(Emoji.DATA_SYMBOLS)
//        ));
//        emojisPager.setAdapter(emojisAdapter);

        emojiTabsView = new View[COUNT_OF_EMOJI_TABS];
        emojiTabsView[EMOJI_TAB_PEOPLE] = view.findViewById(R.id.emoji_tab_people_imagebutton);
        emojiTabsView[EMOJI_TAB_NATURE] = view.findViewById(R.id.emoji_tab_nature_imagebutton);
        emojiTabsView[EMOJI_TAB_OBJECTS] = view.findViewById(R.id.emoji_tab_objects_imagebutton);
        emojiTabsView[EMOJI_TAB_CARS] = view.findViewById(R.id.emoji_tab_cars_imagebutton);
        emojiTabsView[EMOJI_TAB_PUNCTUATION] = view.findViewById(R.id.emoji_tab_punctuation_imagebutton);

        for (int i = 0; i < emojiTabsView.length; i++) {
            final int position = i;
            emojiTabsView[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    emojisPager.setCurrentItem(position);
                }
            });
        }

        view.findViewById(R.id.emojis_backspace_imagebutton).setOnTouchListener(
                new RepeatListener(1000, 50, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mOnEmojiconBackspaceClickedListener != null) {
                            mOnEmojiconBackspaceClickedListener.onEmojiBackspaceClicked(v);
                        }
                    }
                }));

        // initial callback
        onPageSelected(0);

    }

    public static void input(EditText editText, EmojiObject emojiObject) {
        if (editText == null || emojiObject == null) {
            return;
        }
        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();
        if (start < Consts.ZERO_INT_VALUE) {
            editText.append(emojiObject.getEmoji());
        } else {
            editText.getText().replace(Math.min(start, end), Math.max(start, end), emojiObject.getEmoji(),
                    Consts.ZERO_INT_VALUE, emojiObject.getEmoji().length());
        }
    }

    public static void backspace(EditText editText) {
        KeyEvent event = new KeyEvent(
                0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
        editText.dispatchKeyEvent(event);
    }

    @Override
    public void onPageScrolled(int i, float v, int i2) {
    }

    @Override
    public void onPageSelected(int i) {

        if (mEmojiTabLastSelectedIndex == i) {
            return;
        }
        switch (i) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
                if (mEmojiTabLastSelectedIndex >= 0 && mEmojiTabLastSelectedIndex < emojiTabsView.length) {
                    emojiTabsView[mEmojiTabLastSelectedIndex].setSelected(false);
                }
                emojiTabsView[i].setSelected(true);
                mEmojiTabLastSelectedIndex = i;
                break;
        }
    }

    @Override
    public void onPageScrollStateChanged(int i) {
    }

//    private static class EmojisPagerAdapter extends FragmentStatePagerAdapter {
//
//        private List<EmojiGridFragment> fragmentList;
//
//        public EmojisPagerAdapter(FragmentManager fm, List<EmojiGridFragment> fragmentList) {
//            super(fm);
//            this.fragmentList = fragmentList;
//        }
//
//        @Override
//        public Fragment getItem(int i) {
//            return fragmentList.get(i);
//        }
//
//        @Override
//        public int getCount() {
//            return fragmentList.size();
//        }
//    }

    public static class RepeatListener implements View.OnTouchListener {

        private Handler handler = new Handler();

        private int initialInterval;
        private final int normalInterval;
        private final View.OnClickListener clickListener;

        private Runnable handlerRunnable = new Runnable() {
            @Override
            public void run() {
                if (downView == null) {
                    return;
                }
                handler.removeCallbacksAndMessages(downView);
                handler.postAtTime(this, downView, SystemClock.uptimeMillis() + normalInterval);
                clickListener.onClick(downView);
            }
        };

        private View downView;

        public RepeatListener(int initialInterval, int normalInterval,
                View.OnClickListener clickListener) {
            if (clickListener == null)
                throw new IllegalArgumentException("null runnable");
            if (initialInterval < 0 || normalInterval < 0)
                throw new IllegalArgumentException("negative interval");

            this.initialInterval = initialInterval;
            this.normalInterval = normalInterval;
            this.clickListener = clickListener;
        }

        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    downView = view;
                    handler.removeCallbacks(handlerRunnable);
                    handler.postAtTime(
                            handlerRunnable, downView, SystemClock.uptimeMillis() + initialInterval);
                    clickListener.onClick(view);
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_OUTSIDE:
                    handler.removeCallbacksAndMessages(downView);
                    downView = null;
                    return true;
            }
            return false;
        }
    }

    public interface OnEmojiBackspaceClickedListener {

        void onEmojiBackspaceClicked(View view);
    }

}