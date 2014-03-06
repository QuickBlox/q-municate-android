package com.quickblox.qmunicate.ui.voicecall;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.ui.base.BaseActivity;

public class VoiceCallActivity extends BaseActivity {
    public static void start(Context context) {
        Intent intent = new Intent(context, VoiceCallActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_call);
        initUI();
    }

    private void initUI() {
        actionBar.hide();
    }
}