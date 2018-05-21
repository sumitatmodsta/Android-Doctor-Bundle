package com.modastadoc.doctors.network;

/**
 * Created by vijay.hiremath on 13/10/16.
 */
public interface AsyncResponse
{
    void onResponseFromAPI(String output, int REQUEST_NUMBER);
}

