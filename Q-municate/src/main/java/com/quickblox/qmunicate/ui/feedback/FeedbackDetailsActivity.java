package com.quickblox.qmunicate.ui.feedback;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;

import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.ui.base.BaseLogeableActivity;
import com.quickblox.qmunicate.utils.EmailUtils;

public class FeedbackDetailsActivity extends BaseLogeableActivity {

    public static final String FEEDBACK_TYPE = "feedback_type";

    private String feedbackType;
    private EditText feedbackDescriptionEditText;

    public static void start(Context context, String feedbackType) {
        Intent intent = new Intent(context, FeedbackDetailsActivity.class);
        intent.putExtra(FEEDBACK_TYPE, feedbackType);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback_details);
        canPerformLogout.set(false);
        feedbackType = getIntent().getExtras().getString(FEEDBACK_TYPE);
        initUI();
    }

    private void initUI() {
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        feedbackDescriptionEditText = _findViewById(R.id.feedback_description_edittext);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.feedback_details_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                navigateToParent();
                return true;
            case R.id.action_send_email:
                EmailUtils.sendFeedbackEmail(FeedbackDetailsActivity.this, feedbackType,
                        feedbackDescriptionEditText.getText().toString());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}