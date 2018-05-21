package com.modastadoc.doctors.network;

/**
 * Created by vijay.hiremath on 13/10/16.
 */
public enum AsyncConstants
{
    GET_REQUEST(1), POST_REQUEST(2);

    int status;

    private AsyncConstants(int status)
    {
        this.status = status;
    }
}
