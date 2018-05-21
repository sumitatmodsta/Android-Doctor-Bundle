package com.modastadoc.doctors.common.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.StringRes;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.modastadoc.doctors.App;

/**
 * Created by kunasi on 17/08/17.
 */

public class AppCoreUtil {

    private static final String TAG = "LOG";

    /**
     * function to check network availability
     *
     * @return isAvailable
     */
    public static boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) App.getAppContext().getSystemService
                (Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    /**
     * Method to hide keyPad
     *
     * @param activity calling activity in which keypad need to hidden
     */
    public static void hideKeyboard(Activity activity) {
        if (activity != null && activity.getCurrentFocus() != null) {
            InputMethodManager inputMethodManager
                    = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager
                    .hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }

    /**
     * Method to show toast
     *
     * @param activity calling activity in which toast need to show
     * @param message toast message which need to show
     */
    public static void showToast(Activity activity, @StringRes int message) {
        showToast(activity , activity.getString(message));
    }

    /**
     * Method to show toast
     *
     * @param activity calling activity in which toast need to show
     * @param message toast message which need to show
     */
    public static void showToast(Activity activity, String message) {
        Toast.makeText(activity , message ,Toast.LENGTH_LONG).show();
    }

    /**
     * Method to validate string
     *
     * @param str string argument
     * @return isEmpty
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().length() == 0;
    }

    /**
     * Method to validate string
     *
     * @param str string argument
     * @return isEmpty
     */
    public static boolean isNonNull(String str) {
        return str == null || str.trim().length() == 0 || str.equalsIgnoreCase("null");
    }

    /**
     * Method to log values
     *
     * @param title title value
     * @param message response
     */
    public static void log(String title, String message) {
        Log.i(TAG, title + " --> "+message);
    }

    /**
     * Method to log values
     *
     * @param activity activity
     * @param method title value
     * @param message response
     */
    public static void log(String activity, String method, String message) {
        Log.i(TAG, activity + " --> " + method + " --> "+message);
    }

    /**
     * Method to open corresponding settings screen
     *
     * @param activity calling activity from which settings screen need to open
     */
    public static void openSettingsScreen(Activity activity) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
        intent.setData(uri);
        activity.startActivity(intent);
    }
}
