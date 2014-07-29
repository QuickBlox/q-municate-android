package com.quickblox.q_municate.ui.feedback;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.base.BaseFragment;
import com.quickblox.q_municate.utils.EmailUtils;

public class FeedbackFragment extends BaseFragment {

    private RadioGroup feedbackTypesRadioGroup;
    private Button writeEmailButton;

    public static FeedbackFragment newInstance() {
        return new FeedbackFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        title = getString(R.string.nvd_title_feedback);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feedback, container, false);

        initUI(view);
        initListeners();

        return view;
    }

    private void initListeners() {
        writeEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EmailUtils.sendFeedbackEmail(baseActivity, getSelectedFeedbackType());
            }
        });
    }

    private void initUI(View view) {
        setHasOptionsMenu(true);
        feedbackTypesRadioGroup = (RadioGroup) view.findViewById(R.id.feedback_types_radiogroup);
        writeEmailButton = (Button) view.findViewById(R.id.write_email_button);
    }

    private String getSelectedFeedbackType() {
        int radioButtonID = feedbackTypesRadioGroup.getCheckedRadioButtonId();
        RadioButton radioButton = (RadioButton) feedbackTypesRadioGroup.findViewById(radioButtonID);
        return radioButton.getText().toString();
    }
}