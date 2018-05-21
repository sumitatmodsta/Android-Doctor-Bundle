package com.modastadoc.doctors.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.modastadoc.doctors.R;

import java.util.List;


/**
 * Created by Vivek on 20/09/16.
 */

public class ContactUs extends AppCompatActivity {
    TextView email;
    ImageView google_plus, facebook, twitter;
    String twitterURL = "https://twitter.com/ModastaHealth";
    String facebookURL = "https://www.facebook.com/modasta/";
    String googleURL = "https://plus.google.com/u/0/101891887526878173339/about";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contactus);
        email = (TextView) findViewById(R.id.email_us);
        getSupportActionBar().setTitle("ContactUs");
        getSupportActionBar().setHomeButtonEnabled(true);
        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                intent.setType("text/html");
                final PackageManager pm = getPackageManager();
                final List<ResolveInfo> matches = pm.queryIntentActivities(intent, 0);
                ResolveInfo best = null;
                for (final ResolveInfo info : matches) {
                    if (info.activityInfo.packageName.endsWith(".gm") || info.activityInfo.name.toLowerCase().contains("gmail")) {
                        best = info;
                        break;
                    }
                }
                if (best != null) {
                    intent.setClassName(best.activityInfo.packageName, best.activityInfo.name);
                }

                intent.setData(Uri.parse("info@modasta.com"));
                intent.putExtra(Intent.EXTRA_SUBJECT, "My Query");
                intent.putExtra(Intent.EXTRA_TEXT, "Hi Modasta Team");
                try {
                    startActivity(intent);
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(ContactUs.this, "Error Sending Email,Try Later.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        google_plus = (ImageView) findViewById(R.id.google_plus);
        google_plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSocialMedia(googleURL);
            }
        });
        facebook = (ImageView) findViewById(R.id.facebook);
        facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSocialMedia(facebookURL);
            }
        });
        twitter = (ImageView) findViewById(R.id.twitter);
        twitter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSocialMedia(twitterURL);
            }
        });
    }

    public void openSocialMedia(String url) {
        Uri uri = Uri.parse(url); // missing 'http://' will cause crashed
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
