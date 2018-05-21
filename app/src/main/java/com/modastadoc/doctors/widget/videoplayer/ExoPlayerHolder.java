package com.modastadoc.doctors.widget.videoplayer;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.devbrackets.android.exomedia.listener.OnCompletionListener;
import com.devbrackets.android.exomedia.listener.OnErrorListener;
import com.devbrackets.android.exomedia.listener.OnPreparedListener;
import com.devbrackets.android.exomedia.ui.widget.EMVideoView;
import com.modastadoc.doctors.R;

import java.lang.reflect.Method;

/**
 * Created by vijay.hiremath on 16/11/16.
 */
public class ExoPlayerHolder extends RelativeLayout
{
    String TAG = ExoPlayerHolder.class.getSimpleName();

    EMVideoView video;
    VideoController controller;
    boolean isFullscreen;
    Context mContext;
    SeekBar seekBar;

    private int width;
    private int height;
    private int screenButtonHeight;

    TextView tvDuration;
    TextView tvCurrent;
    ImageView ivPlayPause;
    ImageView ivFullscreen;
    VideoOrientationEvents orientationEvents;
    Activity parentActivity;
    TextView tv_connectionLost;
    String mVideoURL;

    /**********************************
     * stop loading video after 50 secs
     **********************************/
    int VIDEO_TIMOUT = 10000;
    int PLAY_STATUS = 1;
    int PAUSE_STATUS = 2;
    int RELOAD_STATUS = 3;

    /**********************************/

    public ExoPlayerHolder(Context context)
    {
        super(context);
    }

    public ExoPlayerHolder(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.exoplayer_holder, this);

        mContext = context;

        video = (EMVideoView) findViewById(R.id.video);

        controller = (VideoController) findViewById(R.id.controller);
        seekBar = controller.getSeekBar();
        tvDuration = controller.getVideoDurationView();
        tvCurrent = controller.getVideoCurrentView();
        ivPlayPause = controller.getPlayButton();
        ivFullscreen = controller.getFullscreen();
        tv_connectionLost = controller.getConnectionLost();

        isFullscreen = false;
        ivPlayPause.setTag(0);

        video.setOnTouchListener(new OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {

                if (video != null)
                {
                    toggelController();
                }

                return false;
            }
        });

        ivPlayPause.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                if (video != null)
                {

                    if (!video.isVideoFinished())
                    {
                        if (video.isPlaying())
                        {
                            video.pause();
                            ivPlayPause.setImageResource(R.drawable.play_btn);
                            ivPlayPause.setTag(PLAY_STATUS);
                            hideControllerAfterTime();
                        } else
                        {
                            video.start();
                            seekBar.postDelayed(onEverySecond, 1000);
                            ivPlayPause.setImageResource(R.drawable.pause_btn);
                            ivPlayPause.setTag(PAUSE_STATUS);
                            hideControllerAfterTime();
                        }
                    } else
                    {
                        video.restart();
                        seekBar.postDelayed(onEverySecond, 1000);
                        ivPlayPause.setImageResource(R.drawable.pause_btn);
                        ivPlayPause.setTag(PAUSE_STATUS);
                        hideControllerAfterTime();
                    }
                } else
                {
                    Log.e(TAG, "video is null.");
                }

            }
        });

        ivFullscreen.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (isFullscreen)
                {
                    exitFullScreenMode();
                } else
                {
                    goFullScreenMode();
                }
            }
        });

        video.setOnCompletionListener(new OnCompletionListener()
        {
            @Override
            public void onCompletion()
            {
                video.setVideoFinished(true);
                showControllerVideoEnds();
            }
        });

        tv_connectionLost.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                tv_connectionLost.setVisibility(View.GONE);

                startVideo(mVideoURL, parentActivity);

                setVideoTimeout();
            }
        });

        measureScreenSize();
    }

    public void exitfullscreen()
    {

        orientationEvents.exitFullscreen();

        controller.setVisibility(View.VISIBLE);
        hideControllerAfterTime();

        isFullscreen = false;
    }

    private void measureScreenSize()
    {

        screenButtonHeight = getSoftButtonsBarHeight();

        Display display = ((Activity) mContext).getWindowManager().getDefaultDisplay();

        if (Build.VERSION.SDK_INT >= 17)
        {
            //new pleasant way to get real metrics
            DisplayMetrics realMetrics = new DisplayMetrics();
            display.getRealMetrics(realMetrics);
            width = realMetrics.widthPixels;
            height = realMetrics.heightPixels;

        } else if (Build.VERSION.SDK_INT >= 14)
        {
            //reflection for this weird in-between time
            try
            {
                Method mGetRawH = Display.class.getMethod("getRawHeight");
                Method mGetRawW = Display.class.getMethod("getRawWidth");
                width = (Integer) mGetRawW.invoke(display);
                height = (Integer) mGetRawH.invoke(display);
            } catch (Exception e)
            {
                //this may not be 100% accurate, but it's all we've got
                width = display.getWidth();
                height = display.getHeight();
            }

        } else
        {
            //This should be close, as lower API devices should not have window navigation bars
            width = display.getWidth();
            height = display.getHeight();
        }

        // when landscape w > h, swap it
        if (width > height)
        {
            int temp = width;
            width = height;
            height = temp;
        }

        /**
         * important!
         */
        Log.e(TAG, "height             : " + height);
        Log.e(TAG, "screenButtonHeight : " + screenButtonHeight);
    }

    private int getSoftButtonsBarHeight()
    {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
        {

            DisplayMetrics metrics = new DisplayMetrics();
            ((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(metrics);

            int usableHeight = metrics.heightPixels;
            ((Activity) mContext).getWindowManager().getDefaultDisplay().getRealMetrics(metrics);

            int realHeight = metrics.heightPixels;

            if (realHeight > usableHeight)
                return realHeight - usableHeight;
            else
                return 0;
        }
        return 0;
    }

    public void goFullscreen()
    {

        int w = width;
        int h = height;

        int scrnBttnHeight = screenButtonHeight;

        if (checkTabletOrPhone())
            w = w - scrnBttnHeight;

        orientationEvents.onFullscreen(w, h);
        controller.setVisibility(View.VISIBLE);
        hideControllerAfterTime();

        isFullscreen = true;
    }

    private boolean checkTabletOrPhone()
    {
        DisplayMetrics metrics = new DisplayMetrics();
        parentActivity.getWindowManager().getDefaultDisplay().getMetrics(metrics);

        float yInches = metrics.heightPixels / metrics.ydpi;
        float xInches = metrics.widthPixels / metrics.xdpi;
        double diagonalInches = Math.sqrt(xInches * xInches + yInches * yInches);
        if (diagonalInches >= 6.5)
        {
            Log.e(TAG, "Its a table!");
            return true;
        } else
        {
            return false;
        }
    }

    private Runnable onEverySecond = new Runnable()
    {

        @Override
        public void run()
        {

            if (seekBar != null)
            {
                seekBar.setProgress(video.getCurrentPosition());
            }

            if (video.isPlaying())
            {
                seekBar.postDelayed(onEverySecond, 1000);
                tvCurrent.setText(displayTime(video.getCurrentPosition()));
            } else
            {
                Log.e(TAG, "not playing");
            }

        }
    };

    private void toggelController()
    {
        if (video != null && !video.isVideoFinished())
        {

            int controller_visiblity = controller.getVisibility();
            if (controller_visiblity == 0)
            {
                controller.setVisibility(View.INVISIBLE);
            } else
            {
                controller.setVisibility(View.VISIBLE);
                hideControllerAfterTime();
            }

        }
    }

    public String displayTime(int ms)
    {

        long totalSecs = ms / 1000;
        long hours = (totalSecs / 3600);
        long mins = (totalSecs / 60) % 60;
        long secs = totalSecs % 60;

        String minsString = (mins == 0)
                ? "00"
                : ((mins < 10)
                ? "0" + mins
                : "0" + mins);
        String secsString = (secs == 0)
                ? "00"
                : ((secs < 10)
                ? "0" + secs
                : "" + secs);
        if (hours > 0)
            return hours + ":" + minsString + ":" + secsString;
        else if (mins > 0)
            return mins + ":" + secsString;
        else if (mins == 0)
            return "0:" + secsString;

        return ":" + secsString;
    }

    private void hideControllerAfterTime()
    {

        Handler handler = new Handler();
        handler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                if (video != null && !video.isVideoFinished())
                {
                    Log.e(TAG, "BOOM!");
                    controller.setVisibility(View.INVISIBLE);
                }
            }
        }, 4000);
    }

    private void showControllerVideoEnds()
    {
        ivPlayPause.setImageResource(R.drawable.reload);
        ivPlayPause.setTag(RELOAD_STATUS);
        controller.setVisibility(View.VISIBLE);
    }

    public void startVideo(String videoURL, Activity activity)
    {
        mVideoURL = videoURL;
        parentActivity = activity;

        try
        {
            orientationEvents = (VideoOrientationEvents) parentActivity;
        } catch (Exception e)
        {
            Log.e(TAG, "Activity must implement VideoOrientationEvent");
            return;
        }

        video.setVideoPath(videoURL);
        controller.startLoading();

        setVideoTimeout();

        video.setOnPreparedListener(new OnPreparedListener()
        {
            @Override
            public void onPrepared()
            {

                controller.stopLoading();

                video.start();
                video.setVideoFinished(false);

                setUpSeekbar(video.getDuration());
                tvDuration.setText(displayTime(video.getDuration()));
                Log.e(TAG, "Playing ? " + video.isPlaying());
                hideControllerAfterTime();
            }
        });

        video.setOnErrorListener(new OnErrorListener()
        {
            @Override
            public boolean onError()
            {
                Log.e(TAG, "onError!");
                controller.stopLoadingError();
                return false;
            }
        });

    }

    public void setUpSeekbar(int duration)
    {

        seekBar.setMax(duration);
        seekBar.postDelayed(onEverySecond, 1000);


        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser)
            {

                if (fromUser)
                {
                    // this is when actually seekbar has been seeked to a new position
                    video.seekTo(progress);
                }
            }
        });
    }


    public void goFullScreenMode()
    {
        goFullscreen();
    }

    public void exitFullScreenMode()
    {
//        video.setVideoRotation(90);
//        video.setVideoRotation(90);
//        video.setVideoRotation(90);
        isFullscreen = false;
        orientationEvents.exitFullscreen();
    }

    public boolean isVideoPlaying()
    {
        return video.isPlaying();
    }

    public void pauseVideo()
    {
        video.pause();
    }

    public void videoTimeoutError()
    {
        Log.e(TAG, "timeout bro. sorry :(");
        video.stopPlayback();
        controller.stopLoadingError();
    }

    private void setVideoTimeout()
    {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {

                int temp = (Integer) ivPlayPause.getTag();
                boolean bug = temp == PLAY_STATUS;
                if (isVideoPlaying())
                {
                    Log.e(TAG, "video is playing dude");
                } else if (bug)
                {
                    Log.e(TAG, "it is paused!!");
                } else
                {
                    videoTimeoutError();
                }
            }

        }, VIDEO_TIMOUT);
    }
}
