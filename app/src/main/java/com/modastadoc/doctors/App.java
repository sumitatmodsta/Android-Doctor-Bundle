package com.modastadoc.doctors;

import android.app.Application;
import android.content.Context;
import android.os.Debug;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by vijay.hiremath on 23/09/16.
 */
public class App extends Application
{
    String TAG = App.class.getSimpleName();
    private static App instance;
    private static Context mContext;

    @Override
    public void onCreate()
    {
        super.onCreate();

        instance   = this;
        mContext = getApplicationContext();

        //if(BuildConfig.DEBUG)
            //handleSSLHandshake();
    }

    /*private void handleSSLHandshake()
    {
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager()
        {
            public java.security.cert.X509Certificate[] getAcceptedIssuers()
            {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType)
            {
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType)
            {
            }
        }};
        SSLContext sc = null;
        try
        {
            sc = SSLContext.getInstance("SSL");
        } catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        try
        {
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
        } catch (KeyManagementException e)
        {
            e.printStackTrace();
        }
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        // Create all-trusting host name verifier
        HostnameVerifier allHostsValid = new HostnameVerifier()
        {
            public boolean verify(String hostname, SSLSession session)
            {
                return true;
            }
        };
        // Install the all-trusting host verifier
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
    }*/

    public static App getInstance()
    {
        return instance;
    }

    public static Context getAppContext() {
        return mContext;
    }
}
