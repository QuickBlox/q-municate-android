package com.quickblox.q_municate.ui.chats.emoji;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.quickblox.q_municate.ui.chats.emoji.emojiTypes.Emoji;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.chats.emoji.emojiTypes.EmojiObject;
import com.quickblox.q_municate_core.utils.ConstsCore;

import java.util.Arrays;

public class EmojiFragment extends Fragment implements ViewPager.OnPageChangeListener {

    private final int COUNT_OF_EMOJI_TABS = 5;
    private final int EMOJI_TAB_PEOPLE = 0;
    private final int EMOJI_TAB_NATURE = 1;
    private final int EMOJI_TAB_OBJECTS = 2;
    private final int EMOJI_TAB_CARS = 3;
    private final int EMOJI_TAB_PUNCTUATION = 4;

    private int tabLastSelectedIndex = -1;
    private OnEmojiBackspaceClickedListener onEmojiBackspaceClickedListener;
    private View[] emojiTabsView;
    private ViewPager emojisViewPager;
    private EmojisPagerAdapter emojisPageAdapter;
    private ImageView emojisBackspaceImageButton;

    public static void input(EditText editText, EmojiObject emojiObject) {
        if (editText == null || emojiObject == null) {
            return;
        }
        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();
        if (start < ConstsCore.ZERO_INT_VALUE) {
            editText.append(emojiObject.getEmoji());
        } else {
            editText.getText().replace(Math.min(start, end), Math.max(start, end), emojiObject.getEmoji(),
                    ConstsCore.ZERO_INT_VALUE, emojiObject.getEmoji().length());
        }
    }

    public static void backspace(EditText editText) {
        KeyEvent event = new KeyEvent(ConstsCore.ZERO_INT_VALUE, ConstsCore.ZERO_INT_VALUE, ConstsCore.ZERO_INT_VALUE,
                KeyEvent.KEYCODE_DEL, ConstsCore.ZERO_INT_VALUE, ConstsCore.ZERO_INT_VALUE, ConstsCore.ZERO_INT_VALUE,
                ConstsCore.ZERO_INT_VALUE, KeyEvent.KEYCODE_ENDCALL);
        editText.dispatchKeyEvent(event);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (getActivity() instanceof OnEmojiBackspaceClickedListener) {
            onEmojiBackspaceClickedListener = (OnEmojiBackspaceClickedListener) getActivity();
        } else if (getParentFragment() instanceof OnEmojiBackspaceClickedListener) {
            onEmojiBackspaceClickedListener = (OnEmojiBackspaceClickedListener) getParentFragment();
        } else {
            throw new IllegalArgumentException(
                    activity + " must implement interface " + OnEmojiBackspaceClickedListener.class
                            .getSimpleName()
            );
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_emoji, container, false);

        initUI(rootView);
        initEmojisPage(rootView);
        initListeners();

        onPageSelected(EMOJI_TAB_PEOPLE);

        return rootView;
    }

    @Override
    public void onDetach() {
        onEmojiBackspaceClickedListener = null;
        super.onDetach();
    }

    private void initUI(View view) {
        emojisViewPager = (ViewPager) view.findViewById(R.id.emojis_viewpager);
        emojisBackspaceImageButton = (ImageView) view.findViewById(R.id.emojis_backspace_imagebutton);
    }

    private void initListeners() {
        emojisBackspaceImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onEmojiBackspaceClickedListener != null) {
                    onEmojiBackspaceClickedListener.onEmojiBackspaceClicked(view);
                }
            }
        });
    }

    private void initEmojisPage(View view) {
        emojisViewPager.setOnPageChangeListener(this);

        emojisPageAdapter = new EmojisPagerAdapter(getFragmentManager(), Arrays.asList(
                EmojiGridFragment.newInstance(Emoji.DATA_PEOPLE), EmojiGridFragment.newInstance(
                        Emoji.DATA_NATURE), EmojiGridFragment.newInstance(Emoji.DATA_OBJECTS),
                EmojiGridFragment.newInstance(Emoji.DATA_PLACES), EmojiGridFragment.newInstance(
                        Emoji.DATA_SYMBOLS)
        ));
        emojisViewPager.setAdapter(emojisPageAdapter);

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
                    emojisViewPager.setCurrentItem(position);
                }
            });
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        if (tabLastSelectedIndex == position) {
            return;
        }
        switch (position) {
            case EMOJI_TAB_PEOPLE:
            case EMOJI_TAB_NATURE:
            case EMOJI_TAB_OBJECTS:
            case EMOJI_TAB_CARS:
            case EMOJI_TAB_PUNCTUATION:
                if (tabLastSelectedIndex >= ConstsCore.ZERO_INT_VALUE && tabLastSelectedIndex < emojiTabsView.length) {
                    emojiTabsView[tabLastSelectedIndex].setSelected(false);
                }
                emojiTabsView[position].setSelected(true);
                tabLastSelectedIndex = position;
                break;
        }
    }

    @Override
    public void onPageScrollStateChanged(int i) {
    }

    public interface OnEmojiBackspaceClickedListener {

        void onEmojiBackspaceClicked(View view);
    }
}