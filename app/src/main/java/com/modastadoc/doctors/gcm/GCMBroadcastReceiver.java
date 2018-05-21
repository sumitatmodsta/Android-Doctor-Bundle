package com.modastadoc.doctors.gcm;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

/**
 * Created by vijay.hiremath on 11/10/16.
 */
public class GCMBroadcastReceiver extends WakefulBroadcastReceiver
{
    String TAG = GCMBroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.e( TAG , "-> " + intent.getExtras().toString());
        ComponentName comp = new ComponentName(context.getPackageName(),GCMNotificationIntentService.class.getName());
        startWakefulService(context, (intent.setComponent(comp)));
        setResultCode(Activity.RESULT_OK);
    }

}
