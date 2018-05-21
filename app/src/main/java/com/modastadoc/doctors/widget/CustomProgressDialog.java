package com.modastadoc.doctors.widget;

import android.animation.Animator;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.animation.AccelerateDecelerateInterpolator;


import com.modastadoc.doctors.R;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by vijay.hiremath on 22/04/16.
 */
public class CustomProgressDialog extends ProgressDialog {

    String TAG = CustomProgressDialog.class.getSimpleName();
    Timer timer;
    Context mContext;
    CircularLoader loader;

    public CustomProgressDialog(Context context ) {

        super(context, R.style.CustomProgressDialogTheme);
        mContext  = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.custom_progress_layout);

        final Activity mActivity = (Activity)mContext;

        loader = (CircularLoader) findViewById(R.id.loader);
        loader.setProgress(100);
        loader.startAnimation();

        loader.getAnimator().addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {}

            @Override
            public void onAnimationEnd(Animator animation) {
                loader.reload();
            }

            @Override
            public void onAnimationCancel(Animator animation) {}

            @Override
            public void onAnimationRepeat(Animator animation) {}
        });

        loader.getAnimator().setInterpolator(new AccelerateDecelerateInterpolator());

    }

    public void stopLoading() {
        loader.getAnimator().removeAllListeners();
        loader.getAnimator().end();
        loader.getAnimator().cancel();
        loader.clearAnimation();
    }
}
