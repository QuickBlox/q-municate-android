package com.quickblox.q_municate_core.qb.helpers.call;

import android.util.Log;

import com.quickblox.videochat.webrtc.QBRTCSession;

import java.util.LinkedList;
import java.util.List;

/**
 * Created on 4/9/15.
 * @author Bogatov Evgeniy bogatovevgeniy@gmail.com
 *
 * Calls for storing and manage call sessions.
 */

public class SessionManager {

    private List<QBRTCSession> sessionList;
    private String currentSessionId;
    private String lastSessionId;


    public SessionManager() {
        this.sessionList = new LinkedList<QBRTCSession>();
    }

    public synchronized void addSession(QBRTCSession qbrtcSession) {
        Log.d("CALL_INTEGRATION", "SessionManager. addSession");
        sessionList.add(qbrtcSession);
    }

    /**
     * Search first session with this id and stop search or return null if
     * session wasn't found
     *
     * @param sessionID
     * @return
     */
    public synchronized QBRTCSession getSession(String sessionID) {
        QBRTCSession result = null;
        for (QBRTCSession session : sessionList) {
            if (session.getSessionID().equals(sessionID)) {
                result = session;
                break;
            }
        }
        return result;
    }

    public synchronized QBRTCSession getCurrentSession() {
        return getSession(currentSessionId);
    }

    public synchronized String getLastSessionId() {
        return lastSessionId;
    }

    public synchronized void setCurrentSession(QBRTCSession currentSession) {
        String sessionID = currentSession.getSessionID();
        this.currentSessionId = sessionID;
        this.lastSessionId = sessionID;
    }

    public synchronized void removeCurrentSession(){
        this.currentSessionId = null;
    }
}
