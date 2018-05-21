package com.modastadoc.doctors.network;

import java.util.List;

/**
 * Created by vijay.hiremath on 13/10/16.
 */
public class AsyncParams
{
    public String URL;
    public int REQUEST_NUMBER;
    public AsyncConstants REQUEST_TYPE;
    public String REQUEST_BODY;
    public boolean isHeaderRequired;
    public List formEntity;
    public boolean isJSON = false;

    public AsyncParams(String URL, int REQUEST_NUMBER, AsyncConstants REQUEST_TYPE, String REQUEST_BODY, boolean isHeaderRequired, boolean isJSON, List formEntity)
    {
        this.URL = URL;
        this.REQUEST_NUMBER = REQUEST_NUMBER;
        this.REQUEST_TYPE = REQUEST_TYPE;
        this.REQUEST_BODY = REQUEST_BODY;
        this.isHeaderRequired = isHeaderRequired;
        this.isJSON = isJSON;
        this.formEntity = formEntity;
    }
}
