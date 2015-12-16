package com.quickblox.q_municate.ui.activities.feedback;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.activities.base.BaseLoggableActivity;
import com.quickblox.q_municate.utils.helpers.EmailHelper;

import butterknife.Bind;

public class FeedbackActivity extends BaseLoggableActivity {

    @Bind(R.id.feedback_types_radiogroup)
    RadioGroup feedbackTypesRadioGroup;

    public static void start(Context context) {
        Intent intent = new Intent(context, FeedbackActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected int getContentResId() {
        return R.layout.activity_feedback;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initFields();
        setUpActionBarWithUpButton();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.done_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_done:
                EmailHelper.sendFeedbackEmail(this, getSelectedFeedbackType());
                break;
            default:
                super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void initFields() {
        title = getString(R.string.feedback_title);
    }

    private String getSelectedFeedbackType() {
        int radioButtonID = feedbackTypesRadioGroup.getCheckedRadioButtonId();
        RadioButton radioButton = (RadioButton) feedbackTypesRadioGroup.findViewById(radioButtonID);
        return radioButton.getText().toString();
    }
}