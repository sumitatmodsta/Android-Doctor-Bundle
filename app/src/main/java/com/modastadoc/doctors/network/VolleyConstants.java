package com.modastadoc.doctors.network;

import com.android.volley.Request;

/**
 * Created by contractor.anooj on 29/01/16.
 */
public enum VolleyConstants {
    STRING_REQUEST(1), JSON_REQUEST(2);

    int status;

    private VolleyConstants(int status) {
        this.status = status;
    }

    private VolleyConstants(Request.Method status) {
    }


}

