package com.modastadoc.doctors.common.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.LayoutRes;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.modastadoc.doctors.R;
import com.modastadoc.doctors.common.interfaces.IDialogListener;
import com.modastadoc.doctors.common.interfaces.ISelectionListener;
import com.modastadoc.doctors.widget.CustomProgressDialog;

import org.w3c.dom.Text;

/**
 * Created by kunasi on 17/08/17.
 */

public class AppDialogUtil {

    private static CustomProgressDialog mCustomDialog;
    private static boolean isDialogVisible;

    /**
     * Show alert dialog
     *
     * @param activity activity context
     * @param title title to be shown via strings.xml resource
     * @param message message to be shown via strings.xml resource
     */
    public static void showAlert(Activity activity, @StringRes int title, @StringRes int message) {
        showAlert(activity, activity.getString(title), activity.getString(message));
    }

    /**
     * Show alert dialog
     *
     * @param context calling activity context
     * @param title title to be shown via strings.xml resource
     * @param message message to be shown via strings.xml resource
     */
    public static void showAlertForContext(Context context, @StringRes int title, @StringRes int message) {
        showAlert(context, context.getString(title), context.getString(message));
    }

    /**
     * Show alert dialog
     *
     * @param activity activity context
     * @param title title to be shown via strings.xml resource
     * @param message message to be shown
     */
    public static void showAlert(Activity activity, @StringRes int title, String message) {
        showAlert(activity, activity.getString(title), message);
    }

    /**
     * Show alert dialog
     *
     * @param activity activity context
     * @param title title to be shown
     * @param message message to be shown
     */
    public static void showAlert(Activity activity, String title, String message) {
        new AlertDialog.Builder(activity, R.style.AppCompatAlertDialogStyle)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    /**
     * Show alert dialog
     *
     * @param context calling activity context
     * @param title title to be shown
     * @param message message to be shown
     */
    public static void showAlert(Context context, String title, String message) {
        new AlertDialog.Builder(context, R.style.AppCompatAlertDialogStyle)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    public static void showError(Activity activity) {
        showAlert(activity, R.string.error_title, R.string.error_something_wrong_message);
    }

    public static void showInternetError(Activity activity) {
        showAlert(activity, R.string.error_no_network_title, R.string.error_no_network_message);
    }

    /**
     * Show dialog.
     *
     * @param activity activity context.
     * @param layoutID layout ID to be rendered.
     * @param listener tells which option is selected.
     */
    public static void showExtendDialog(Activity activity, @LayoutRes int layoutID, final IDialogListener listener) {

        if(!isDialogVisible) {
            isDialogVisible = true;
            // Create custom dialog object
            final Dialog dialog = new Dialog(activity);
            // Include dialog.xml file
            dialog.setContentView(layoutID);
            dialog.setCancelable(false);
            dialog.show();
            dialog.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onPositiveButtonClicked();
                    dialog.dismiss();
                }
            });
            dialog.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onNegativeButtonClicked();
                    dialog.dismiss();
                }
            });
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    isDialogVisible = false;
                }
            });
        }
    }

    /**
     * Show selection dialog.
     *
     * @param activity activity context.
     * @param options options list to be shown.
     * @param listener tells which option is selected.
     */
    public static void showDialog(Activity activity, String[] options, final ISelectionListener listener) {
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(
                activity,
                android.R.layout.select_dialog_singlechoice);

        for(String option : options)
            adapter.add(option);

        new AlertDialog.Builder(activity)
                .setAdapter(adapter,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                listener.onSelected(which);
                            }
                        })
                .show();
    }

    /**
     * Show dialog
     *
     * @param activity activity context
     * @param title    title to be shown via strings.xml resource
     * @param message  message to be shown via strings.xml resource
     * @param posTitle positive button title to be shown via strings.xml resource
     * @param negTitle negative button title to be shown via strings.xml resource
     * @param icon     icon image to be shown via drawable
     * @param listener callback listener
     */

    /*public static void showDialog(final Activity activity, @StringRes int title, @StringRes int message,
                                  @StringRes int posTitle, @StringRes int negTitle, @DrawableRes int icon,
                                  final IAlertListener listener) {
        showDialog(activity, activity.getString(title), activity.getString(message), activity.getString(posTitle),
                activity.getString(negTitle), icon, listener);
    }*/

    /**
     * Show dialog
     *
     * @param activity activity context
     * @param title    title to be shown via strings.xml resource
     * @param message  message to be shown
     * @param posTitle positive button title to be shown via strings.xml resource
     * @param negTitle negative button title to be shown via strings.xml resource
     * @param listener callback listener
     */

    public static void showDialog(final Activity activity, @StringRes int title, String message,
                                  @StringRes int posTitle, @StringRes int negTitle, final IDialogListener listener) {
        showDialog(activity, activity.getString(title), message, activity.getString(posTitle),
                activity.getString(negTitle), -1, listener);
    }

    private static void showDialog(final Activity activity, String title, String message,
                                   String posTitle, String negTitle, int icon, final IDialogListener listener) {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(activity, R.style.AppCompatAlertDialogStyle);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(posTitle, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.onPositiveButtonClicked();
            }
        });
        builder.setNegativeButton(negTitle, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                listener.onNegativeButtonClicked();
            }
        });
        if(icon != -1)
            builder.setIcon(icon);
        builder.show();
    }

    /**
     * Show settings dialog
     *
     * @param activity activity context
     * @param title title to be shown
     * @param message message to be shown
     */
    /*public static void showSettingsDialog(final Activity activity, String title, String message) {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(activity, R.style.AppCompatAlertDialogStyle);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("SETTINGS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AppCoreUtil.openSettingsScreen(activity);
            }
        });
        builder.setNegativeButton("NOT NOW", null);
        builder.show();
    }*/

    /**
     * Show custom dialog
     *
     * @param activity activity context
     */
    public static void showCustomDialog(Activity activity) {
        mCustomDialog = new CustomProgressDialog(activity);
        mCustomDialog.setCancelable(false);
        mCustomDialog.show();
    }

    /**
     * Show custom dialog
     *
     * @param activity activity context
     * @param cancellable boolean for setCancelable()
     */
    public static void showCustomDialog(Activity activity, boolean cancellable) {
        mCustomDialog = new CustomProgressDialog(activity);
        mCustomDialog.setCancelable(cancellable);
        mCustomDialog.show();
    }

    /**
     * Dismiss custom dialog
     */
    public static void dismissCustomDialog() {
        if(mCustomDialog != null && mCustomDialog.isShowing()) {
            mCustomDialog.stopLoading();
            mCustomDialog.dismiss();
        }
    }

    public static boolean canShowPopup() {
        return mCustomDialog == null || !mCustomDialog.isShowing();
    }
}
