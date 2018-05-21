package com.modastadoc.doctors.network;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.Map;

/**
 * Created by vijay.hiremath on 11/11/16.
 */
public class PostApiSimple extends StringRequest
{
    Map<String, String> mParams;
    public PostApiSimple( Response.Listener<String> listener, Response.ErrorListener errorListener , Map<String, String> params , String url )
    {
        super(Method.POST , url , listener , errorListener );
        this.mParams = params;
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError
    {
        return mParams;
    }

    @Override
    public String getBodyContentType()
    {
        return "application/x-www-form-urlencoded; charset=UTF-8";
    }

}
