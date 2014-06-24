package com.quickblox.qmunicate.ui.feedback;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.ui.base.BaseFragment;

public class FeedbackFragment extends BaseFragment {

    private RadioGroup feedbackTypesRadioGroup;

    public static FeedbackFragment newInstance() {
        return new FeedbackFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        title = getString(R.string.nvd_title_feedback_type);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feedback, container, false);

        initUI(view);
        initListeners();

        return view;
    }

    private void initListeners() {

    }

    private void initUI(View view) {
        setHasOptionsMenu(true);
        feedbackTypesRadioGroup = (RadioGroup) view.findViewById(R.id.feedback_types_radiogroup);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.feedback_type_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_next_step:
                startFeedbackDetailsActivity();
                break;
        }
        return true;
    }

    private void startFeedbackDetailsActivity() {
        FeedbackDetailsActivity.start(baseActivity, getSelectedFeedbackType());
    }

    private String getSelectedFeedbackType() {
        int radioButtonID = feedbackTypesRadioGroup.getCheckedRadioButtonId();
        RadioButton radioButton = (RadioButton) feedbackTypesRadioGroup.findViewById(radioButtonID);
        return radioButton.getText().toString();
    }
}