package com.modastadoc.doctors.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Browser;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.modastadoc.doctors.R;
import com.modastadoc.doctors.common.constants.PreferenceConstants;
import com.modastadoc.doctors.common.constants.ServerConstants;
import com.modastadoc.doctors.database.LocalDataManager;

/**
 * Created by vivek.c on 30/09/16.
 */
public class View_Reports extends AppCompatActivity
{
    String TAG = View_Reports.class.getSimpleName();

    ListView listView;
    Intent getIntent;

    String[] parts;
    String[] partsNames;

    public static Intent createIntent( Context context , String reports, String reportName , String type )
    {
        Intent intent = new Intent( context , View_Reports.class);
        intent.putExtra("reports"     , "" + reports );
        intent.putExtra("reportNames" , "" + reportName );
        intent.putExtra("type"        , "" + type );

        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_reports);

        listView = (ListView) findViewById(R.id.listView);

        getIntent = getIntent();

        String reports     = getIntent.getStringExtra("reports");
        String reportNames = getIntent.getStringExtra("reportNames");
        final String type        = getIntent.getStringExtra("type");


        Log.e("current_bug" , "reports     : " + reports );
        Log.e("current_bug" , "ReportName  : " + reportNames );

        if( reportNames != null )
        {
            partsNames = reportNames.split( "," );
        }

        if( reports != null )
        {
            parts      = reports.split( "," );
        }

        if( parts == null  )
        {
            Toast.makeText(View_Reports.this, "No Reports found!", Toast.LENGTH_SHORT).show();
            finish();
        }

        if( partsNames != null && reportNames.length() > 0 )
        {
            ArrayAdapter<String> adapter = new ArrayAdapter<String>( this, android.R.layout.simple_list_item_1, partsNames );
            listView.setAdapter( adapter );
        }
        else if ( parts != null && reports.length() > 0)
        {
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, parts);
            listView.setAdapter( adapter );
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                String s = parts[position];
                if( type .equalsIgnoreCase("lab") )
                {
                    viewLabDocument(s);
                }
                else
                {
                    viewDocument(s);
                }

            }
        });
    }

    private void viewLabDocument( String uploadid )
    {
        Log.e( TAG , "UploadId : " + uploadid);

        String VIEW_REPORT = ServerConstants.VIEW_LAB_REPORT;
        String imageUrl =  VIEW_REPORT + uploadid;

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(imageUrl));
        Bundle bundle = new Bundle();

        String auth     = LocalDataManager.getInstance().get(PreferenceConstants.Token);
        bundle.putString("Authorization", auth);
        browserIntent.putExtra(Browser.EXTRA_HEADERS, bundle);

        startActivity(browserIntent);
    }

    private void viewDocument(String report_id)
    {
        String dummy_report_id = report_id;

        String BASEURL = ServerConstants.DOMAIN;
        String VIEW_REPORT = ServerConstants.VIEW_REPORT_API;
        String imageUrl = BASEURL + VIEW_REPORT + dummy_report_id;
        Log.e( "url" , imageUrl );

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(imageUrl));
        Bundle bundle = new Bundle();

        String thisisit = LocalDataManager.getInstance().get(PreferenceConstants.LoginPrefrences);
        String auth = LocalDataManager.getInstance().get(PreferenceConstants.Token);
        Log.v("auth", auth);

        bundle.putString( "Authorization", auth );
        browserIntent.putExtra(Browser.EXTRA_HEADERS, bundle);

        startActivity(browserIntent);
    }
}
