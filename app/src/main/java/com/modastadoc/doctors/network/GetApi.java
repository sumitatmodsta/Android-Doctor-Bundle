package com.modastadoc.doctors.network;

import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.modastadoc.doctors.VolleyCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

/**
 * Created by vijay.hiremath on 20/07/16.
 */
public class GetApi extends Request<JSONObject>
{
    private VolleyCallback callback;
    private int API_CODE;
    String TAG = GetApi.class.getSimpleName();

    public GetApi( Object basicObj , Response.ErrorListener errorListener, String url , int apiCode )
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
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response)
    {
        String jsonString = null;
        try
        {
            jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));

            JSONObject obj = new JSONObject(jsonString);
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
    protected void deliverResponse(JSONObject jsonObject)
    {
        callback.volleyResponse( API_CODE , jsonObject.toString() );
    }

    @Override
    protected VolleyError parseNetworkError(VolleyError volleyError)
    {
        return volleyError;
    }
}