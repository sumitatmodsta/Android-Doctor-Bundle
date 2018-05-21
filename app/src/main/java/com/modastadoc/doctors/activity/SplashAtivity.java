package com.modastadoc.doctors.activity;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.modastadoc.doctors.R;
import com.modastadoc.doctors.common.constants.PreferenceConstants;
import com.modastadoc.doctors.database.LocalDataManager;
import com.modastadoc.doctors.gcm.GCMInfoHandler;

/**
 * Created by vivek.c on 29/09/16.
 */
public class SplashAtivity extends AppCompatActivity
{
    private ImageView mLogo;
    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);

        mContext = this;

        mLogo = (ImageView) findViewById(R.id.splash_logo);
        Animation slideup = AnimationUtils.loadAnimation(this, R.anim.splashscreen_logo_animation);
        mLogo.startAnimation(slideup);

        String auth         = LocalDataManager.getInstance().get(PreferenceConstants.Token);
        String version_code = LocalDataManager.getInstance().get(PreferenceConstants.VERSION_CODE);
        Log.e("authSlash", auth);
        Log.e("authSlash", "-> " + version_code);


        if( version_code.equalsIgnoreCase("") )
        {
            getClass(LoginActivity.class);
        }
        else if ( auth.trim().length() > 0 )
        {
            getClass(HomeActivity.class);
        }
        else
        {
            getClass(LoginActivity.class);
        }

        AsyncTask.execute(new Runnable()
        {
            @Override
            public void run()
            {
                new GCMInfoHandler(mContext).registerGCM();
            }
        });

    }

    private void getClass(final Class t)
    {
        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                final Intent mainIntent = new Intent(SplashAtivity.this, t);
                startActivity(mainIntent);
                finish();
            }
        }, 3000);


    }
}
