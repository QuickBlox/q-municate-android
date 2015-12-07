package com.quickblox.q_municate_core.qb.helpers;

import android.app.Activity;
import android.content.Context;

import com.quickblox.chat.QBChatService;
import com.quickblox.core.helper.Lo;
import com.quickblox.q_municate_core.qb.helpers.call.WorkingSessionPull;

import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

public class QBVideoChatHelper extends BaseHelper {

    private final static int ACTIVE_SESSIONS_DEFAULT_SIZE = 5;

    private QBChatService chatService;
    private Class<? extends Activity> activityClass;

    private WorkingSessionPull workingSessionPull = new WorkingSessionPull(ACTIVE_SESSIONS_DEFAULT_SIZE);

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);

    public QBVideoChatHelper(Context context) {
        super(context);
    }

    public void init(QBChatService chatService) {
        Lo.g("init videochat");
        this.chatService = chatService;
    }

    private boolean isExistSameSession(String sessionId){
        WorkingSessionPull.WorkingSession session = workingSessionPull.getSession(sessionId);
        return (session != null );
    }

    private class ClearSessionTask extends TimerTask {

        private String sessionId;
        private int opponentId;

        ClearSessionTask(String sessionId, int opponentId) {
            this.sessionId = sessionId;
            this.opponentId = opponentId;
        }

        @Override
        public void run() {
            WorkingSessionPull.WorkingSession workingSession = workingSessionPull.removeSession(sessionId);
        }
    }

    private class CallSession implements WorkingSessionPull.WorkingSession {

        private AtomicBoolean status;

        CallSession(String session) {
            status = new AtomicBoolean(true);
        }

        @Override
        public boolean isActive() {
            return status.get();
        }

        public void setStatus(boolean status) {
            this.status.set(status);
        }

        @Override
        public void cancel() {
            status.set(false);
        }
    }
}