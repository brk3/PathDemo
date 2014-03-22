package com.bourke.pathdemo.app;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.bourke.pathdemo.app.SvgHelper.SvgPath;

public class DemoView extends View {

    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private SvgPath mWaitPath;

    private float mWait;

    public DemoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DemoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init();
    }

    private void init() {
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(10.0f);
        mPaint.setColor(Color.rgb(164, 199, 57));

        createWaitPath();

        int mDuration = 4000;
        ObjectAnimator mWaitAnimator = ObjectAnimator.ofFloat(this, "wait", 1.0f, 0.0f)
                .setDuration(mDuration);
        mWaitAnimator.setRepeatMode(ObjectAnimator.RESTART);
        mWaitAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        mWaitAnimator.setInterpolator(new LinearInterpolator());
        mWaitAnimator.start();
    }

    private void createWaitPath() {
        Path p = new Path();
        p.moveTo(32, 32);
        p.lineTo(232, 32);

        mWaitPath = new SvgHelper.SvgPath(p, mPaint);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(mWaitPath.path, mWaitPath.paint);
    }

    public float getWait() {
        return mWait;
    }

    public void setWait(float wait) {
        mWait = wait;
        float targetLength = 32.0f;
        mWaitPath.paint.setPathEffect(createPathEffect(mWaitPath.length, mWait, targetLength));
        invalidate();
    }

    private static PathEffect createPathEffect(float pathLength, float phase, float offset) {
        return new DashPathEffect(new float[] { pathLength, pathLength },
                Math.max(phase * pathLength, offset));
    }
}
