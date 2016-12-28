package com.monsoonblessing.moments;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * <h1>Utility class to help with any permissions</h1>
 * <p>
 * Updated by Kevin on 2016-12-26.
 */
public class PermissionManager {
    // permission codes
    public static final int PERMISSION_READ_EXTERNAL_STORAGE_CODE = 1;
    public static final int PERMISSION_CAMERA_CODE = 2;

    // permission names
    public static final String EXTERNAL_STORAGE_PERMISSION = Manifest.permission.READ_EXTERNAL_STORAGE;
    public static final String CAMERA_PERMISSION = Manifest.permission.CAMERA;


    /**
     * Returns true if we have external storage permission
     *
     * @param context the context
     */
    public static boolean hasExternalStoragePermission(Context context) {
        return hasPermission(context, EXTERNAL_STORAGE_PERMISSION);
    }


    /**
     * Returns true if we have camera permission
     *
     * @param context the context
     */
    public static boolean hasCameraPermission(Context context) {
        return hasPermission(context, CAMERA_PERMISSION);
    }


    /**
     * Returns true if we have external storage permission
     *
     * @param context    the context
     * @param permission the permission we are checking for
     */
    private static boolean hasPermission(Context context, String permission) {
        return (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED);
    }

    /*
    Permission requesters
     */


    /**
     * Requests for external storage permission via a popup
     * If user denied previously, a popup explaining why we need the permission is brought up
     *
     * @param activity the activity
     */
    public static void requestExternalStoragePermission(Activity activity) {
        requestPermission(activity, EXTERNAL_STORAGE_PERMISSION, PERMISSION_READ_EXTERNAL_STORAGE_CODE);
    }


    /**
     * Requests for camera permission via a popup
     * If user denied previously, a popup explaining why we need the permission is brought up
     *
     * @param activity the activity
     */
    public static void requestCameraPermission(Activity activity) {
        requestPermission(activity, CAMERA_PERMISSION, PERMISSION_CAMERA_CODE);
    }


    /**
     * Requests for a permission via a popup
     * If user denied previously, a popup explaining why we need the permission is brought up
     *
     * @param activity       the activity
     * @param permission     the permission we are requesting for
     * @param permissionCode the request code for the permission
     */
    private static void requestPermission(final Activity activity, final String permission, final int permissionCode) {

        // if user denied permission, show them a popup with an explanation before re-prompting permission
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {

            String message = "";

            switch (permission) {
                case EXTERNAL_STORAGE_PERMISSION:
                    message = "External storage permission required to access gallery or " +
                            "store camera images";
                    break;
                case CAMERA_PERMISSION:
                    message = "In order to take a picture with your device via this app, camera " +
                            "permission is required";
                    break;
                // other permissions can have different messages
            }

            new AlertDialog.Builder(activity)
                    .setMessage(message)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            requestPermissionPopup(activity, permission, permissionCode);
                        }
                    })
                    .create()
                    .show();

        } else {
            // prompt user to enable permission
            requestPermissionPopup(activity, permission, permissionCode);
        }
    }


    /**
     * Prompts user to enable permission using android's built in permission popup
     *
     * @param activity       the activity
     * @param permission     the permission we are requesting for
     * @param permissionCode the request code for the permission
     */
    private static void requestPermissionPopup(Activity activity, String permission, int permissionCode) {
        ActivityCompat.requestPermissions(activity,
                new String[]{permission},
                permissionCode);

    }
}
