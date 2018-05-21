package com.modastadoc.doctors.network;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.modastadoc.doctors.common.constants.PreferenceConstants;
import com.modastadoc.doctors.common.constants.ServerConstants;
import com.modastadoc.doctors.database.LocalDataManager;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

/**
 * Created by contractor.anooj on 24/03/16.
 */
public class ImageUpload extends AsyncTask<Void, Void, String> {

    private String mPath, mOrderID;
    public ImageUploadResponse uploadResponse = null;

    public ImageUpload(ImageUploadResponse listener, String path, String orderID) {
        this.uploadResponse = listener;
        this.mPath = path;
        this.mOrderID = orderID;
    }

    @Override
    protected String doInBackground(Void... unsued) {
        try {
            HttpClient httpClient = new DefaultHttpClient();
            HttpContext localContext = new BasicHttpContext();
            HttpPost httpPost = new HttpPost(ServerConstants.SUMMARY_DOCUMENT_UPLOAD);

            httpPost.setHeader("Authorization", LocalDataManager.getInstance().get(PreferenceConstants.Token));

            MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

            //String cookie = AccountHelper.getUserCookie();
            File file = new File(mPath);
            entity.addPart("pri_files", new FileBody(file, "application/octet"));
            entity.addPart("orderId", new StringBody(mOrderID));

            httpPost.setEntity(entity);

            HttpResponse response = httpClient.execute(httpPost, localContext);

            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));

            return reader.readLine();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(e.getClass().getName(), e.getMessage(), e);
            return null;
        }

    }

    @Override
    protected void onProgressUpdate(Void... unsued) {

    }

    @Override
    protected void onPostExecute(String sResponse) {
        try {
            uploadResponse.onResponse(sResponse);
            Log.i("SummaryFra -- ", sResponse + " -- ");
            /*System.out.print(sResponse);

            if (sResponse != null) {
                *//*JSONObject mUploadObj = new JSONObject(sResponse);
                JSONObject mDataObject = mUploadObj.getJSONObject("data");
                String mUploadedImgURL = mDataObject.getString("url");
                uploadResponse.afterImageUpload(mUploadedImgURL);*//*

            }else {
                error();
            }*/
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("SummaryFra -- ", e.getMessage());
            error();
        }
    }

    private void error() {
        uploadResponse.onResponse("");
    }
}