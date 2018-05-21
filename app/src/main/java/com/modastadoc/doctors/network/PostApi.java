package com.modastadoc.doctors.network;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.modastadoc.doctors.common.constants.PreferenceConstants;
import com.modastadoc.doctors.common.utils.AppCoreUtil;
import com.modastadoc.doctors.database.LocalDataManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by vijay.hiremath on 19/09/16.
 */
public class PostApi extends StringRequest
{
    String TAG = PostApi.class.getSimpleName();

    Map<String, String> mParams;
    public PostApi( Response.Listener<String> listener, Response.ErrorListener errorListener,
                    Map<String, String> params , String url ) {
        super(Method.POST , url , listener , errorListener );
        this.mParams = params;
        setRetryPolicy(new DefaultRetryPolicy(
                        80000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        );
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return mParams;
    }

    @Override
    public String getBodyContentType() {
        return "application/x-www-form-urlencoded; charset=UTF-8";
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        HashMap<String, String> params = new HashMap<>();

        String auth     = LocalDataManager.getInstance().get(PreferenceConstants.Token);
        AppCoreUtil.log(TAG, "user token", auth);
        params.put( "Authorization" , auth );
        return params;
    }

}
