package com.modastadoc.doctors.network;

import android.content.Context;
import android.util.Log;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vijay.hiremath on 13/10/16.
 */
public class HttpRequestWorker
{
    public Context currentContext;
    public AsyncResponse delegate = null;
    String server,sessionHeader;
    Header[] headers;
    String[] value;
    boolean isJson  =   true;
    List nameValuePairs = new ArrayList();
    public HttpRequestWorker() {
        super();
    }
    public HttpRequestWorker(List requestbody) {
        isJson  =   false;
        nameValuePairs  =   requestbody;
    }

    /*
         * Method: GetRequest
         * @param: url:String,isHeaderRequired:boolean
         * @Desc : url				: URL to connect the server,
         *         isHeaderRequired	: Set dbName as Header for every request after Registration.
         */

    public String GetRequest(String url) {

        try {

            HttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(url);


            HttpParams params = httpClient.getParams();
            Log.e("Http Params", "" + params);
            HttpConnectionParams.setConnectionTimeout(params, 3000);//connection timeout
            HttpConnectionParams.setSoTimeout(params, 0);//data timeout

            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            String result = httpClient.execute(httpGet, responseHandler);
            Log.e("RESPONSE", "" + result);
            return result;
        }
        catch (Exception ex) {
            return "Failed " + ex;
        }
    }

    /*
     * Method: POSTRequest
     * @param: url:String, content:String, isHeaderRequired:boolean
     * @Desc : url				: URL to connect the server,
     *         content			: JSON Content to send,
     *         isHeaderRequired	: Set dbName as Header for every request after Registration.
     */
    public String PostRequest(String url, String content) {
        try {
            Log.e("HTTP:POST:REQ: ", "" + url+" "+content);

            String mStatus = null;

            DefaultHttpClient httpclient = new DefaultHttpClient();
            HttpPost mHttpPost = new HttpPost(url);
            if(isJson) {
                StringEntity se = new StringEntity(content);
                se.setContentType("application/json");
                mHttpPost.setEntity(se);
            }else{
                mHttpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            }


            HttpResponse httpresponse = httpclient.execute(mHttpPost);
            mStatus = EntityUtils.toString(httpresponse.getEntity(), "utf-8");

            headers = httpresponse.getAllHeaders();
            Log.e("RESPONSE_OF"+url,mStatus);
            Log.e("UTF_8", mStatus + "");
            return  mStatus;

        } catch (Exception ex) {
            ex.printStackTrace();
            return "Failed " + ex;
        }
    }



    public String postResponse()
    {
        String responseHeader = ""+ headers;
        Log.e("response Header",responseHeader);
        return responseHeader;
    }
}
