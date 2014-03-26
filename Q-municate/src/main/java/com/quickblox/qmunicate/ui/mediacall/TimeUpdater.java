package com.quickblox.qmunicate.ui.mediacall;


import android.os.Handler;
import android.widget.TextView;

import com.quickblox.qmunicate.ui.utils.Consts;

public class TimeUpdater implements Runnable {

    private TextView timeTextView;
    private Handler handler;
    private int second;
    private int minute;

    public TimeUpdater(TextView timeTextView, Handler handler) {
        this.timeTextView = timeTextView;
        this.handler = handler;
    }

    @Override
    public void run() {
        second++;
        if (second == 60) {
            second = 0;
            minute++;
        }
        timeTextView.setText(String.format("%02d:%02d", minute, second));
        handler.removeCallbacks(this);
        handler.postDelayed(this, Consts.SECOND);
    }
}
