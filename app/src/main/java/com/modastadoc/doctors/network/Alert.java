package com.modastadoc.doctors.network;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.LayoutRes;
import android.support.v7.app.AlertDialog;
import android.widget.ArrayAdapter;

import com.modastadoc.doctors.R;

/**
 * Created by vijay.hiremath on 13/10/16.
 */
public class Alert
{
    public static String toastMsg;
    private static Context mContext;
    public AlertListener listener;
    public Alert(Context context){
        mContext    =   context;
    }

    public void showSimpleAlert(String title, String message){
        android.support.v7.app.AlertDialog.Builder builder =
                new android.support.v7.app.AlertDialog.Builder(mContext, R.style.AppCompatAlertDialogStyle);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }

    public void showDialogWithList(String title,String[] arrayOfOptions, @LayoutRes int layout){
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(mContext);
        if(title != null)
            builderSingle.setTitle(title);

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                mContext,
                android.R.layout.select_dialog_singlechoice);
        for(int i=0;i<arrayOfOptions.length;i++){
            arrayAdapter.add(arrayOfOptions[i]);
        }

        builderSingle.setAdapter(
                arrayAdapter,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onDialogOptionClick(which);
                    }
                });
        builderSingle.show();
    }

    public void showSimpleDecision(String title, String message){
        android.support.v7.app.AlertDialog.Builder builder =
                new android.support.v7.app.AlertDialog.Builder(mContext, R.style.AppCompatAlertDialogStyle);
        builder.setTitle(title);
        builder.setMessage(message);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }
}
