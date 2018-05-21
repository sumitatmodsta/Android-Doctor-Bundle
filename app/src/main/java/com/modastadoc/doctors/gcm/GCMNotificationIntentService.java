package com.modastadoc.doctors.gcm;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.modastadoc.doctors.R;
import com.modastadoc.doctors.activity.HomeActivity;
import com.modastadoc.doctors.activity.PatientInfoActivity;
import com.modastadoc.doctors.activity.SuggestionsFromInHouseActivity;
import com.modastadoc.doctors.common.constants.PreferenceConstants;
import com.modastadoc.doctors.database.LocalDataManager;

import org.json.JSONObject;

/**
 * Created by vijay.hiremath on 11/10/16.
 */
public class GCMNotificationIntentService extends IntentService
{
    String TAG = GCMNotificationIntentService.class.getSimpleName();
    NotificationManager mNotificationManager;

    public GCMNotificationIntentService()
    {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);

        for (String key : extras.keySet())
        {
            Log.d(TAG, key + " : KEY");
        }


        String user_type = LocalDataManager.getInstance().get(PreferenceConstants.USER_TYPE);
        if( user_type.equalsIgnoreCase(PreferenceConstants.ONLY_IMAKA) )
        {
            return;
        }

        Log.e(TAG, "***********************************");
        Log.e(TAG, "New Notification ");
        Log.e(TAG, "Received    : " + extras.toString());
        Log.e(TAG, "MessageType :" + messageType);

        if (extras.containsKey("message"))
        {
            String data = extras.getString("message");
            Log.i(TAG, data);
            try
            {
                JSONObject jsonObject = new JSONObject(data);
                String slug = jsonObject.optString("slug");
                Log.i(TAG, slug);
                JSONObject jsonData = jsonObject.optJSONObject("data");
                if(jsonData != null && jsonData.length() > 0) {
                    String title, message, orderID;
                    switch (slug) {
                        case GCMContracts.NEW_QUERY_NOTIFY:
                        case GCMContracts.FOLLOWUP_QUERY:
                        case GCMContracts.DOC_SUGGESTION:
                            title = jsonData.optString("title");
                            message = jsonData.optString("message");

                            buildNotification(title, message, slug);
                            break;
                        case GCMContracts.D_BOOKING_SUCCESS:
                        case GCMContracts.D_BOOKING_REMINDER:
                        case GCMContracts.D15_BOOKING_REMINDER:
                        case GCMContracts.PATIENT_READY:
                        case GCMContracts.DOCTOR_RESHEDULE:
                        case GCMContracts.DOCTOR_CANCEL:
                            String docID = jsonData.optString("doctor_id");
                            if(docID.equalsIgnoreCase(LocalDataManager.getInstance().get(PreferenceConstants.DOCID))) {
                                title = jsonData.getString("title");
                                message = jsonData.getString("message");
                                orderID = jsonData.getString("order_id");
                                buildAppointMentNotification(orderID, title, message, slug);
                            }
                            break;
                        default:
                            break;
                    }
                }
                /*if (slug.equalsIgnoreCase(GCMContracts.NEW_QUERY_NOTIFY))
                {
                    JSONObject jsonData = jsonObject.getJSONObject("data");
                    String title        = jsonData.getString("title");
                    String message      = jsonData.getString("message");

                    buildNotification( title , message , slug);
                }
                else if( slug.equalsIgnoreCase(GCMContracts.FOLLOWUP_QUERY))
                {
                    JSONObject jsonData = jsonObject.getJSONObject("data");
                    String title        = jsonData.getString("title");
                    String message      = jsonData.getString("message");

                    buildNotification( title , message , slug );
                }
                else if( slug.equalsIgnoreCase( GCMContracts.DOC_SUGGESTION) )
                {
                    JSONObject jsonData = jsonObject.getJSONObject("data");
                    String title        = jsonData.getString("title");
                    String message      = jsonData.getString("message");

                    buildNotification( title , message , slug );
                }*/

            }
            catch( Exception e )
            {
                Log.e( TAG , "E - " + e.toString() );
            }
        }
        Log.e(TAG, "***********************************");
    }

    private void buildAppointMentNotification(String orderID, String title, String message, String slug) {

        mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent contentIntent = null;

        NotificationCompat.Builder mBuilder = null;
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            mBuilder = new NotificationCompat.Builder(this)
                    .setLargeIcon(((BitmapDrawable) this.getResources().getDrawable(R.drawable.appicon)).getBitmap())
                    .setSmallIcon( R.drawable.ic_stat_noticication_icon )
                    .setContentTitle( "" + title  )
                    .setContentText(  "" + message )
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message));
        } else {
            // Lollipop specific setColor method goes here.
            mBuilder = new NotificationCompat.Builder(this)
                    .setLargeIcon(((BitmapDrawable) this.getResources().getDrawable(R.drawable.appicon)).getBitmap())
                    .setSmallIcon(R.drawable.ic_stat_noticication_icon)
                    .setContentTitle("" + title   )
                    .setContentText( "" + message )
                    .setColor(getBaseContext().getResources().getColor(R.color.colorPrimaryDark))
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message));
        }

        Intent in = new Intent(this, PatientInfoActivity.class);
        in.putExtra("order_id", orderID);
        in.putExtra("from_notification", true);


        int iUniqueId = (int) (System.currentTimeMillis() & 0xfffffff);
        contentIntent = PendingIntent.getActivity(this, iUniqueId, in, 0);

        mBuilder.setContentIntent(contentIntent);
        mBuilder.setAutoCancel(true);

        mNotificationManager.notify(GCMContracts.PUSH_NOTIFICATION_ID, mBuilder.build());

        try {
            Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone ringtone = RingtoneManager.getRingtone(this.getApplicationContext(), soundUri);
            ringtone.play();
        } catch (Exception e) {
            Log.e(TAG, "notification sound error : " + e);
        }
    }

    private void buildNotification( String title , String message , String slug )
    {
        mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent contentIntent = null;

        NotificationCompat.Builder mBuilder = null;
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
        {
            mBuilder = new NotificationCompat.Builder(this)
                    .setLargeIcon(((BitmapDrawable) this.getResources().getDrawable(R.drawable.appicon)).getBitmap())
                    .setSmallIcon( R.drawable.ic_stat_noticication_icon )
                    .setContentTitle( "" + title  )
                    .setContentText(  "" + message )
                    .setStyle(new NotificationCompat.BigTextStyle().bigText("BigText"));
        }
        else
        {
            // Lollipop specific setColor method goes here.
            mBuilder = new NotificationCompat.Builder(this)
                    .setLargeIcon(((BitmapDrawable) this.getResources().getDrawable(R.drawable.appicon)).getBitmap())
                    .setSmallIcon(R.drawable.ic_stat_noticication_icon)
                    .setContentTitle("" + title   )
                    .setContentText( "" + message )
                    .setColor(getBaseContext().getResources().getColor(R.color.colorPrimaryDark))
                    .setStyle(new NotificationCompat.BigTextStyle().bigText("BigText"));
        }

        Intent intent = null;
        if( slug.equalsIgnoreCase( GCMContracts.DOC_SUGGESTION ))
        {
            intent = new Intent( this , SuggestionsFromInHouseActivity.class);
            intent.putExtra( GCMContracts.GCM_SLUG , slug );
        }
        else
        {
            intent = new Intent(this, HomeActivity.class);
            intent.putExtra( GCMContracts.GCM_SLUG , slug );
        }


        if (intent != null)
        {
            int iUniqueId = (int) (System.currentTimeMillis() & 0xfffffff);
            contentIntent = PendingIntent.getActivity(this, iUniqueId, intent, 0);
        } else
        {
            Log.e(TAG, "intent is null");
            return;
        }

        mBuilder.setContentIntent(contentIntent);
        mBuilder.setAutoCancel(true);

        mNotificationManager.notify(GCMContracts.PUSH_NOTIFICATION_ID, mBuilder.build());

        try
        {
            Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone ringtone = RingtoneManager.getRingtone(this.getApplicationContext(), soundUri);
            ringtone.play();
        } catch (Exception e)
        {
            Log.e(TAG, "notification sound error : " + e);
        }
    }
}
