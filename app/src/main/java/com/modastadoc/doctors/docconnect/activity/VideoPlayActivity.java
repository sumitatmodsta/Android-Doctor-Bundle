package com.modastadoc.doctors.docconnect.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.modastadoc.doctors.R;
import com.modastadoc.doctors.widget.videoplayer.ExoPlayerHolder;
import com.modastadoc.doctors.widget.videoplayer.VideoOrientationEvents;

public class VideoPlayActivity extends AppCompatActivity implements VideoOrientationEvents
{
    int oriWidth = 0;
    int oriHeight = 0;

    ExoPlayerHolder holder;
    Context mContext;

    String TAG = VideoPlayActivity.class.getSimpleName();

    String url;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_play);

        mContext = this;

        holder = (ExoPlayerHolder) findViewById(R.id.exoplayerholder);

        if (getSupportActionBar() != null)
        {
            ActionBar actionBar = getSupportActionBar();
            actionBar.hide();
        }

        /************************/
        Bundle bundle = getIntent().getExtras();
        if( bundle != null )
        {
            if( bundle.containsKey("video_link") );
            {
                url = bundle.getString("video_link");
            }
        }
        /************************/


        Log.e( TAG , "video : " + url );
        playExoHolder( url );
    }

    private void playExoHolder(String url)
    {
        holder.startVideo(url, this);
    }

    public static Intent createIntent( Context context , String link )
    {
        Intent intent = new Intent( context , VideoPlayActivity.class);
        intent.putExtra("video_link", link);
        return intent;
    }

    @Override
    public void onFullscreen(int w, int h)
    {
        Log.e(TAG, "fullscreen!");

    }

    @Override
    public void exitFullscreen()
    {
        Log.e( TAG , "exitFullscreen" );
    }


}
