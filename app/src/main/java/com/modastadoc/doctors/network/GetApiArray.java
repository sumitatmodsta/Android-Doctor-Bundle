package com.modastadoc.doctors.network;

import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.modastadoc.doctors.VolleyCallback;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;

/**
 * Created by vijay.hiremath on 11/11/16.
 */
public class GetApiArray extends Request<JSONArray>
{
    private VolleyCallback callback;
    private int API_CODE;
    String TAG = GetApiArray.class.getSimpleName();

    public GetApiArray( Object basicObj , Response.ErrorListener errorListener, String url , int apiCode )
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
    protected Response<JSONArray> parseNetworkResponse(NetworkResponse response)
    {
        String jsonString = null;
        try
        {
            jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));

            JSONArray obj = new JSONArray(jsonString);
            return Response.success(obj,HttpHeaderParser.parseCacheHeaders(response));
        }
        catch (UnsupportedEncodingException e)
        {
            Log.e(TAG, "UnsupportedEncodingException : " + e.toString());
        }
        catch (JSONException e)
        {
            Log.e( TAG , "JSONException : " + e.toString());
        }

        return null;
    }

    @Override
    protected void deliverResponse(JSONArray jsonObject)
    {
        callback.volleyResponse( API_CODE , jsonObject.toString() );
    }

    @Override
    protected VolleyError parseNetworkError(VolleyError volleyError)
    {
        return volleyError;
    }
}
