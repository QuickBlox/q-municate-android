package com.quickblox.qmunicate.ui.chats.emoji;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.*;
import android.widget.EditText;

import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.ui.chats.emoji.emojiTypes.Emoji;
import com.quickblox.qmunicate.ui.chats.emoji.emojiTypes.EmojiObject;

import java.util.Arrays;
import java.util.List;

public class EmojiFragment extends Fragment implements ViewPager.OnPageChangeListener {

    private OnEmojiconBackspaceClickedListener onEmojiconBackspaceClickedListener;
    private int emojiTabLastSelectedIndex = -1;
    private View[] emojiTabs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_emoji, container, false);
        final ViewPager emojisPager = (ViewPager) view.findViewById(R.id.emojis_viewpager);
        emojisPager.setOnPageChangeListener(this);
        EmojisPagerAdapter emojisAdapter = new EmojisPagerAdapter(getFragmentManager(), Arrays.asList(
                EmojiGridFragment.newInstance(Emoji.DATA_PEOPLE),
                EmojiGridFragment.newInstance(Emoji.DATA_NATURE),
                EmojiGridFragment.newInstance(Emoji.DATA_OBJECTS),
                EmojiGridFragment.newInstance(Emoji.DATA_PLACES),
                EmojiGridFragment.newInstance(Emoji.DATA_SYMBOLS)
        ));
        emojisPager.setAdapter(emojisAdapter);

        emojiTabs = new View[5];
        emojiTabs[0] = view.findViewById(R.id.emoji_tab_people_imagebutton);
        emojiTabs[1] = view.findViewById(R.id.emoji_tab_nature_imagebutton);
        emojiTabs[2] = view.findViewById(R.id.emoji_tab_objects_imagebutton);
        emojiTabs[3] = view.findViewById(R.id.emoji_tab_cars_imagebutton);
        emojiTabs[4] = view.findViewById(R.id.emoji_tab_punctuation_imagebutton);
        for (int i = 0; i < emojiTabs.length; i++) {
            final int position = i;
            emojiTabs[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    emojisPager.setCurrentItem(position);
                }
            });
        }
        view.findViewById(R.id.emojis_backspace_imagebutton).setOnTouchListener(new RepeatListener(1000, 50, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onEmojiconBackspaceClickedListener != null) {
                    onEmojiconBackspaceClickedListener.onEmojiconBackspaceClicked(v);
                }
            }
        }));
        onPageSelected(0);
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (getActivity() instanceof OnEmojiconBackspaceClickedListener) {
            onEmojiconBackspaceClickedListener = (OnEmojiconBackspaceClickedListener) getActivity();
        } else if(getParentFragment() instanceof  OnEmojiconBackspaceClickedListener) {
            onEmojiconBackspaceClickedListener = (OnEmojiconBackspaceClickedListener) getParentFragment();
        } else {
            throw new IllegalArgumentException(activity + " must implement interface " + OnEmojiconBackspaceClickedListener.class.getSimpleName());
        }
    }

    @Override
    public void onDetach() {
        onEmojiconBackspaceClickedListener = null;
        super.onDetach();
    }

    public static void input(EditText editText, EmojiObject emojiObject) {
        if (editText == null || emojiObject == null) {
            return;
        }

        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();
        if (start < 0) {
            editText.append(emojiObject.getEmoji());
        } else {
            editText.getText().replace(Math.min(start, end), Math.max(start, end), emojiObject.getEmoji(), 0, emojiObject.getEmoji().length());
        }
    }

    public static void backspace(EditText editText) {
        KeyEvent event = new KeyEvent(0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
        editText.dispatchKeyEvent(event);
    }

    @Override
    public void onPageScrolled(int i, float v, int i2) {
    }

    @Override
    public void onPageSelected(int i) {
        if (emojiTabLastSelectedIndex == i) {
            return;
        }
        switch (i) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
                if (emojiTabLastSelectedIndex >= 0 && emojiTabLastSelectedIndex < emojiTabs.length) {
                    emojiTabs[emojiTabLastSelectedIndex].setSelected(false);
                }
                emojiTabs[i].setSelected(true);
                emojiTabLastSelectedIndex = i;
                break;
        }
    }

    @Override
    public void onPageScrollStateChanged(int i) {
    }

    private static class EmojisPagerAdapter extends FragmentStatePagerAdapter {
        private List<EmojiGridFragment> fragments;

        public EmojisPagerAdapter(FragmentManager fm, List<EmojiGridFragment> fragments) {
            super(fm);
            this.fragments = fragments;
        }

        @Override
        public Fragment getItem(int i) {
            return fragments.get(i);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
    }

    /**
     * A class, that can be used as a TouchListener on any view (e.g. a Button).
     * It cyclically runs a clickListener, emulating keyboard-like behaviour. First
     * click is fired immediately, next before initialInterval, and subsequent before
     * normalInterval.
     * <p/>
     * <p>Interval is scheduled before the onClick completes, so it has to run fast.
     * If it runs slow, it does not generate skipped onClicks.
     */
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

        /**
         * @param initialInterval The interval before first click event
         * @param normalInterval  The interval before second and subsequent click
         *                        events
         * @param clickListener   The OnClickListener, that will be called
         *                        periodically
         */
        public RepeatListener(int initialInterval, int normalInterval, View.OnClickListener clickListener) {
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
                    handler.postAtTime(handlerRunnable, downView, SystemClock.uptimeMillis() + initialInterval);
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

    public interface OnEmojiconBackspaceClickedListener {
        void onEmojiconBackspaceClicked(View v);
    }
}
