package com.quickblox.q_municate.ui.chats.emoji;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.quickblox.q_municate.ui.chats.emoji.emojiTypes.Emoji;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate.ui.chats.emoji.emojiTypes.EmojiObject;

public class EmojiGridFragment extends Fragment implements AdapterView.OnItemClickListener {

    private OnEmojiconClickedListener mOnEmojiconClickedListener;
    private EmojiObject[] emojiObjects;

    protected static EmojiGridFragment newInstance(EmojiObject[] emojiObjects) {
        EmojiGridFragment emojiGridFragment = new EmojiGridFragment();
        Bundle args = new Bundle();
        args.putSerializable(QBServiceConsts.EXTRA_EMOJIS, emojiObjects);
        emojiGridFragment.setArguments(args);
        return emojiGridFragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnEmojiconClickedListener) {
            mOnEmojiconClickedListener = (OnEmojiconClickedListener) activity;
        } else if (getParentFragment() instanceof OnEmojiconClickedListener) {
            mOnEmojiconClickedListener = (OnEmojiconClickedListener) getParentFragment();
        } else {
            throw new IllegalArgumentException(
                    activity + " must implement interface " + OnEmojiconClickedListener.class
                            .getSimpleName());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.view_emoji_grid, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        GridView gridView = (GridView) view.findViewById(R.id.emojis_gridview);
        emojiObjects = getArguments() == null ? Emoji.DATA_PEOPLE : (EmojiObject[]) getArguments()
                .getSerializable(QBServiceConsts.EXTRA_EMOJIS);
        gridView.setAdapter(new EmojiAdapter(view.getContext(), emojiObjects));
        gridView.setOnItemClickListener(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(QBServiceConsts.EXTRA_EMOJIS, emojiObjects);
    }

    @Override
    public void onDetach() {
        mOnEmojiconClickedListener = null;
        super.onDetach();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mOnEmojiconClickedListener != null) {
            mOnEmojiconClickedListener.onEmojiconClicked((EmojiObject) parent.getItemAtPosition(position));
        }
    }

    public interface OnEmojiconClickedListener {

        void onEmojiconClicked(EmojiObject emojiObject);
    }
}