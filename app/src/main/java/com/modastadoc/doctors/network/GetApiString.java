package com.modastadoc.doctors.network;

import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.modastadoc.doctors.VolleyCallback;

import java.io.UnsupportedEncodingException;

/**
 * Created by vijay.hiremath on 05/12/16.
 */
public class GetApiString extends Request<String>
{

    private VolleyCallback callback;
    private int API_CODE;
    String TAG = GetApiString.class.getSimpleName();


    public GetApiString( Object basicObj , Response.ErrorListener errorListener, String url , int apiCode )
    {
        super(Method.GET, url, errorListener);

        this.callback = (VolleyCallback) basicObj;
        this.API_CODE = apiCode;

        setRetryPolicy(new DefaultRetryPolicy(
                        40000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        );
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response)
    {
        String jsonString = null;
        try
        {
            jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));

            return Response.success(jsonString,HttpHeaderParser.parseCacheHeaders(response));
        }
        catch (UnsupportedEncodingException e)
        {
            Log.e(TAG, "UnsupportedEncodingException : " + e.toString());
        }

        return null;
    }

    @Override
    protected void deliverResponse(String response)
    {
        callback.volleyResponse( API_CODE , response );
    }

    @Override
    protected VolleyError parseNetworkError(VolleyError volleyError)
    {
        return volleyError;
    }
}
