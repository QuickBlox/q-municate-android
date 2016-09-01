package com.quickblox.q_municate.utils.helpers;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;

public class SystemPermissionHelper {

    public static final int PERMISSIONS_REQUEST = 15;

    private final Activity activity;

    public SystemPermissionHelper(Activity activity) {
        this.activity = activity;
    }


    public boolean isPermissionDanied(String permission) {
        return ContextCompat.checkSelfPermission(activity.getApplicationContext(), permission) == PackageManager.PERMISSION_DENIED;
    }

    public void requestPermission(String permission){
        ActivityCompat.requestPermissions(activity, new String[]{permission},
                PERMISSIONS_REQUEST);
    }

    public void requestPermissions(String... permissions){
        ActivityCompat.requestPermissions(activity, permissions,
                PERMISSIONS_REQUEST);
    }

    private boolean hasAllPermissionsGranted(@NonNull int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

    public boolean isAllPermissionGranted(String... permissions) {
        for (String permission : permissions) {
            if (isPermissionDanied(permission)) {
                return false;
            }
        }

        return true;
    }

    public void checkAndRequestPermissions(String... permissions){
        if (collectDaniedPermissions(permissions).length > 0) {
            requestPermissions(collectDaniedPermissions(permissions));
        }
    }

    private String [] collectDaniedPermissions(String... permissions){
        String [] daniedPermissions = new String[permissions.length];

        for (String permission : permissions) {
            if (isPermissionDanied(permission)) {
                daniedPermissions[daniedPermissions.length - 1] = permission;
            }
        }

        return daniedPermissions;
    }
}
