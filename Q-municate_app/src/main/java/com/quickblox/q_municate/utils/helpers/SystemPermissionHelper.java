package com.quickblox.q_municate.utils.helpers;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.quickblox.q_municate.R;
import com.quickblox.videochat.webrtc.QBRTCTypes;

import java.util.ArrayList;

public class SystemPermissionHelper {

    public static final int PERMISSIONS_FOR_CALL_REQUEST = 15;
    public static final int PERMISSIONS_FOR_IMPORT_FRIENDS_REQUEST = 16;
    public static final int PERMISSIONS_FOR_SAVE_FILE_REQUEST = 17;
    public static final int PERMISSIONS_FOR_SAVE_FILE_IMAGE_REQUEST = 18;
    public static final int PERMISSIONS_FOR_TAKE_PHOTO_REQUEST = 19;
    public static final int PERMISSIONS_FOR_AUDIO_RECORD_REQUEST = 20;
    public static final int PERMISSIONS_FOR_VIDEO_RECORD_REQUEST = 21;

    private Activity activity;
    private Fragment fragment;

    public SystemPermissionHelper(Activity activity) {
        this.activity = activity;
    }

    public SystemPermissionHelper(Fragment fragment) {
        this.fragment = fragment;
    }

    public void requestPermission(int requestCode, String permission) {
        if (fragment != null){
            fragment.requestPermissions(new String[]{permission}, requestCode);
        } else {
            ActivityCompat.requestPermissions(activity, new String[]{permission}, requestCode);
        }
    }

    public void requestPermissions(int requestCode, String... permissions) {
        if (fragment != null){
            Log.v("Permissions", "request Permissions for fragment " + fragment.getClass().getSimpleName());
            fragment.requestPermissions(permissions, requestCode);
        } else {
            Log.v("Permissions", "request Permissions for activity " + activity.getClass().getSimpleName());
            ActivityCompat.requestPermissions(activity, permissions, requestCode);
        }
    }

    public static void requestPermission(AppCompatActivity activity, int requestId,
                                         String permission, boolean finishActivity) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
            // Display a dialog with rationale.
            Log.d("Permissions", "shouldShowRequestPermissionRationale");
            SystemPermissionHelper.RationaleDialog.newInstance(requestId, finishActivity)
                    .show(activity.getSupportFragmentManager(), "dialog");
        } else {
            // Location permission has not been granted yet, request it.
            ActivityCompat.requestPermissions(activity, new String[]{permission}, requestId);

        }
    }

    public static boolean isPermissionGranted(String[] grantPermissions, int[] grantResults,
                                              String permission) {
        for (int i = 0; i < grantPermissions.length; i++) {
            if (permission.equals(grantPermissions[i])) {
                return grantResults[i] == PackageManager.PERMISSION_GRANTED;
            }
        }
        return false;
    }

    public boolean isPermissionGranted(String permission) {
        if (fragment != null){
            return ContextCompat.checkSelfPermission(fragment.getContext(), permission) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(activity.getApplicationContext(), permission) == PackageManager.PERMISSION_GRANTED;
        }
    }

    public boolean isAllPermissionGranted(String... permissions) {
        for (String permission : permissions) {
            if (!isPermissionGranted(permission)) {
                return false;
            }
        }

        return true;
    }

    public void checkAndRequestPermissions(int requestCode, String... permissions) {
        if (collectDeniedPermissions(permissions).length > 0) {
            requestPermissions(requestCode, collectDeniedPermissions(permissions));
        }
    }

    private String[] collectDeniedPermissions(String... permissions) {
        ArrayList<String> deniedPermissionsList = new ArrayList<>();
        for (String permission : permissions) {
            if (!isPermissionGranted(permission)) {
                deniedPermissionsList.add(permission);
            }
        }

        return deniedPermissionsList.toArray(new String[deniedPermissionsList.size()]);
    }

    public boolean isAllPermissionsGrantedForCallByType(QBRTCTypes.QBConferenceType qbConferenceType) {
        if (QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_AUDIO.equals(qbConferenceType)) {
            return isPermissionGranted(Manifest.permission.RECORD_AUDIO);
        } else {
            return isAllPermissionGranted(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA);
        }
    }

    public boolean isCameraPermissionGranted() {
        return isPermissionGranted(Manifest.permission.CAMERA);
    }

    public boolean isMicrophonePermissionGranted() {
        return isPermissionGranted(Manifest.permission.RECORD_AUDIO);
    }

    public boolean isAllAudioRecordPermissionGranted() {
        return isAllPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO);
    }

    public void requestPermissionsForCallByType(QBRTCTypes.QBConferenceType qbConferenceType) {
        if (QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_AUDIO.equals(qbConferenceType)) {
            checkAndRequestPermissions(PERMISSIONS_FOR_CALL_REQUEST, Manifest.permission.RECORD_AUDIO);
        } else {
            checkAndRequestPermissions(PERMISSIONS_FOR_CALL_REQUEST, Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA);
        }
    }

    public void requestAllPermissionForAudioRecord() {
        checkAndRequestPermissions(PERMISSIONS_FOR_AUDIO_RECORD_REQUEST, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    public void requestPermissionsTakePhoto() {
            checkAndRequestPermissions(PERMISSIONS_FOR_TAKE_PHOTO_REQUEST, Manifest.permission.CAMERA);
    }

    public void requestPermissionsTakeVideo() {
            checkAndRequestPermissions(PERMISSIONS_FOR_VIDEO_RECORD_REQUEST, Manifest.permission.CAMERA);
    }

    public boolean isAllPermissionsGrantedForImportFriends() {
        return isAllPermissionGranted(Manifest.permission.READ_CONTACTS);
    }

    public void requestPermissionsForImportFriends() {
        checkAndRequestPermissions(PERMISSIONS_FOR_IMPORT_FRIENDS_REQUEST, Manifest.permission.READ_CONTACTS);
    }

    public boolean isAllPermissionsGrantedForSaveFile() {
        return isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    public boolean isAllPermissionsGrantedForSaveFileImage() {
        return isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    public void requestPermissionsForSaveFile() {
        checkAndRequestPermissions(PERMISSIONS_FOR_SAVE_FILE_REQUEST, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    public void requestPermissionsForSaveFileImage() {
        checkAndRequestPermissions(PERMISSIONS_FOR_SAVE_FILE_IMAGE_REQUEST, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    public ArrayList<String> collectDeniedPermissionsFomResult(String permissions[], int[] grantResults) {
        ArrayList<String> deniedPermissions = new ArrayList<>();
        for (int i = 0; i < permissions.length; i++) {
            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                deniedPermissions.add(permissions[i]);
            }
        }

        return deniedPermissions;
    }

    public static void openSystemSettings(Context context) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
        intent.setData(uri);
        context.startActivity(intent);
    }


    public static class RationaleDialog extends DialogFragment {

        private static final String ARGUMENT_PERMISSION_REQUEST_CODE = "requestCode";

        private static final String ARGUMENT_FINISH_ACTIVITY = "finish";

        private boolean finishActivity = false;

        public static RationaleDialog newInstance(int requestCode, boolean finishActivity) {
            Bundle arguments = new Bundle();
            arguments.putInt(ARGUMENT_PERMISSION_REQUEST_CODE, requestCode);
            arguments.putBoolean(ARGUMENT_FINISH_ACTIVITY, finishActivity);
            RationaleDialog dialog = new RationaleDialog();
            dialog.setArguments(arguments);
            return dialog;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Bundle arguments = getArguments();
            final int requestCode = arguments.getInt(ARGUMENT_PERMISSION_REQUEST_CODE);
            finishActivity = arguments.getBoolean(ARGUMENT_FINISH_ACTIVITY);

            return new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.permission_rationale_location)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // After click on Ok, request the permission.
                            ActivityCompat.requestPermissions(getActivity(),
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    requestCode);
                            // Do not finish the Activity while requesting permission.
                            finishActivity = false;
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            super.onDismiss(dialog);
            if (finishActivity) {
                Toast.makeText(getActivity(),
                        R.string.permission_required_toast,
                        Toast.LENGTH_SHORT)
                        .show();
                getActivity().finish();
            }
        }
    }
}
