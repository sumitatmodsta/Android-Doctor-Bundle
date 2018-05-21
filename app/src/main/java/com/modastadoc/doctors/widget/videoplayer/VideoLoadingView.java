package com.modastadoc.doctors.widget.videoplayer;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by vijay.hiremath on 16/11/16.
 */
public class VideoLoadingView extends View
{
    public static final int BallSpinFadeLoader = 22;
    public static final int DEFAULT_SIZE = 45;

    int mIndicatorId;
    int mIndicatorColor;

    Paint mPaint;

    BaseIndicatorController mIndicatorController;

    private boolean mHasAnimation;

    public VideoLoadingView(Context context)
    {
        super(context);
        init(null, 0);
    }

    public VideoLoadingView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init(attrs, 0);
    }

    public VideoLoadingView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public VideoLoadingView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes)
    {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs, defStyleAttr);
    }

    private void init(AttributeSet attrs, int defStyle)
    {

//        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.AVLoadingIndicatorView);
//        mIndicatorId=a.getInt(R.styleable.AVLoadingIndicatorView_indicator, BallPulse);
//        mIndicatorColor=a.getColor(R.styleable.AVLoadingIndicatorView_indicator_color, Color.WHITE);
//        a.recycle();

        mPaint = new Paint();
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);

        applyIndicator();
    }

    private void applyIndicator()
    {
        mIndicatorController = new BallSpinFadeLoaderIndicator();
        mIndicatorController.setTarget(this);
    }

    @Override
    protected void onDetachedFromWindow()
    {
        super.onDetachedFromWindow();
        mIndicatorController.setAnimationStatus(BaseIndicatorController.AnimStatus.CANCEL);
    }

    void drawIndicator(Canvas canvas)
    {
        mIndicatorController.draw(canvas, mPaint);
    }

    void applyAnimation()
    {
        mIndicatorController.initAnimation();
    }

    private int dp2px(int dpValue)
    {
        return (int) getContext().getResources().getDisplayMetrics().density * dpValue;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom)
    {
        super.onLayout(changed, left, top, right, bottom);
        if (!mHasAnimation)
        {
            mHasAnimation = true;
            applyAnimation();
        }
    }

    @Override
    public void setVisibility(int v)
    {
        if (getVisibility() != v)
        {
            super.setVisibility(v);
            if (v == GONE || v == INVISIBLE)
            {
                mIndicatorController.setAnimationStatus(BaseIndicatorController.AnimStatus.END);
            } else
            {
                mIndicatorController.setAnimationStatus(BaseIndicatorController.AnimStatus.START);
            }
        }
    }

    @Override
    protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();
        if (mHasAnimation)
        {
            mIndicatorController.setAnimationStatus(BaseIndicatorController.AnimStatus.START);
        }
    }


    private int measureDimension(int defaultSize, int measureSpec)
    {
        int result = defaultSize;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        if (specMode == MeasureSpec.EXACTLY)
        {
            result = specSize;
        } else if (specMode == MeasureSpec.AT_MOST)
        {
            result = Math.min(defaultSize, specSize);
        } else
        {
            result = defaultSize;
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        drawIndicator(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int width = measureDimension(dp2px(DEFAULT_SIZE), widthMeasureSpec);
        int height = measureDimension(dp2px(DEFAULT_SIZE), heightMeasureSpec);
        setMeasuredDimension(width, height);
    }
}
