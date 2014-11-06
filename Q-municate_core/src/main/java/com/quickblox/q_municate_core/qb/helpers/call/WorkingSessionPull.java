package com.quickblox.q_municate_core.qb.helpers.call;

import java.util.HashMap;
import java.util.Map;

public class WorkingSessionPull {

    private static final String TAG = WorkingSessionPull.class.getSimpleName();
    private Map<String, WorkingSession> activeSessions;
    private int defaultSize;

    public WorkingSessionPull(int defaultSize){
        activeSessions = new HashMap<String, WorkingSession>(
                defaultSize);
    }

    public WorkingSession getSession(String sessionId){
        synchronized (activeSessions) {
            return activeSessions.get(sessionId);
        }
    }

    public WorkingSession addSession(WorkingSession workingSession,String sessionId){
        synchronized (activeSessions) {
            return activeSessions.put(sessionId, workingSession);
        }
    }

    public WorkingSession removeSession(String sessionId){
        synchronized (activeSessions) {
            return activeSessions.remove(sessionId);
        }
    }

    public boolean isEmpty(){
        synchronized (activeSessions){
            return activeSessions.isEmpty();
        }
    }

    public boolean existActive() {
        for (Map.Entry<String, WorkingSession> entry:activeSessions.entrySet()) {
            if (entry.getValue().isActive()) {
                return true;
            }
        }
        return false;
    }

    public interface WorkingSession{
        public boolean isActive();
        public void cancel();
    }
}
