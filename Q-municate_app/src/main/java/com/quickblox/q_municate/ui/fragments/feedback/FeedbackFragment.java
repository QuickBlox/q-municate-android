package com.quickblox.q_municate.ui.fragments.feedback;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.fragments.base.BaseFragment;
import com.quickblox.q_municate.utils.helpers.EmailHelper;

import butterknife.Bind;

public class FeedbackFragment extends BaseFragment {

    @Bind(R.id.feedback_types_radiogroup)
    RadioGroup feedbackTypesRadioGroup;

    public static FeedbackFragment newInstance() {
        return new FeedbackFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feedback, container, false);

        activateButterKnife(view);

        return view;
    }

    @Override
    public void initActionBar() {
        super.initActionBar();
        actionBarBridge.setActionBarTitle(R.string.action_bar_feedback);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.done_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_done:
                EmailHelper.sendFeedbackEmail(baseActivity, getSelectedFeedbackType());
                break;
        }
        return true;
    }

    private String getSelectedFeedbackType() {
        int radioButtonID = feedbackTypesRadioGroup.getCheckedRadioButtonId();
        RadioButton radioButton = (RadioButton) feedbackTypesRadioGroup.findViewById(radioButtonID);
        return radioButton.getText().toString();
    }
}