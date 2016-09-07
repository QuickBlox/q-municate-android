package com.quickblox.q_municate_core.utils.helpers;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.quickblox.videochat.webrtc.QBRTCTypes;

import java.util.ArrayList;

public class SystemPermissionHelper {

    public static final int PERMISSIONS_FOR_CALL_REQUEST = 15;
    public static final int PERMISSIONS_FOR_IMPORT_FRIENDS_REQUEST = 16;
    public static final int PERMISSIONS_FOR_SAVE_FILE_REQUEST = 17;

    private final Activity activity;
    private ArrayList<RequestPermissionsResultListener> requestPermissionsResultListeners = new ArrayList<>();

    public SystemPermissionHelper(Activity activity) {
        this.activity = activity;
    }


    public boolean isPermissionDenied(String permission) {
        return ContextCompat.checkSelfPermission(activity.getApplicationContext(), permission) == PackageManager.PERMISSION_DENIED;
    }

    public void requestPermission(int requestCode, String permission) {
        ActivityCompat.requestPermissions(activity, new String[]{permission},
                requestCode);
    }

    public void requestPermissions(int requestCode, String... permissions) {
        ActivityCompat.requestPermissions(activity, permissions,
                requestCode);
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
            if (isPermissionDenied(permission)) {
                return false;
            }
        }

        return true;
    }

    public void checkAndRequestPermissions(int requestCode, String... permissions) {
        if (collectDaniedPermissions(permissions).length > 0) {
            requestPermissions(requestCode, collectDaniedPermissions(permissions));
        }
    }

    private String[] collectDaniedPermissions(String... permissions) {
        ArrayList<String> daniedPermissionsList = new ArrayList<>();
        for (String permission : permissions) {
            if (isPermissionDenied(permission)) {
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
            checkAndRequestPermissions(PERMISSIONS_FOR_CALL_REQUEST, Manifest.permission.RECORD_AUDIO);
        } else {
            checkAndRequestPermissions(PERMISSIONS_FOR_CALL_REQUEST, Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA);
        }
    }

    public void requestPermissionsForImportFriends(){
        checkAndRequestPermissions(PERMISSIONS_FOR_IMPORT_FRIENDS_REQUEST, Manifest.permission.READ_CONTACTS);
    }

    public boolean isAllPerrmissionsGrantedForSaveFile(){
        return isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    public void requestPermissionsForSaveFile(){
        checkAndRequestPermissions(PERMISSIONS_FOR_SAVE_FILE_REQUEST, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    public void addRequestPermissionsResultListener(RequestPermissionsResultListener permissionsResultListener){
        if (permissionsResultListener != null){
            requestPermissionsResultListeners.add(permissionsResultListener);
        }
    }

    public void openAppPermissionsSettings(){
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
        intent.setData(uri);
        activity.getApplicationContext().startActivity(intent);
    }

    public void removeRequestPermissionsResultListener(RequestPermissionsResultListener permissionsResultListener){
        requestPermissionsResultListeners.remove(permissionsResultListener);
    }

    private ArrayList<RequestPermissionsResultListener> getRequestPermissionsResultListeners(){
        return requestPermissionsResultListeners;
    }

    public void notifyRequestPermissionsResultListeners (int requestCode, String permissions[], int[] grantResults){
        for (RequestPermissionsResultListener listener : requestPermissionsResultListeners){
            listener.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public interface RequestPermissionsResultListener{
        void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults);
    }
}
