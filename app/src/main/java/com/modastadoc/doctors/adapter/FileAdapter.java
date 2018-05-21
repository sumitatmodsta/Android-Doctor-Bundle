package com.modastadoc.doctors.adapter;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Browser;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.modastadoc.doctors.R;
import com.modastadoc.doctors.activity.Live2Activity;
import com.modastadoc.doctors.activity.PatientInfoActivity;
import com.modastadoc.doctors.activity.ViewSummaryActivity;
import com.modastadoc.doctors.common.constants.AppConstants;
import com.modastadoc.doctors.common.constants.PreferenceConstants;
import com.modastadoc.doctors.common.constants.ServerConstants;
import com.modastadoc.doctors.database.LocalDataManager;
import com.modastadoc.doctors.model.PatientFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

/**
 * Created by kunai on 14/08/17.
 */

public class FileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements IFileAdapter{

    final int REQUEST_CODE = 1;

    private List<PatientFile> mList;
    private Context mContext;

    PatientFile fileName;

    public FileAdapter(Context context) {
        mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_upload_file, parent, false);

        return new FileViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((FileViewHolder)holder).bind(mList.get(position));
    }

    @Override
    public int getItemCount() {
        if(mList != null && mList.size() > 0)
            return mList.size();
        return 0;
    }

    @Override
    public void permissionRequestCallback() {
        viewFile(fileName);
    }

    private class FileViewHolder extends RecyclerView.ViewHolder {
        private TextView message, viewFile;
        FileViewHolder(View view) {
            super(view);

            message = (TextView) view.findViewById(R.id.name);
            viewFile = (TextView) view.findViewById(R.id.view);
        }

        void bind(final PatientFile file) {
            message.setText(file.name);
            viewFile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    viewFile(file);
                }
            });
        }
    }

    public void refresh(List<PatientFile> list) {
        this.mList = list;
        notifyDataSetChanged();
    }

    private void viewFile(PatientFile file) {
        if(file.type.equalsIgnoreCase(AppConstants.FILE_TYPE_LAB)) {

            String url = ServerConstants.VIEW_LAB_REPORT + file.id;
            Log.v("url", url);

            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            Bundle bundle = new Bundle();

            String auth     = LocalDataManager.getInstance().get(PreferenceConstants.Token);
            bundle.putString("Authorization", auth);
            browserIntent.putExtra(Browser.EXTRA_HEADERS, bundle);

            mContext.startActivity(browserIntent);
        }else {
            String url = ServerConstants.DOMAIN + ServerConstants.VIEW_REPORT_API + file.id;
            Log.v("url", url);

            String auth = LocalDataManager.getInstance().get(PreferenceConstants.Token);

            int index = file.name.lastIndexOf(".");
            String fileExtantion = index != -1 ? file.name.substring(index + 1, file.name.length()) : "";

            if (fileExtantion.equalsIgnoreCase("jpg") || fileExtantion.equalsIgnoreCase("jpeg") || fileExtantion.equalsIgnoreCase("png")) {
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("Authorization", auth);

                if (mContext instanceof PatientInfoActivity) {
                    PatientInfoActivity activity = (PatientInfoActivity) mContext;
                    activity.ShowPatientDetails(url, params);
                }else if (mContext instanceof Live2Activity) {
                    Live2Activity activity = (Live2Activity) mContext;
                    activity.ShowPatientDetails(url, params);
                }else if (mContext instanceof ViewSummaryActivity) {
                    ViewSummaryActivity activity = (ViewSummaryActivity) mContext;
                    activity.ShowPatientDetails(url, params);
                }
            } else if (fileExtantion.equalsIgnoreCase("pdf")) {
                fileName = file;
                if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    new DownloadPdf().execute(url, fileName.name);
                } else {
                    if (mContext instanceof PatientInfoActivity) {
                        ActivityCompat.requestPermissions((PatientInfoActivity) mContext, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
                    } else if (mContext instanceof Live2Activity) {
                        ActivityCompat.requestPermissions((Live2Activity) mContext, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
                    } else if (mContext instanceof ViewSummaryActivity) {
                        ActivityCompat.requestPermissions((ViewSummaryActivity) mContext, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
                    }
                }
            }
        }
    }

    private void downloadFile(String url, File fileName) {
        final int MEGABYTE = 1024 * 1024;
        String auth = LocalDataManager.getInstance().get(PreferenceConstants.Token);

        try {
            URL url1 = new URL(url);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url1.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Authorization", auth);
            httpURLConnection.connect();
            InputStream inputStream = httpURLConnection.getInputStream();
            FileOutputStream fileOutputStream = new FileOutputStream(fileName);

            byte[] buffer = new byte[MEGABYTE];
            int bufferLength = 0;
            while ((bufferLength = inputStream.read(buffer)) > 0) {
                fileOutputStream.write(buffer, 0, bufferLength);
            }
            fileOutputStream.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openPdfFile(){
        File pdfFile = new File(Environment.getExternalStorageDirectory() + "/reports/" + fileName.name);  // -> filename = maven.pdf
        Uri path = Uri.fromFile(pdfFile);
        Intent pdfIntent = new Intent(Intent.ACTION_VIEW);
        pdfIntent.setDataAndType(path, "application/pdf");
        pdfIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        try{
            mContext.startActivity(pdfIntent);
        }catch(ActivityNotFoundException e){
            Toast.makeText(mContext, "No Application available to view PDF", Toast.LENGTH_SHORT).show();
        }
    }

    private class DownloadPdf extends AsyncTask<String, Void,Void>{

        @Override
        protected Void doInBackground(String... strings) {
            String url = strings[0];
            String fileName = strings[1];

            String extDir = Environment.getExternalStorageDirectory().toString();
            File folder = new File(extDir, "reports");
            folder.mkdir();

            File pdf = new File(folder, fileName);
            try {
                pdf.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            downloadFile(url,pdf);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            openPdfFile();
        }
    }
}
