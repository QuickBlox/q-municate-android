package com.quickblox.q_municate_core.qb.helpers.call;

import android.util.Log;

import com.quickblox.videochat.webrtc.QBRTCSession;

import java.util.LinkedList;
import java.util.List;
import java.util.TimerTask;

/**
 * Created on 4/9/15.
 *
 * @author Bogatov Evgeniy bogatovevgeniy@gmail.com
 */

public class SessionManager {

    private List<QBRTCSession> sessionList;


    public SessionManager() {
        this.sessionList =  new LinkedList<QBRTCSession>();
    }

    public void addSession(QBRTCSession qbrtcSession) {
        Log.d("CALL_INTEGRATION", "SessionManager. addSession");
                 sessionList.add(qbrtcSession);
    }

    /**
     * Search first session with this id and stop search or return null if
     * session wasn't found
     * @param sessionID
     * @return
     */

    public QBRTCSession getSession(String sessionID) {
        QBRTCSession result = null;
        for (QBRTCSession session : sessionList){
            if(session.getSessionID().equals(sessionID)){
             result = session;
                Log.d("CALL_INTEGRATION", "SessionManager. Get session successful");
                break;
            }
        }
        return result;
    }

//    private void startClearSessionTask(ConnectionConfig connectionConfig) {
//        ClearSessionTask clearSessionTask = new ClearSessionTask(connectionConfig.getConnectionSession(),
//                connectionConfig.getToUser().getId());
//        scheduler.schedule(clearSessionTask, ConstsCore.DEFAULT_CLEAR_SESSION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
//    }
//
//    private boolean isExistSameSession(String sessionId){
//        WorkingSessionPull.WorkingSession session = workingSessionPull.getSession(sessionId);
//        return (session != null );
//    }


    private class ClearSessionTask extends TimerTask {

        private String sessionId;
        private int opponentId;

        ClearSessionTask(String sessionId, int opponentId) {
            this.sessionId = sessionId;
            this.opponentId = opponentId;
        }

        @Override
        public void run() {
//            WorkingSessionPull.WorkingSession workingSession = workingSessionPull.removeSession(sessionId);
        }
    }

}
