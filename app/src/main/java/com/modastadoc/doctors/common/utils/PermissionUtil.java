package com.modastadoc.doctors.common.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.modastadoc.doctors.App;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kunasi on 10/08/17.
 */

public class PermissionUtil {

    /**
     * Check if the calling context has a set of permissions.
     *
     * @param perms   one ore more permissions, such as {@link Manifest.permission#CAMERA}.
     * @return true if all permissions are already granted, false if at least one permission is not
     * yet granted.
     * @see Manifest.permission
     */
    public static boolean hasPermission(@NonNull String... perms) {
        // Always return true for SDK < M, let the system deal with the permissions
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            // DANGER ZONE!!! Changing this will break the library.
            return true;
        }

        for (String perm : perms) {
            if (ContextCompat.checkSelfPermission(App.getAppContext(), perm)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }

    /**
     * Request the permission.
     *
     * @param activity    the calling activity.
     * @param requestCode requestCode argument to permission result callback.
     * @param perms       one ore more permissions, such as {@link Manifest.permission#CAMERA}.
     * @see Manifest.permission
     */
    public static void requestPermission(Activity activity, int requestCode, @NonNull String... perms) {
        ActivityCompat.requestPermissions(activity, perms, requestCode);
    }

    /**
     * Handle the permissions in the given perms argument which has not been given permissions by user.
     *
     * @param perms   one ore more permissions, such as {@link Manifest.permission#CAMERA}.
     * @return String[] necessary permissions which are need to be asked dynamically.
     * @see Manifest.permission
     */
    public static String[] getNecessaryPermissions(@NonNull String... perms) {
        List<String> list = new ArrayList<>();
        for (String perm : perms) {
            if (ContextCompat.checkSelfPermission(App.getAppContext(), perm)
                    != PackageManager.PERMISSION_GRANTED) {
                list.add(perm);
            }
        }

        return list.toArray(new String[list.size()]);
    }

    /**
     * Handle the result of a permission request, should be called from the calling {@link
     * Activity}'s {@link ActivityCompat.OnRequestPermissionsResultCallback#onRequestPermissionsResult(int,
     * String[], int[])} method.
     *
     * @param activity     the calling activity
     * @param requestCode  requestCode argument to permission result callback.
     * @param permissions  permissions argument to permission result callback.
     * @param grantResults grantResults argument to permission result callback.
     * @param callbacks    callbacks argument to implement {@link Callbacks}.
     */
    public static void onRequestPermissionsResult(Activity activity, int requestCode,
                                                  @NonNull String[] permissions,
                                                  @NonNull int[] grantResults,
                                                  Callbacks callbacks) {
        // Make a collection of denied and permanentlyDenied permissions from the request.
        List<String> denied = new ArrayList<>();
        List<String> permanentlyDenied = new ArrayList<>();
        for (int i = 0; i < permissions.length; i++) {
            String perm = permissions[i];
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                if(ActivityCompat.shouldShowRequestPermissionRationale(activity, perm))
                    denied.add(perm);
                else
                    permanentlyDenied.add(perm);
            }
        }

        // Invoke corresponding callback method based on permissions result.
        if(denied.size() > 0)
            callbacks.onPermissionsDenied(requestCode);
        else if(permanentlyDenied.size() > 0)
            callbacks.onPermissionsPermanentlyDenied(requestCode, permanentlyDenied);
        else
            callbacks.onPermissionsGranted(requestCode);
    }

    /**
     * Handle the permissions names.
     *
     * @param permission permission argument, such as {@link Manifest.permission#CAMERA}.
     * @return String    short name of given permission argument.
     */
    public static String getPermissionName(String permission) {
        String name;
        switch (permission) {
            case Manifest.permission.CAMERA:
                name = "Camera";
                break;
            case Manifest.permission.ACCESS_FINE_LOCATION:
            case Manifest.permission.ACCESS_COARSE_LOCATION:
                name = "Location";
                break;
            case Manifest.permission.READ_EXTERNAL_STORAGE:
            case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                name = "Storage";
                break;
            case Manifest.permission.RECORD_AUDIO:
                name = "Microphone";
                break;
            case Manifest.permission.GET_ACCOUNTS:
                name = "Contacts";
                break;
            case Manifest.permission.READ_PHONE_STATE:
                name = "Phone";
                break;
            default:
                name = permission;
                break;
        }

        return name;
    }

    /**
     * Handle the camera permanently denied error message.
     *
     * @param perms   permission argument, such as {@link Manifest.permission#CAMERA}.
     * @return String permanently denied error message.
     */
    public static String getCameraPermanentlyDeniedMessage(@NonNull List<String> perms) {
        String message = "To capture photos & videos, ";
        return getPermanentlyDeniedMessage(message, perms);
    }

    /**
     * Handle the gallery permanently denied error message.
     *
     * @param perms   permission argument, such as {@link Manifest.permission#CAMERA}.
     * @return String permanently denied error message.
     */
    public static String getGalleryPermanentlyDeniedMessage(@NonNull List<String> perms) {
        String message = "To pickup photos, ";
        return getPermanentlyDeniedMessage(message, perms);
    }

    private static String getPermanentlyDeniedMessage(String message, List<String> perms) {
        final int size = perms.size();
        String perm = "";
        for(int i=0; i< size; i++) {
            if(i == (size-1)) {
                perm += getPermissionName(perms.get(i));
            }else {
                perm += getPermissionName(perms.get(i))+", ";
            }
        }

        StringBuilder sb = new StringBuilder(message);
        sb.append("allow Modasta Doc access to your ");
        sb.append(perm);
        sb.append(". ");
        sb.append("Tap Settings > Permissions, and turn ");
        sb.append(perm);
        sb.append(" on.");

        return sb.toString();
    }

    /**
     * Callback interface to receive the results of {@code onRequestPermissionsResult(int, String[], int[])}
     * calls.
     */
    public interface Callbacks {

        void onPermissionsGranted(int requestCode);
        void onPermissionsDenied(int requestCode);
        void onPermissionsPermanentlyDenied(int requestCode, List<String> perms);
    }
}
