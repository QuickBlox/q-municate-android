package com.quickblox.q_municate_core.utils.helpers;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.quickblox.videochat.webrtc.QBRTCTypes;

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

    public void requestPermission(String permission) {
        ActivityCompat.requestPermissions(activity, new String[]{permission},
                PERMISSIONS_REQUEST);
    }

    public void requestPermissions(String... permissions) {
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

    public boolean isPermissionGranted(String permission) {
        return ContextCompat.checkSelfPermission(activity.getApplicationContext(), permission) == PackageManager.PERMISSION_GRANTED;
    }

    public boolean isAllPermissionGranted(String... permissions) {
        for (String permission : permissions) {
            if (isPermissionDanied(permission)) {
                return false;
            }
        }

        return true;
    }

    public void checkAndRequestPermissions(String... permissions) {
        if (collectDaniedPermissions(permissions).length > 0) {
            requestPermissions(collectDaniedPermissions(permissions));
        }
    }

    private String[] collectDaniedPermissions(String... permissions) {
        ArrayList<String> daniedPermissionsList = new ArrayList<>();
        for (String permission : permissions) {
            if (isPermissionDanied(permission)) {
                daniedPermissionsList.add(permission);
            }
        }

        return daniedPermissionsList.toArray(new String[daniedPermissionsList.size()]);
    }

    public boolean isAllPermissionsGrantedForCallByType(QBRTCTypes.QBConferenceType qbConferenceType) {
        if (QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_AUDIO.equals(qbConferenceType)) {
            return isPermissionGranted(Manifest.permission.RECORD_AUDIO);
        } else {
            return isAllPermissionGranted(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA);
        }
    }

    public boolean isAllPermissionsGrantedForImportFriends(){
        return isAllPermissionGranted(Manifest.permission.READ_CONTACTS);
    }

    public void requestPermissionsForCallByType(QBRTCTypes.QBConferenceType qbConferenceType){
        if (QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_AUDIO.equals(qbConferenceType)) {
            checkAndRequestPermissions(Manifest.permission.RECORD_AUDIO);
        } else {
            checkAndRequestPermissions(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA);
        }
    }

    public void requestPermissionsForImportFriends(){
        checkAndRequestPermissions(Manifest.permission.READ_CONTACTS);
    }
}
