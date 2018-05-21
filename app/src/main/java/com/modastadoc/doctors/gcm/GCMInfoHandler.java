package com.modastadoc.doctors.gcm;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.modastadoc.doctors.common.constants.UserConstants;
import com.modastadoc.doctors.database.LocalDataManager;

import java.io.IOException;

/**
 * Created by vijay.hiremath on 11/10/16.
 */
public class GCMInfoHandler extends IntentService
{
    Context context;
    public static String GCM_REGISTRATION_ID;
    public String REGISTRATION_ID;
    private GoogleCloudMessaging gcm;
    private static final String TAG = GCMInfoHandler.class.getSimpleName();

    public static final String GCM_PROJECT_ID = "331698878706";

    public GCMInfoHandler()
    {
        super(TAG);
    }

    public GCMInfoHandler(Context context)
    {
        super(TAG);
        this.context = context;
    }

    public String registerGCM()
    {
        gcm = GoogleCloudMessaging.getInstance(context);
//        GCM_REGISTRATION_ID = getRegistrationId(context);
        if (TextUtils.isEmpty(GCM_REGISTRATION_ID))
        {
            registerInBackground();
        }
        return GCM_REGISTRATION_ID;
    }

    private void registerInBackground()
    {
        AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>()
        {

            @Override
            protected String doInBackground(Void... params)
            {

                String msg = null;
                try
                {
                    if (gcm == null)
                    {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    REGISTRATION_ID = gcm.register( GCM_PROJECT_ID );

                    Log.e( TAG , "****************" );
                    Log.e( TAG , "GCM token : " + REGISTRATION_ID);
                    LocalDataManager.getInstance().set( UserConstants.GCMTOKEN , REGISTRATION_ID );
                    Log.e(TAG, "****************");

                } catch (IOException ex)
                {
                    msg = "Error :" + ex.getMessage();
                }

                return REGISTRATION_ID;
            }

            @Override
            protected void onPostExecute(String msg)
            {
                Log.e( TAG , "onPost : " + msg );
            }

        };

        task.execute(null, null, null);
    }


    @Override
    protected void onHandleIntent(Intent intent)
    {
        Log.e( TAG , "onHandleIntent" );
    }
}
