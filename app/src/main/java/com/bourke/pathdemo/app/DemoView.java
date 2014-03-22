package com.bourke.pathdemo.app;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.bourke.pathdemo.app.SvgHelper.SvgPath;

import java.util.ArrayList;
import java.util.List;

public class DemoView extends View {

    private static final String TAG = "PathDemo/DemoView";

    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final SvgHelper mSvg = new SvgHelper(mPaint);

    private ObjectAnimator mWaitAnimator;

    private List<SvgPath> mPaths = new ArrayList<SvgPath>(0);

    private Thread mLoader;
    private final Object mSvgLock = new Object();

    private float mWait;

    private int mDuration = 3000;
    private float mFadeFactor = 10.0f;
    private float mParallax = 1.0f;

    public DemoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DemoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init();
    }

    @Override
    protected void onSizeChanged(final int w, final int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        Log.d(TAG, "onSizeChanged");

        if (mLoader != null) {
            try {
                mLoader.join();
            } catch (InterruptedException e) {
                Log.e(TAG, "Unexpected error", e);
            }
        }

        mLoader = new Thread(new Runnable() {
            @Override
            public void run() {
                mSvg.load(getContext(), R.raw.android_robot);
                synchronized (mSvgLock) {
                    mPaths = mSvg.getPathsForViewport(
                            w - getPaddingLeft() - getPaddingRight(),
                            h - getPaddingTop() - getPaddingBottom());
                    updatePathsPhaseLocked();
                }
            }
        }, "SVG Loader");
        mLoader.start();
    }

    private void updatePathsPhaseLocked() {
        final int count = mPaths.size();
        for (int i = 0; i < count; i++) {
            SvgHelper.SvgPath svgPath = mPaths.get(i);
            svgPath.paint.setPathEffect(createPathEffect(svgPath.length, mWait, 0.0f));
        }
    }

    private void init() {
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(6.0f);
        mPaint.setColor(Color.rgb(164, 199, 57));

        mWaitAnimator = ObjectAnimator.ofFloat(this, "wait", 1.0f, 0.0f).setDuration(mDuration);
        mWaitAnimator.setRepeatMode(ObjectAnimator.RESTART);
        mWaitAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        mWaitAnimator.setInterpolator(new LinearInterpolator());
        mWaitAnimator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        synchronized (mSvgLock) {
            canvas.save();
            canvas.translate(getPaddingLeft(), getPaddingTop());// + mOffsetY);
            final int count = mPaths.size();
            for (int i = 0; i < count; i++) {
                SvgHelper.SvgPath svgPath = mPaths.get(i);

                // We use the fade factor to speed up the alpha animation
                int alpha = (int) (Math.min((1.0f - mWait) * mFadeFactor, 1.0f) * 255.0f);
                svgPath.paint.setAlpha((int) (alpha * mParallax));

                canvas.drawPath(svgPath.path, svgPath.paint);
            }
            canvas.restore();
        }
    }

    public float getWait() {
        return mWait;
    }

    public void setWait(float wait) {
        mWait = wait;
        synchronized (mSvgLock) {
            updatePathsPhaseLocked();
        }
        invalidate();
    }

    private static PathEffect createPathEffect(float pathLength, float phase, float offset) {
        return new DashPathEffect(new float[] { pathLength, pathLength },
                Math.max(phase * pathLength, offset));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        switch(event.getAction()) {
            case (MotionEvent.ACTION_UP) :
                mWaitAnimator.start();
                return true;
            default :
                return super.onTouchEvent(event);
        }
    }
}
