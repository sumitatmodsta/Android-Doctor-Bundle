package com.modastadoc.doctors.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * Created by vijay.hiremath on 13/10/16.
 */
public class NetworkChecker
{
    private static NetworkChecker instance = new NetworkChecker();

    static Context context;
    ConnectivityManager connectivityManager;
    NetworkInfo wifiInfo, mobileInfo;
    boolean connected = false;

    public static NetworkChecker getInstance(Context ctx) {
        context = ctx.getApplicationContext();
        return instance;
    }

    public boolean haveNetworkConnection() {

        try {
            connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            connected = networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected();
            Log.e("network_connection", "" + connected);
            return connected;

        } catch (Exception e) {
            System.out.println("CheckConnectivity_Exception: " + e.getMessage());
            Log.e("connectivity", e.toString());
            return false;
        }

    }
}
