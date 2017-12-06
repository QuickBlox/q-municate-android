package com.quickblox.q_municate.ui.activities.others;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

//workaround to remove activity from recent list, due to finishAndRemoveTask works >= 21
public class ExitActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (android.os.Build.VERSION.SDK_INT >= 21) {
            finishAndRemoveTask();
        } else {
            finish();
        }
//        to disable any transition animation
        overridePendingTransition(0, 0);
    }

    public static void exitApplication(Context context) {
        Intent intent = new Intent(context, ExitActivity.class);

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);

        context.startActivity(intent);
    }
}