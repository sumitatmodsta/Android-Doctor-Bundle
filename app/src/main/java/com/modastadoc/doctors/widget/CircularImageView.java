package com.modastadoc.doctors.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import com.modastadoc.doctors.R;

/**
 * Created by contractor.anooj on 05/02/16.
 */
public class CircularImageView extends ImageView {

    private static final float DEFAULT_SHADOW_RADIUS = 8.0f;

    private int borderWidth;
    private int canvasSize;
    private float shadowRadius;
    private int shadowColor = Color.BLACK;
    private Bitmap image;
    private Drawable drawable;
    private Paint paint;
    private Paint paintBorder;

    public CircularImageView(final Context context) {
        this(context, null);
    }

    public CircularImageView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.circularImageViewStyle);
    }

    public CircularImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CircularImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        paint = new Paint();
        paint.setAntiAlias(true);

        paintBorder = new Paint();
        paintBorder.setAntiAlias(true);

        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.CircularImageView, defStyleAttr, 0);

        if (attributes.getBoolean(R.styleable.CircularImageView_border, true)) {
            int defaultBorderSize = (int) (2 * getContext().getResources().getDisplayMetrics().density + 0.5f);
            setBorderWidth(attributes.getDimensionPixelOffset(R.styleable.CircularImageView_border_width, defaultBorderSize));
            //setBorderColor(attributes.getColor(R.styleable.CircularImageView_border_color, Color.WHITE));
            setBorderColor(attributes.getColor(R.styleable.CircularImageView_border_color, getResources().getColor(R.color.circleBorder)));
        }

        if (attributes.getBoolean(R.styleable.CircularImageView_shadow, false)) {
            shadowRadius = DEFAULT_SHADOW_RADIUS;
            drawShadow(attributes.getFloat(R.styleable.CircularImageView_shadow_radius, shadowRadius), attributes.getColor(R.styleable.CircularImageView_shadow_colors, shadowColor));
        }
    }

    public void setBorderWidth(int borderWidth) {
        this.borderWidth = borderWidth;
        requestLayout();
        invalidate();
    }

    public void setBorderColor(int borderColor) {
        if (paintBorder != null)
            paintBorder.setColor(borderColor);
        invalidate();
    }

    public void addShadow() {
        if (shadowRadius == 0)
            shadowRadius = DEFAULT_SHADOW_RADIUS;
        drawShadow(shadowRadius, shadowColor);
        invalidate();
    }

    public void setShadowRadius(float shadowRadius) {
        drawShadow(shadowRadius, shadowColor);
        invalidate();
    }

    public void setShadowColor(int shadowColor) {
        drawShadow(shadowRadius, shadowColor);
        invalidate();
    }
    @Override
    public void onDraw(Canvas canvas) {
        loadBitmap();

        if (image == null)
            return;

        canvasSize = canvas.getWidth();
        if (canvas.getHeight() < canvasSize) {
            canvasSize = canvas.getHeight();
        }

        int circleCenter = (canvasSize - (borderWidth * 2)) / 2;
        canvas.drawCircle(circleCenter + borderWidth, circleCenter + borderWidth, circleCenter + borderWidth - (shadowRadius + shadowRadius / 2), paintBorder);
        canvas.drawCircle(circleCenter + borderWidth, circleCenter + borderWidth, circleCenter - (shadowRadius + shadowRadius / 2), paint);
    }

    private void loadBitmap() {
        if (this.drawable == getDrawable())
            return;

        this.drawable = getDrawable();
        this.image = drawableToBitmap(this.drawable);
        updateShader();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        canvasSize = w;
        if (h < canvasSize)
            canvasSize = h;
        if (image != null)
            updateShader();
    }

    private void drawShadow(float shadowRadius, int shadowColor) {
        this.shadowRadius = shadowRadius;
        this.shadowColor = shadowColor;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setLayerType(LAYER_TYPE_SOFTWARE, paintBorder);
        }
        paintBorder.setShadowLayer(shadowRadius, 0.0f, shadowRadius / 2, shadowColor);
    }

    private void updateShader() {
        if (this.image == null)
            return;
        BitmapShader shader = new BitmapShader(Bitmap.createScaledBitmap(
                ThumbnailUtils.extractThumbnail(image, canvasSize, canvasSize), canvasSize, canvasSize, false),
                Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        paint.setShader(shader);
    }

    private Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable == null) {
            return null;
        } else if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        int intrinsicWidth = drawable.getIntrinsicWidth();
        int intrinsicHeight = drawable.getIntrinsicHeight();

        if (!(intrinsicWidth > 0 && intrinsicHeight > 0))
            return null;

        try {
            Bitmap bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        } catch (OutOfMemoryError e) {
            Log.e(getClass().toString(), "OutOfMemoryError while generating bitmap!");
            return null;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = measureWidth(widthMeasureSpec);
        int height = measureHeight(heightMeasureSpec);
        int imageSize = (width < height) ? width : height;
        setMeasuredDimension(imageSize, imageSize);
    }

    private int measureWidth(int measureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else if (specMode == MeasureSpec.AT_MOST) {
            result = specSize;
        } else {
            result = canvasSize;
        }

        return result;
    }

    private int measureHeight(int measureSpecHeight) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpecHeight);
        int specSize = MeasureSpec.getSize(measureSpecHeight);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else if (specMode == MeasureSpec.AT_MOST) {
            result = specSize;
        } else {
            result = canvasSize;
        }

        return (result + 2);
    }
}
