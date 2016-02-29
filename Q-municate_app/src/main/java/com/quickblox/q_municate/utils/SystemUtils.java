package com.quickblox.q_municate.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.quickblox.q_municate.App;

import java.util.List;

public class SystemUtils {

    public static boolean isAppRunningNow() {
        ActivityManager activityManager = (ActivityManager) App.getInstance().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTasks = activityManager.getRunningTasks(Integer.MAX_VALUE);

        if (!runningTasks.isEmpty()) {
            return runningTasks.get(0).topActivity.getPackageName().equalsIgnoreCase(App.getInstance().getPackageName());
        } else {
            return false;
        }
    }

    public static Intent getPreviousIntent(Context context) {
        ActivityManager activityManager = (ActivityManager) App.getInstance().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTasks = activityManager.getRunningTasks(Integer.MAX_VALUE);
        String ourPackageName = App.getInstance().getPackageName();

        if (runningTasks != null) {
            for (ActivityManager.RunningTaskInfo taskInfo : runningTasks) {
                if (taskInfo.topActivity.getPackageName().equalsIgnoreCase(ourPackageName)) {
                    try {
                        Intent intent = new Intent(context, Class.forName(taskInfo.topActivity.getClassName()));
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        return intent;
                    } catch (ClassNotFoundException e) {
                        // This should never happen
                        e.printStackTrace();
                    }
                }
            }
        }

        return null;
    }

    public static int getThreadPoolSize() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return Runtime.getRuntime().availableProcessors();
        } else {
            // According to docs for Runtime.getRuntime().availableProcessors()
            // before Android 4.2 "traditionally this returned the number currently online"
            // There is really small amount of devices that have single core, so we're multiplying returned value on 2
            // And hope a lot of running threads won't burn user's device, he-he
            return Runtime.getRuntime().availableProcessors() * 2;
        }
    }

    public static String getNameActivityOnTopStack(){
        ActivityManager activityManager = (ActivityManager) App.getInstance().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTasks = activityManager.getRunningTasks(Integer.MAX_VALUE);
        String ourPackageName = App.getInstance().getPackageName();
        String topActivityName = null;

        if (runningTasks != null) {
            for (ActivityManager.RunningTaskInfo taskInfo : runningTasks) {
                if (taskInfo.topActivity.getPackageName().equalsIgnoreCase(ourPackageName)) {
                    topActivityName =  taskInfo.topActivity.getClassName();
                }
            }
        }
        return topActivityName;
    }
}