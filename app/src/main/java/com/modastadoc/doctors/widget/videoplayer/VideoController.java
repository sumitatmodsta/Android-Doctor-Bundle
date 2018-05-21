package com.modastadoc.doctors.widget.videoplayer;

import android.content.Context;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.modastadoc.doctors.R;

/**
 * Created by vijay.hiremath on 16/11/16.
 */
public class VideoController extends FrameLayout
{
    Context mContext;
    SeekBar seekBar;
    TextView tvDuration;
    TextView tvCurrent;
    ImageView ivPlay;
    ImageView ivFullscreen;
    LinearLayout footer;
    VideoLoadingView videoLoadingView;
    CountDownTimer countDownTimer;
    TextView tv_connectionLost;

    int switch_tag = 0;

    public VideoController(Context context)
    {
        super(context);
    }

    public VideoController(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        mContext = context;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        inflater.inflate(R.layout.media_controller, this);

        seekBar = (SeekBar) findViewById(R.id.mediacontroller_progress);
        tvDuration = (TextView) findViewById(R.id.time);
        tvCurrent = (TextView) findViewById(R.id.time_current);
        ivPlay = (ImageView) findViewById(R.id.play_pause);
        ivFullscreen = (ImageView) findViewById(R.id.fullscreen);
        footer = (LinearLayout) findViewById(R.id.footer);
        videoLoadingView = (VideoLoadingView) findViewById(R.id.video_loading);
        tv_connectionLost = (TextView) findViewById(R.id.tv_connection_lost);
    }

    public VideoController(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }

    public SeekBar getSeekBar()
    {
        return seekBar;
    }

    public TextView getVideoDurationView()
    {
        return tvDuration;
    }

    public TextView getVideoCurrentView()
    {
        return tvCurrent;
    }

    public ImageView getPlayButton()
    {
        return ivPlay;
    }

    public ImageView getFullscreen()
    {
        return ivFullscreen;
    }

    public TextView getConnectionLost()
    {
        return tv_connectionLost;
    }

    public void startLoading()
    {
        //start animation for loading
        ivPlay.setImageResource(R.drawable.video_loading_icon);
        startAnimation(ivPlay);
        footer.setVisibility(View.INVISIBLE);


        ivPlay.setVisibility(View.GONE);
        videoLoadingView.setVisibility(View.VISIBLE);
    }

    public void stopLoading()
    {
        //stop animation for loading
        stopAnimation(ivPlay);
        ivPlay.setImageResource(R.drawable.pause_btn);
        footer.setVisibility(View.VISIBLE);

        videoLoadingView.setVisibility(View.GONE);
        ivPlay.setImageResource(R.drawable.pause_btn);
        ivPlay.setVisibility(View.VISIBLE);
    }

    public void stopLoadingError()
    {
        //stop animation for loading for error
        stopAnimation(ivPlay);
        ivPlay.setImageResource(R.drawable.new_play_error);
        ivPlay.setVisibility(View.GONE);
        footer.setVisibility(View.INVISIBLE);
        videoLoadingView.setVisibility(View.GONE);

        tv_connectionLost.setVisibility(View.VISIBLE);
        setVisibility(View.VISIBLE);
    }

    private void startAnimation(View view)
    {
        final Animation rotate = AnimationUtils.loadAnimation(getContext(), R.anim.rotate);
        view.startAnimation(rotate);
    }

    private void stopAnimation(View view)
    {
        view.clearAnimation();
    }
}
